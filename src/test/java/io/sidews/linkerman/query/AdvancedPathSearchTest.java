package io.sidews.linkerman.query;

import io.sidews.linkerman.LinkerScriptContext;
import io.sidews.linkerman.base.BasicGrammarAnalyzer;
import io.sidews.linkerman.base.SymbolManager;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptPackage;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Stack;

public class AdvancedPathSearchTest {

    private static final Logger logger = LoggerFactory.getLogger(PathSearcherOldTest.class);

    private static AdvancedPathSearcher pathSearcher;

    private static LinkerScriptContext ctx;

    private static LinkerScriptPackage pkg;

    @BeforeAll
    static void globalSetup() {
        ctx = new LinkerScriptContext();
        pkg = ctx.getLinkerScriptPackage();
        var grammarAnalyzer = new BasicGrammarAnalyzer(ctx);
        var symbolManager = new SymbolManager(grammarAnalyzer, ctx);
        pathSearcher = new AdvancedPathSearcher(symbolManager);
    }

    @Test
    void testFindPathsBound_Assignment_LinkerScript() {
        testFindPathsBound_From_To(pkg.getAssignment(), pkg.getLinkerScript());
    }

    private void testFindPathsBound_From_To(EClass fromType, EClass toType) {
        var rootEdge = pathSearcher.findPathsBound(pkg.getAssignment(), pkg.getLinkerScript());
        logAllPaths(rootEdge);
    }

    @Test
    void testFindExplicitPathsBound_Assignment_LinkerScript() {
        var paths = pathSearcher.findExplicitPathsBound(pkg.getLExpression(), pkg.getLinkerScript());
        for (var path : paths) {
            logger.info("\t{}", pathToString(path));
        }
    }

    public static void logAllPaths(AdvancedPathSearcher.ExpandingEdge root) {
        logPathsRecursive(root, "");
    }

    private static void logPathsRecursive(AdvancedPathSearcher.ExpandingEdge edge, String path) {
        String currentName = edge.value() != null ? edge.value().getName() : null;

        String newPath = (currentName == null)
                ? path
                : (path.isEmpty() ? currentName : path + " -> " + currentName);

        List<AdvancedPathSearcher.ExpandingEdge> children = edge.childEdges();
        if (children.isEmpty()) {
            logger.info("Path: {}", newPath);
        } else {
            for (AdvancedPathSearcher.ExpandingEdge child : children) {
                logPathsRecursive(child, newPath);
            }
        }
    }

    private String pathToString(Stack<EReference> path) {
        var logMessage = new StringBuilder(path.peek().getEContainingClass().getName());
        for (var edge : path.reversed()) {
            logMessage.append(String.format(" -[%s]-> ", edge.getName()));
            logMessage.append(edge.getEReferenceType().getName());
        }
        return logMessage.toString();
    }
}













