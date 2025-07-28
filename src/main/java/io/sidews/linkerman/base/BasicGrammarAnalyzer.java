package io.sidews.linkerman.base;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.*;
import org.eclipse.xtext.serializer.ISerializationContext;
import org.eclipse.xtext.serializer.analysis.IGrammarConstraintProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BasicGrammarAnalyzer implements GrammarAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(BasicGrammarAnalyzer.class);

    private final Grammar grammar;

    private final IGrammarConstraintProvider constraintProvider;

    public BasicGrammarAnalyzer(Grammar grammar, IGrammarConstraintProvider constraintProvider) {
        this.grammar = grammar;
        this.constraintProvider = constraintProvider;

    }

    @Override
    public boolean isASTRoot(EClass eClass) {
        var entryRule = GrammarUtil.allParserRules(grammar).get(0);
        if (entryRule.getType() != null && entryRule.getType().getClassifier() != null) {
            return eClass.equals(entryRule.getType().getClassifier());
        }
        return false;
    }

    @Override
    public Set<EReference> getAllSymbolContainers(EClass symbolType) {
        return getSymbolContainersIn(getAllReferencesIn(symbolType.getEPackage()), symbolType);
    }

    @Override
    public Set<EReference> getSymbolContainers(EClass symbolEClass, EClass parentEClass) {
        return getSymbolContainersIn(parentEClass.getEAllReferences().stream(), symbolEClass);
    }

    private static Stream<EReference> getAllReferencesIn(EPackage ePackage) {
        return ePackage.getEClassifiers()
                .stream()
                .filter(c -> c instanceof EClass)
                .map(EClass.class::cast)
                .flatMap(c -> c.getEAllReferences().stream());
    }

    private Set<EReference> getSymbolContainersIn(Stream<EReference> possibleReferences, EClass symbolType) {
        return possibleReferences.filter(ref -> ref.getEReferenceType().isSuperTypeOf(symbolType))
                .filter(ref -> getSymbolReturnTypes(ref.getEReferenceType()).stream()
                        .anyMatch(symbolType::isSuperTypeOf))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<EClass> getSymbolReturnTypes(EClass symbolType) {
        var returnTypes = new HashSet<EClass>();
        var parserRules = findParserRule(symbolType);
        logger.debug("Parser rule count for {}: {}", symbolType.getName(), parserRules.size());
        if (!parserRules.isEmpty()) {
            parserRules.forEach(r -> {
                logger.debug("Exploring rule: {}", r.getName());
                findReturnTypesForElement(
                        r.getAlternatives(), symbolType, returnTypes, new HashSet<>());
                logger.debug("**********************************************************");
            });
        }
        else if (isActionRefType(symbolType)) {
            returnTypes.add(symbolType);
        }
        return returnTypes;
    }

    private static EClass findReturnTypesForElement(AbstractElement element,
                                                    EClass currentType,
                                                    Set<EClass> returnTypes,
                                                    Set<EClass> visitedTypes) {
        if (visitedTypes.contains(currentType)) {
            //return currentType;
        }
        //visitedTypes.add(currentType);
        logger.debug("Current type: {}", currentType.getName());
        logger.debug("Return types count: {}", returnTypes.size());

        if (returnTypes.size() == 27) {
            throw new RuntimeException();
        }

        switch (element) {
            case CompoundElement container -> {
                logger.debug("Element: {}", container instanceof Group ? "Group" : "Compound Element");
                for (var childElement : container.getElements()) {
                    currentType = findReturnTypesForElement(childElement, currentType, returnTypes, visitedTypes);
                }
            }
            case RuleCall ruleCall when ruleCall.getRule() instanceof ParserRule parserRule -> {
                var newPathType = (EClass) parserRule.getType().getClassifier();
                logger.debug("Element: Call -> {}", parserRule.getName());
                /*if (currentType.isSuperTypeOf(newPathType) && currentType != newPathType) {
                    findReturnTypesForElement(parserRule.getAlternatives(), newPathType, returnTypes, visitedTypes);
                }*/
                findReturnTypesForElement(parserRule.getAlternatives(), newPathType, returnTypes, visitedTypes);
            }
            case Action action -> {
                currentType = (EClass) action.getType().getClassifier();
                returnTypes.add(currentType);
            }
            case null, default -> returnTypes.add(currentType);
        }

        logger.debug("--------------------------------");
        return currentType;
    }

    public List<ParserRule> findParserRule(EClass symbolType) {
        return GrammarUtil.allParserRules(grammar)
                .stream()
                .filter(r -> r.getType().getClassifier().equals(symbolType))
                .toList();
    }

    private boolean isActionRefType(EClass refType) {
        return GrammarUtil.allParserRules(grammar)
                .stream()
                .flatMap(r -> EcoreUtil2.getAllContentsOfType(r, Action.class).stream())
                .map(Action::getType)
                .map(TypeRef::getClassifier)
                .anyMatch(c -> c.equals(refType));
    }

    @Override
    public boolean isFeatureRequired(EStructuralFeature feature) {
        var constraintMap = constraintProvider.getConstraints(grammar);
        var contexts = new ArrayList<ISerializationContext>();
        for (var entry : constraintMap.values()) {
            contexts.addAll(entry.getContexts(feature.getEContainingClass()));
        }
        for (var context : contexts) {
            var constraint = constraintMap.get(context);
            for (var featureInfo : constraint.getFeatures()) {
                if (featureInfo.getFeature().equals(feature)) {
                    return featureInfo.getLowerBound() > 0;
                }
            }
        }
        return false;
    }

}