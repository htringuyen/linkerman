package io.sidews.linkerman.base;

import io.sidews.linkerman.LinkerScriptContext;
import io.sidews.linkerman.ModelDescriptor;
import io.sidews.linkerman.util.EMFUtil;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultModelDescriptorRegistryTest {

    private static final Logger logger = LoggerFactory.getLogger(DefaultModelDescriptorRegistryTest.class);

    private ModelDescriptor.Registry registry;

    private static final LinkerScriptContext ctx = new LinkerScriptContext();

    @BeforeEach
    void setup() {
        registry = new DefaultModelDescriptor.Registry(new BasicGrammarAnalyzer(ctx.getGrammar(), ctx.getGrammarConstraintProvider()),
                ctx.getEPackage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetOrCreateDefaultDescriptors_DebugInfo() {
        EMFUtil.getAllEClassIn(ctx.getEPackage()).forEach(symbolEClass -> {
            logger.info("{}:", symbolEClass.getName());
            var descriptor = registry.getOrCreate((Class<? extends EObject>) symbolEClass.getInstanceClass());
            logger.info("--> return={}, parent={}, symbol={}",
                    descriptor.getReturnType().getSimpleName(),
                    descriptor.getParentType() == null ? "NULL" : descriptor.getParentType().getSimpleName(),
                    descriptor.getSymbolType().getSimpleName());
        });
    }
}
