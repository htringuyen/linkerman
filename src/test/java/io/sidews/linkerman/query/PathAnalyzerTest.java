package io.sidews.linkerman.query;

import io.sidews.linkerman.LinkerScriptContext;
import io.sidews.linkerman.base.BasicGrammarAnalyzer;
import io.sidews.linkerman.base.SymbolManager;
import org.eclipse.emf.ecore.EReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

public class PathAnalyzerTest {

    private static final Logger logger = LoggerFactory.getLogger(PathAnalyzerTest.class);

    private PathAnalyzer pathAnalyzer;

    @BeforeEach
    void setup() {
        var context = new LinkerScriptContext();
        var grammarAnalyzer = new BasicGrammarAnalyzer(context);
        var symbolManager = new SymbolManager(grammarAnalyzer, context);
        var pathSearcher = new PathSearcherImpl(context, symbolManager);
        pathAnalyzer = new PathAnalyzer(context, pathSearcher);
    }

    @Test
    void testFindPossiblePaths_Sample1() {
        doTestFindPossiblePaths("/LinkerScript//OutputSection//Assignment[position()=1]");
    }

    @Test
    void testFindPossiblePaths_Sample2() {
        doTestFindPossiblePaths("/LinkerScript//OutputSection//LExpression");
    }

    @Test
    void testFindPossiblePaths_Sample3() {
        doTestFindPossiblePaths("/LinkerScript//OutputSection//Assignment//LExpression[textMatch('some_pattern')]");
    }

    @Test
    void testFindPossiblePaths_Sample4() {
        doTestFindPossiblePaths("/LinkerScript//OutputSection");
    }

    private void doTestFindPossiblePaths(String query) {
        var paths = pathAnalyzer.findPossiblePaths(query);
        logger.info("Possible paths for: {} ({})", query, paths.size());
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
