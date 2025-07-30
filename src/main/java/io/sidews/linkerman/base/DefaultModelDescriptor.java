package io.sidews.linkerman.base;

import io.sidews.linkerman.DSLSymbol;
import io.sidews.linkerman.ModelDescriptor;
import io.sidews.linkerman.SymbolResolver;
import io.sidews.linkerman.util.EMFUtil;
import org.eclipse.emf.ecore.*;

import java.util.HashMap;
import java.util.Map;

public class DefaultModelDescriptor<T extends EObject> implements ModelDescriptor<T> {

    private final DSLSymbol symbol;

    private final SymbolResolver symbolResolver;

    private DefaultModelDescriptor(DSLSymbol symbol, SymbolResolver symbolResolver) {
        this.symbolResolver = symbolResolver;
        this.symbol = symbol;
    }

    @Override
    public Class<T> getSymbolType() {
        return EMFUtil.getInstanceClass(symbol.getESymbolType());
    }

    @Override
    public Class<? extends EObject> getParentType() {
        if (symbol.isASTRoot()) {
            return null;
        }
        return EMFUtil.getInstanceClass(symbolResolver.resolveParentType(symbol));
    }

    @Override
    public Class<? extends T> getReturnType() {
        return EMFUtil.getInstanceClass(symbolResolver.resolveReturnType(symbol));
    }

    @Override
    public DSLSymbol getSymbol() {
        return symbol;
    }

    @Override
    public SymbolResolver getSymbolResolver() {
        return symbolResolver;
    }

    public static class Registry implements ModelDescriptor.Registry {

        private final Map<Class<?>, Factory<?>> factoryMap = new HashMap<>();

        private final SymbolManager symbolManager;

        public Registry(SymbolManager symbolManager) {
            this.symbolManager = symbolManager;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends EObject> Factory<T> getFactory(Class<T> symbolType) {
            return (Factory<T>) factoryMap.computeIfAbsent(symbolType, s -> createDefaultFactory(symbolType));
        }

        @Override
        public <T extends EObject> ModelDescriptor<T> getOrCreate(Class<T> symbolType) {
            return getFactory(symbolType).getOrCreate();
        }

        @Override
        public <T extends EObject> void registerFactory(Class<T> symbolType, Factory<T> factory) {
            factoryMap.put(symbolType, factory);
        }

        private <T extends EObject> Factory<T> createDefaultFactory(Class<T> symbolType) {
            return () -> new DefaultModelDescriptor<>(
                    symbolManager.getSymbol(symbolType), SymbolResolvers.DEFAULT_SAFEST);
        }
    }
}