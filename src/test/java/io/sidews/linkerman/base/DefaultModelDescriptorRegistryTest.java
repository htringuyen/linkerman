package io.sidews.linkerman.base;

import io.sidews.linkerman.ModelDescriptor;
import io.sidews.linkerman.XtextContext;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultModelDescriptorRegistryTest {

    private static final Logger logger = LoggerFactory.getLogger(DefaultModelDescriptorRegistryTest.class);

    private ModelDescriptor.Registry registry;

    @BeforeEach
    void setup() {
        registry = new DefaultModelDescriptor.Registry(new BasicGrammarAnalyzer(XtextContext.getGrammar(), XtextContext.getGrammarConstraintProvider()),
                XtextContext.getEPackage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetOrCreateDefaultDescriptors_DebugInfo() {
        XtextContext.getEAllClasses().forEach(symbolEClass -> {
            logger.info("{}:", symbolEClass.getName());
            var descriptor = registry.getOrCreate((Class<? extends EObject>) symbolEClass.getInstanceClass());
            logger.info("--> return={}, parent={}, symbol={}",
                    descriptor.getReturnType().getSimpleName(),
                    descriptor.getParentType() == null ? "NULL" : descriptor.getParentType().getSimpleName(),
                    descriptor.getSymbolType().getSimpleName());
        });
    }
}
