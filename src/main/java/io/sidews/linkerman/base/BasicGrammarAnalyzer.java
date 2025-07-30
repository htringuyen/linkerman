package io.sidews.linkerman.base;

import io.sidews.linkerman.DSLContext;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.*;
import org.eclipse.xtext.serializer.ISerializationContext;
import org.eclipse.xtext.serializer.analysis.IGrammarConstraintProvider;
import org.eclipse.xtext.serializer.analysis.SerializationContextMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BasicGrammarAnalyzer implements GrammarAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(BasicGrammarAnalyzer.class);

    private final DSLContext context;

    private final SerializationContextMap<IGrammarConstraintProvider.IConstraint> constraintMap;

    public BasicGrammarAnalyzer(DSLContext context) {
        this.context = context;
        constraintMap = context.getGrammarConstraintProvider().getConstraints(context.getGrammar());
    }

    @Override
    public boolean isASTRoot(EClass eClass) {
        var entryRule = GrammarUtil.allParserRules(context.getGrammar()).getFirst();
        if (entryRule.getType() != null && entryRule.getType().getClassifier() != null) {
            return eClass.equals(entryRule.getType().getClassifier());
        }
        return false;
    }

    @Override
    public List<EReference> getAllSymbolContainers(EClass symbolType) {
        return getSymbolContainersIn(getAllReferencesIn(symbolType.getEPackage()), symbolType);
    }

    @Override
    public List<EReference> getSymbolContainers(EClass symbolEClass, EClass parentEClass) {
        return getSymbolContainersIn(parentEClass.getEAllReferences().stream(), symbolEClass);
    }

    private static Stream<EReference> getAllReferencesIn(EPackage ePackage) {
        return ePackage.getEClassifiers()
                .stream()
                .filter(c -> c instanceof EClass)
                .map(EClass.class::cast)
                .flatMap(c -> c.getEAllReferences().stream());
    }

    private List<EReference> getSymbolContainersIn(Stream<EReference> possibleReferences, EClass symbolType) {
        var symbolReturnTypesCache = new HashMap<EClass, Collection<EClass>>();

        return possibleReferences
                .filter(ref -> {
                    EClass refType = ref.getEReferenceType();
                    if (!refType.isSuperTypeOf(symbolType)) {
                        return false;
                    }
                    Collection<EClass> returnTypes = symbolReturnTypesCache.computeIfAbsent(
                            refType, this::getSymbolReturnTypes);
                    return returnTypes.stream().anyMatch(symbolType::isSuperTypeOf);
                })
                .distinct()
                .toList();
    }

    public List<EClass> getSymbolReturnTypes(EClass symbolType) {
        var result = new LinkedHashSet<EClass>();
        var parserRules = findParserRuleByType(symbolType);
        if (!parserRules.isEmpty()) {
            var resultPaths = new ArrayList<Stack<EClass>>();
            parserRules.forEach(rule -> {
                findReturnTypesForRule(rule, resultPaths);
            });
            resultPaths.forEach(path -> result.add(path.pop()));
        }
        else if (isActionRefType(symbolType)) {
            // do simple processing that work for all cases of linkerscript dsl
            // but for other language, there may be rare exceptions
            // however, to handle these rare exceptions requires a much more complicated algorithms
            result.add(symbolType);
        }
        else {
            throw new DynamicProcessingException("Unknown rare case");
        }
        return result.stream().toList();
    }

    private void findReturnTypesForRule(ParserRule rule, List<Stack<EClass>> resultPaths) {
        var element = rule.getAlternatives();
        if (element instanceof Alternatives alternatives) {
            for (var alt : alternatives.getElements()) {
                if (alt instanceof RuleCall call
                        && call.getRule() instanceof ParserRule altRule) {
                    findReturnTypesForRule(altRule, resultPaths);
                }
                else {
                    var rootPath = createRootPathFor(rule);
                    resultPaths.add(rootPath);
                    findReturnTypesFromSimpleElement(alt, rootPath, resultPaths);
                }
            }
        }
        else if (element instanceof RuleCall call
                && call.getRule() instanceof ParserRule altRule) {
            findReturnTypesForRule(altRule, resultPaths);
        }
        else {
            var rootPath = createRootPathFor(rule);
            resultPaths.add(rootPath);
            findReturnTypesFromSimpleElement(element, rootPath, resultPaths);
        }
    }

    private Stack<EClass> createRootPathFor(ParserRule rule) {
        var path = new Stack<EClass>();
        path.push((EClass) rule.getType().getClassifier());
        return path;
    }

    private void findReturnTypesFromSimpleElement(AbstractElement element, Stack<EClass> currentPath, List<Stack<EClass>> resultPaths) {

        switch (element) {
            case Alternatives alternatives -> {
                for (var childElement : alternatives.getElements()) {
                    var branchingPath = new Stack<EClass>();
                    branchingPath.addAll(currentPath); // is the copy necessary?
                    resultPaths.add(branchingPath);
                    findReturnTypesFromSimpleElement(childElement, branchingPath, resultPaths);
                }
            }
            case Group group -> {
                for (var childElement : group.getElements()) {
                    findReturnTypesFromSimpleElement(childElement, currentPath, resultPaths);
                }
            }
            case RuleCall ruleCall -> {
                // ignore non-direct rule call
            }
            case Action action -> {
                currentPath.push((EClass) action.getType().getClassifier());
            }
            case null, default -> {
                // ignore
            }
        }
    }

    public List<ParserRule> findParserRuleByType(EClass symbolType) {
        return GrammarUtil.allParserRules(context.getGrammar())
                .stream()
                .filter(r -> r.getType().getClassifier().equals(symbolType))
                .toList();
    }

    private boolean isActionRefType(EClass refType) {
        return GrammarUtil.allParserRules(context.getGrammar())
                .stream()
                .flatMap(r -> EcoreUtil2.getAllContentsOfType(r, Action.class).stream())
                .map(Action::getType)
                .map(TypeRef::getClassifier)
                .anyMatch(c -> c.equals(refType));
    }

    @Override
    public boolean isFeatureRequired(EStructuralFeature feature) {
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