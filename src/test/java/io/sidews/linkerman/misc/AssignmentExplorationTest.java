package io.sidews.linkerman.misc;

import io.sidews.linkerman.LinkerScriptContext;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AssignmentExplorationTest {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentExplorationTest.class);

    private static final LinkerScriptContext context = new LinkerScriptContext();

    private static final Grammar grammar = context.getGrammar();

    @Test
    void testFindTerminalForFeature_All() {
        var pkg = context.getLinkerScriptPackage();
        for (var symbolClassifier : pkg.getEClassifiers()) {
            if (symbolClassifier instanceof EClass symbolType) {
                for (var feature : symbolType.getEAllReferences()) {
                    var featureName = feature.getName();
                    var terminal = findTerminalForFeature(symbolType, featureName, grammar);
                    logTerminalInfo(terminal, featureName, symbolType);
                }
            }
            else {
                logger.info("Outlier Classifier: {}", symbolClassifier.getName());
            }
        }
    }

    @Test
    void testFindTerminalForFeature_StatementAssignment() {
        var pkg = context.getLinkerScriptPackage();
        var symbolType = pkg.getStatementAssignment();
        var featureName = "assignment";
        var terminal = findTerminalForFeature(symbolType, featureName, grammar);
        logTerminalInfo(terminal, featureName, symbolType);
    }

    public static AbstractElement findTerminalForFeature(EClass eClass, String featureName, Grammar grammar) {
        return grammar.getRules().stream()
                .filter(ParserRule.class::isInstance)
                .map(ParserRule.class::cast)
                .filter(rule -> {
                    // Check if rule's return type matches
                    if (eClass.isSuperTypeOf((EClass) rule.getType().getClassifier())) {
                        return true;
                    }
                    // Check if any action within the rule creates this EClass
                    TreeIterator<EObject> iterator = rule.eAllContents();
                    while (iterator.hasNext()) {
                        EObject obj = iterator.next();
                        if (obj instanceof Action) {
                            Action action = (Action) obj;
                            if (eClass.isSuperTypeOf((EClass) action.getType().getClassifier())) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .flatMap(rule -> {
                    List<EObject> contents = new ArrayList<>();
                    TreeIterator<EObject> iterator = rule.eAllContents();
                    iterator.forEachRemaining(contents::add);
                    return contents.stream();
                })
                .filter(Assignment.class::isInstance)
                .map(Assignment.class::cast)
                .filter(assignment -> featureName.equals(assignment.getFeature()))
                .map(Assignment::getTerminal)
                .findFirst()
                .orElse(null);
    }

    public static void logTerminalInfo(AbstractElement terminal, String featureName, EClass eClass) {
        if (terminal == null) {
            logger.warn("No terminal found for feature '{}' in EClass '{}'", featureName, eClass.getName());
            return;
        }

        logger.info("Terminal info for feature '{}' in EClass '{}':", featureName, eClass.getName());
        logger.info("  Terminal type: {}", terminal.getClass().getSimpleName());

        if (terminal instanceof RuleCall) {
            RuleCall ruleCall = (RuleCall) terminal;
            AbstractRule rule = ruleCall.getRule();
            logger.info("  RuleCall details:");
            logger.info("    Called rule: {}", rule.getName());
            logger.info("    Rule type: {}", rule.getClass().getSimpleName());
            if (rule instanceof ParserRule) {
                ParserRule parserRule = (ParserRule) rule;
                if (parserRule.getType() != null) {
                    logger.info("    Returns: {}", parserRule.getType().getClassifier().getName());
                }
            }
        } else if (terminal instanceof CrossReference) {
            CrossReference crossRef = (CrossReference) terminal;
            logger.info("  CrossReference details:");
            logger.info("    Referenced type: {}", crossRef.getType().getClassifier().getName());
            if (crossRef.getTerminal() instanceof RuleCall) {
                RuleCall terminalRule = (RuleCall) crossRef.getTerminal();
                logger.info("    Terminal rule: {}", terminalRule.getRule().getName());
            }
        } else if (terminal instanceof Keyword) {
            Keyword keyword = (Keyword) terminal;
            logger.info("  Keyword details:");
            logger.info("    Value: '{}'", keyword.getValue());
        } else if (terminal instanceof Alternatives) {
            Alternatives alternatives = (Alternatives) terminal;
            logger.info("  Alternatives details:");
            logger.info("    Number of alternatives: {}", alternatives.getElements().size());
            for (int i = 0; i < alternatives.getElements().size(); i++) {
                AbstractElement element = alternatives.getElements().get(i);
                logger.info("    Alternative {}: {}", i + 1, element.getClass().getSimpleName());
            }
        } else if (terminal instanceof Group) {
            Group group = (Group) terminal;
            logger.info("  Group details:");
            logger.info("    Number of elements: {}", group.getElements().size());
        } else {
            logger.info("  Other terminal type: {}", terminal.getClass().getName());
        }

        // Log cardinality if present
        if (terminal.getCardinality() != null) {
            logger.info("  Cardinality: {}", terminal.getCardinality());
        }
    }
}
