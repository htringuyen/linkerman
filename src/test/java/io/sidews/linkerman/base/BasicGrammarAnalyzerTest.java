package io.sidews.linkerman.base;

import io.sidews.linkerman.LinkerScriptContext;
import io.sidews.linkerman.util.EMFUtil;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptPackage;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasicGrammarAnalyzerTest {

    private static final Logger logger = LoggerFactory.getLogger(BasicGrammarAnalyzerTest.class);

    private static BasicGrammarAnalyzer analyzer;

    private static LinkerScriptPackage pkg;

    @BeforeAll
    static void globalSetup() {
        var ctx = new LinkerScriptContext();
        analyzer = new BasicGrammarAnalyzer(ctx);
        pkg = ctx.getLinkerScriptPackage();
    }

    @Test
    void testGetSymbolReturnTypes_Counting() {
        assertEquals(1, countSymbolReturnTypes(pkg.getLinkerScript()));
        assertEquals(23, countSymbolReturnTypes(pkg.getLinkerScriptStatement()));
        assertEquals(5, countSymbolReturnTypes(pkg.getOutputSectionCommand()));
        assertEquals(1, countSymbolReturnTypes(pkg.getMemoryCommand()));
    }

    @Test
    void testGetSymbolReturnTypes_MemoryCommand() {
        var memoryCommand = pkg.getMemoryCommand();
        assertTrue(analyzer.getSymbolReturnTypes(memoryCommand).contains(memoryCommand));
    }

    @Test
    void testGetSymbolReturnTypes_DebugInfo() {
        for (var classifier : pkg.getEClassifiers()) {
            if (classifier instanceof EClass symbolType) {
                var returnTypes = analyzer.getSymbolReturnTypes(symbolType);
                logger.info("Return types for symbol {} ({}):", symbolType.getName(), returnTypes.size());
                returnTypes.forEach(type -> logger.info("--> {}", type.getName()));
            }
            else {
                warnOddClassifier(classifier);
            }
            printLineSeparator();
        }
    }

    private int countSymbolReturnTypes(EClass symbolType) {
        return analyzer.getSymbolReturnTypes(symbolType).size();
    }

    @Test
    void testGetAllSymbolContainers_DebugInfo() {
        for (var classifier : pkg.getEClassifiers()) {
            if (classifier instanceof EClass symbolType) {
                var refs = analyzer.getAllSymbolContainers(symbolType);
                logger.info("Container refs for symbol {} ({}):", symbolType.getName(), refs.size());
                refs.forEach(ref -> logger.info("--> name: {}, parent: {}, baseType: {}",
                        ref.getName(), ref.getEContainingClass().getName(), ref.getEReferenceType().getName()));
            }
            else {
                warnOddClassifier(classifier);
            }
            printLineSeparator();
        }
    }

    private void warnOddClassifier(EClassifier classifier) {
        logger.warn("Odd classifier (non-EClass): {}", classifier.getName());
    }

    private void printLineSeparator() {
        logger.info("-------------------------------------------");
    }

    @Test
    void testIsFeatureRequired_DebugInfo() {
        EMFUtil.getAllEClassIn(pkg).forEach(eClass -> {
            logger.info("[{}] features:", eClass.getName());
            eClass.getEAllAttributes().forEach(attr -> {
                logger.info("--> [ATTR] required: {} - name: {} - type: {}",
                        analyzer.isFeatureRequired(attr), attr.getName(), attr.getEAttributeType().getName());
            });
            eClass.getEAllReferences().forEach(ref -> {
                logger.info("--> [REFS] required: {} - name: {} - type: {}",
                        analyzer.isFeatureRequired(ref), ref.getName(), ref.getEReferenceType().getName());
            });
            logger.info("------------------------------------------");
        });
    }

    @Test
    void testGetSymbolContainer_InputSection_StatementInputSection() {
        var refs = analyzer.getSymbolContainers(pkg.getInputSection(), pkg.getStatementInputSection());
        assertEquals(1, refs.size());
    }

    @Test
    void testGetSymbolReturnTypes_EdgeCase() {
        var symbolType = pkg.getLExpression();
        var result = analyzer.getSymbolReturnTypes(symbolType);
        logger.info("Return types for {} ({}):", symbolType.getName(), result.size());
        for (var type : result) {
            logger.info("--> {}", type.getName());
        }
    }
}

