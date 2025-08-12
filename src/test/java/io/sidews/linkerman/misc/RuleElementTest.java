package io.sidews.linkerman.misc;

import io.sidews.linkerman.LinkerScriptContext;
import org.eclipse.xtext.*;
import org.eclipse.xtext.util.Strings;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleElementTest {

    private static final Logger logger = LoggerFactory.getLogger(RuleElementTest.class);

    private static final LinkerScriptContext ctx = new LinkerScriptContext();
    private static final Grammar grammar = ctx.getGrammar();

    @Test
    void testLogAlternativesWithDepth() {
        var parserRule = findParserRuleByName("LExpression");
        logElementTree(parserRule.getAlternatives(), 8, 0);
    }

    public static void logElementTree(AbstractElement element, int maxDepth, int indentLevel) {
        if (element == null || maxDepth < 0) return;

        logElement(element, indentLevel);

        // Recurse based on type
        if (maxDepth == 0) return;

        if (element instanceof Alternatives alt) {
            for (AbstractElement sub : alt.getElements()) {
                logElementTree(sub, maxDepth - 1, indentLevel + 1);
            }
        } else if (element instanceof Group group) {
            for (AbstractElement sub : group.getElements()) {
                logElementTree(sub, maxDepth - 1, indentLevel + 1);
            }
        } else if (element instanceof UnorderedGroup uGroup) {
            for (AbstractElement sub : uGroup.getElements()) {
                logElementTree(sub, maxDepth - 1, indentLevel + 1);
            }
        } else if (element instanceof Assignment assign) {
            logElementTree(assign.getTerminal(), maxDepth - 1, indentLevel + 1);
        } else if (element instanceof Action || element instanceof RuleCall
                || element instanceof Keyword || element instanceof CrossReference) {
            // Leaf elements, do not recurse
        } else {
            logger.warn("{}[Unhandled type] {}", "  ".repeat(indentLevel), element.getClass().getSimpleName());
        }
    }

    private static void logElement(AbstractElement element, int indentLevel) {
        String indent = "  ".repeat(indentLevel);
        String message;

        if (element instanceof Alternatives) {
            message = indent + "[Alternatives]";
        } else if (element instanceof Group) {
            message = indent + "[Group]";
        } else if (element instanceof UnorderedGroup) {
            message = indent + "[UnorderedGroup]";
        } else if (element instanceof Assignment assign) {
            message = String.format(
                    "%s[Assignment] feature='%s', operator='%s'",
                    indent, assign.getFeature(), assign.getOperator()
            );
        } else if (element instanceof RuleCall ruleCall) {
            String ruleName = ruleCall.getRule() != null ? ruleCall.getRule().getName() : "<null>";
            message = indent + "[RuleCall] to: " + ruleName;
        } else if (element instanceof Keyword keyword) {
            message = indent + "[Keyword] '" + keyword.getValue() + "'";
        } else if (element instanceof CrossReference xref) {
            message = indent + "[CrossReference] " + Strings.emptyIfNull(xref.toString()).trim();
        } else if (element instanceof Action action) {
            message = indent + "[Action] " + Strings.emptyIfNull(action.toString()).trim();
        } else {
            message = indent + "[Unknown] " + element.getClass().getSimpleName() + ": " + Strings.emptyIfNull(element.toString()).trim();
        }

        logger.info(message);
    }

    private static ParserRule findParserRuleByName(String ruleName) {
        return GrammarUtil.allParserRules(grammar)
                .stream()
                .filter(rule -> ruleName.equals(rule.getName()))
                .findFirst()
                .orElseThrow();
    }
}
