package io.sidews.linkerman.internal;

import io.sidews.linkerman.DynamicModel;
import io.sidews.linkerman.LinkerScriptContext;
import io.sidews.linkerman.ModelDescriptor;
import io.sidews.linkerman.Serializer;
import io.sidews.linkerman.base.BasicGrammarAnalyzer;
import io.sidews.linkerman.base.BasicSymbolConstructor;
import io.sidews.linkerman.base.DefaultDynamicModel;
import io.sidews.linkerman.base.DefaultModelDescriptor;
import org.eclipse.cdt.linkerscript.linkerScript.InputSection;
import org.eclipse.cdt.linkerscript.linkerScript.LExpression;
import org.eclipse.cdt.linkerscript.linkerScript.StatementAssignment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CompilerImplTest {

    private static final Logger logger = LoggerFactory.getLogger(CompilerImplTest.class);

    private static final LinkerScriptContext ctx = new LinkerScriptContext();

    private static ModelDescriptor.Registry descriptorRegistry;

    private static DynamicModel.Registry modelRegistry;

    private static CompilerImpl compiler;

    private static Serializer serializer;

    @BeforeAll
    static void globalSetup() {
        var grammarAnalyzer = new BasicGrammarAnalyzer(ctx.getGrammar(),
                ctx.getGrammarConstraintProvider());
        descriptorRegistry = new DefaultModelDescriptor.Registry(grammarAnalyzer, ctx.getEPackage());
        var symbolConstructor = new BasicSymbolConstructor(descriptorRegistry, ctx.getEPackage(), grammarAnalyzer);
        modelRegistry = new DefaultDynamicModel.Registry(descriptorRegistry, grammarAnalyzer, symbolConstructor);
        serializer = new SerializerImpl(ctx);
        compiler = new CompilerImpl(descriptorRegistry, modelRegistry, serializer, ctx);
    }

    @Test
    void testBuildDefaultScriptEncompass_InputSection() {
        var model = modelRegistry.createDefault(InputSection.class);
        var returnType = model.getDescriptor().getReturnType();
        var script = compiler.buildDefaultScriptEncompass(model);
        logger.info("\n{}", script);
    }

    @Test
    void testBuildDefaultScriptEncompass_StatementAssignment() {
        var model = modelRegistry.createDefault(StatementAssignment.class);
        logger.info("Build default script for {}", model.getDescriptor().getSymbolType());
        var script = compiler.buildDefaultScriptEncompass(model);
        logger.info("\n{}", script);
    }

    @Test
    void testCompile_InputSection() {
        var snippet = """
                KEEP(*(.text.Excep_*))
                """;

        var model = compiler.compile(snippet, descriptorRegistry.getOrCreate(InputSection.class))
                .single();

        logModelXmi(model);

        assertNotNull(model);
    }

    @Test
    void testCompile_Expression() {
        var model = compiler.compile("hello", descriptorRegistry.getOrCreate(LExpression.class))
                .single();

        logModelXmi(model);
        assertNotNull(model);
    }


    private void logModelXmi(DynamicModel<?> model) {
        logger.info("model in xmi:\n{}", serializer.serializeToXMI(model));
    }

}




























