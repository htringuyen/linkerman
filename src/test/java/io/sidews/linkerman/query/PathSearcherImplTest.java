package io.sidews.linkerman.query;

import io.sidews.linkerman.LinkerScriptContext;
import io.sidews.linkerman.base.LinkermanService;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptPackage;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

public class PathSearcherImplTest {

    private static final Logger logger = LoggerFactory.getLogger(PathSearcherImplTest.class);

    private PathSearcher searcher;

    private LinkerScriptPackage pkg;

    @BeforeEach
    void setup() {
        var context = new LinkerScriptContext();
        var symbolManager = LinkermanService.getSymbolManager(context);
        pkg = context.getLinkerScriptPackage();
        searcher = new PathSearcherImpl(context, symbolManager);
    }

    @Test
    void testFindPaths_Assignment_LinkerScript() {
        doFindTestPaths(pkg.getAssignment(), pkg.getLinkerScript());
    }

    @Test
    void testFindPaths_LExpression_LinkerScript() {
        doFindTestPaths(pkg.getLExpression(), pkg.getLinkerScript());
    }

    private void doFindTestPaths(EClass fromPoint, EClass toPoint) {
        var paths = searcher.findPaths(fromPoint, toPoint);
        logger.info("Paths from {} to {} ({})",
                fromPoint.getName(), toPoint.getName(), paths.size());
        for (var path : paths) {
            logger.info("  {}", pathToString(path));
        }
    }

    private String pathToString(LinkedList<EReference> path) {
        var result = new StringBuilder();
        result.append(path.getFirst().getEReferenceType().getName());
        for (var segment : path) {
            result.append(String.format(" -[%s]-> ", segment.getName()));
            result.append(segment.getEContainingClass().getName());
        }
        return result.toString();
    }
}
