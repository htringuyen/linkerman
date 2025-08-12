package io.sidews.linkerman.query;

import io.sidews.linkerman.LinkerScriptContext;
import io.sidews.linkerman.base.BasicGrammarAnalyzer;
import io.sidews.linkerman.base.SymbolManager;
import io.sidews.linkerman.internal.DSLLoader;
import org.eclipse.cdt.linkerscript.linkerScript.Assignment;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryEngineTest {

    private QueryEngine queryEngine;

    private DSLLoader loader;

    @BeforeEach
    void setup() {
        var context = new LinkerScriptContext();
        var grammarAnalyzer = new BasicGrammarAnalyzer(context);
        var symbolManager = new SymbolManager(grammarAnalyzer, context);
        var pathSearcher = new PathSearcherImpl(context, symbolManager);
        var pathAnalyzer = new PathAnalyzer(context, pathSearcher);
        queryEngine = new QueryEngine(pathAnalyzer);
        loader = new DSLLoader(context);
    }

    @Test
    void testQuery_Assignment_In_OutputSection() {
        var content = """
                SECTIONS
                {
                    .text :
                    {
                        test_var = . + 0x10;
                    }
                }
                """;
        var query = "/LinkerScript//OutputSection//Assignment";
        var result = doQuerySingle(content, query, Assignment.class);
        assertEquals("test_var", result.getName());
    }

    @Test
    void testQuery_Assigment_In_Sections_Block() {
        var content = """
                SECTIONS
                {   test_var1 = . + 0x10;
                    .text :
                    {
                        test_var2 = . + 0x10;
                    }
                }
                """;
        var query = "/LinkerScript//SectionsCommand/StatementAssignment/Assignment";
        var result = doQuerySingle(content, query, Assignment.class);
        assertEquals("test_var1", result.getName());
    }

    private <T> T doQuerySingle(String content, String query, Class<T> instanceType) {
        var loadResult = loader.loadDSL(content);
        assertTrue(loadResult.getIssues().isEmpty());
        var root = loadResult.getRoot();
        var result = queryEngine.executeSinglePath(query, root);
        assertEquals(1, result.size());
        return instanceType.cast(result.getFirst());
    }


}
