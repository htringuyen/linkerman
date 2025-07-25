package io.sidews.linkerman.base;

import io.sidews.linkerman.LinkerScriptContext;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript;
import org.eclipse.cdt.linkerscript.linkerScript.StatementInputSection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BasicSymbolConstructorTest {

    private static final Logger logger = LoggerFactory.getLogger(BasicSymbolConstructorTest.class);

    private static BasicSymbolConstructor constructor;

    @BeforeAll
    static void globalSetup() {
        var ctx = new LinkerScriptContext();
        var grammarAnalyzer = new BasicGrammarAnalyzer(
                ctx.getGrammar(), ctx.getGrammarConstraintProvider());
        var registry = new DefaultModelDescriptor.Registry(grammarAnalyzer, ctx.getEPackage());
        constructor = new BasicSymbolConstructor(registry, ctx.getEPackage(), grammarAnalyzer);
    }

    @Test
    void testConstructDefaultEObject_LinkerScript() {
        var linkerScript = constructor.constructDefaultSymbol(LinkerScript.class);
        assertEquals(1, linkerScript.getStatements().size());
    }

    @Test
    void testConstructDefaultEObject_StatementInputSection() {
        var statement = constructor.constructDefaultSymbol(StatementInputSection.class);
        assertNotNull(statement.getSpec());
    }
}
