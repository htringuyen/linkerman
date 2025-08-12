package io.sidews.linkerman.query;

import io.sidews.linkerman.LinkerScriptContext;
import io.sidews.linkerman.base.BasicGrammarAnalyzer;
import io.sidews.linkerman.base.SymbolManager;
import io.sidews.linkerman.util.EMFUtil;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptPackage;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

public class PathSearcherOldTest {

    private static final Logger logger = LoggerFactory.getLogger(PathSearcherOldTest.class);

    private static PathSearcherOld pathSearcher;

    private static LinkerScriptContext ctx;

    private static LinkerScriptPackage pkg;

    @BeforeAll
    static void globalSetup() {
        ctx = new LinkerScriptContext();
        var grammarAnalyzer = new BasicGrammarAnalyzer(ctx);
        var symbolManager = new SymbolManager(grammarAnalyzer, ctx);
        pathSearcher = new PathSearcherOld(symbolManager);
        pkg = ctx.getLinkerScriptPackage();
    }

    @Test
    void testSearchPaths_Single() {
        var symbolType = ctx.getLinkerScriptPackage().getAssignment();
        var paths = pathSearcher.searchPaths(EMFUtil.getInstanceClass(symbolType));
        for (var leafNode : paths) {
            LinkNodeLogger.logPath(leafNode, symbolType);
        }
    }

    @Test
    void testSearchPaths_All() {
        for (var classifier : ctx.getLinkerScriptPackage().getEClassifiers()) {
            if (classifier instanceof EClass symbolType) {
                logger.info("Paths for {}", symbolType.getName());
                var paths = pathSearcher.searchPaths(EMFUtil.getInstanceClass(symbolType));
                for (var leafNode : paths) {
                    LinkNodeLogger.logPath(leafNode, symbolType, "\t\t");
                }
            }
        }
    }

    @Test
    void testSearchPathsBound_Assignment_LinkerScript() {
        testSearchPathsBound_From_To(pkg.getAssignment(), pkg.getLinkerScript());
    }

    @Test
    void testSearchPathsBound_LExpression_LinkerScript() {
        testSearchPathsBound_From_To(pkg.getLExpression(), pkg.getLinkerScript());
    }

    @Test
    void testSearchPathsBound_LExpression_OutputSection() {
        testSearchPathsBound_From_To(pkg.getLExpression(), pkg.getOutputSection());
    }

    @Test
    void testSearchPathsBound_LExpression_Assignment() {
        testSearchPathsBound_From_To(pkg.getLExpression(), pkg.getAssignment());
    }

    @Test
    void testSearchPathsBound_Assigment_OutputSection() {
        testSearchPathsBound_From_To(pkg.getAssignment(), pkg.getOutputSection());
    }

    private void testSearchPathsBound_From_To(EClass from, EClass to) {
        var paths = pathSearcher.searchPathsBound(from, to);
        logger.info("Paths from {} to {} ({})", from.getName(), to.getName(), paths.size());
        for (var path : paths) {
            logger.info("\t{}", pathToString(path));
        }
    }

    private String pathToString(Stack<EReference> path) {
        var logMessage = new StringBuilder(path.peek().getEReferenceType().getName());
        for (var edge : path.reversed()) {
            logMessage.append(String.format(" -[%s]-> ", edge.getName()));
            logMessage.append(edge.getEContainingClass().getName());
        }
        return logMessage.toString();
    }



}
