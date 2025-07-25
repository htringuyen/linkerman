package io.sidews.linkerman.base;

import io.sidews.linkerman.XtextContext;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript;
import org.eclipse.cdt.linkerscript.linkerScript.StatementInputSection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BasicEObjectConstructorTest {

    private static final Logger logger = LoggerFactory.getLogger(BasicEObjectConstructorTest.class);

    private static BasicEObjectConstructor constructor;

    @BeforeAll
    static void globalSetup() {
        var grammarAnalyzer = new BasicGrammarAnalyzer(
                XtextContext.getGrammar(), XtextContext.getGrammarConstraintProvider());
        var registry = new DefaultModelDescriptor.Registry(grammarAnalyzer, XtextContext.getEPackage());
        constructor = new BasicEObjectConstructor(registry, XtextContext.getEPackage(), grammarAnalyzer);
    }

    @Test
    void testConstructDefaultEObject_LinkerScript() {
        var linkerScript = constructor.constructDefaultEObject(LinkerScript.class);
        assertEquals(1, linkerScript.getStatements().size());
    }

    @Test
    void testConstructDefaultEObject_StatementInputSection() {
        var statement = constructor.constructDefaultEObject(StatementInputSection.class);
        assertNotNull(statement.getSpec());
    }

}
