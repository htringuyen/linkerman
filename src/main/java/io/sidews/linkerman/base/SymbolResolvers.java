package io.sidews.linkerman.base;

import io.sidews.linkerman.DSLSymbol;
import io.sidews.linkerman.SymbolResolver;
import org.eclipse.emf.ecore.EClass;

public class SymbolResolvers {

    public static final SymbolResolver DEFAULT_SAFEST = new SymbolResolver() {
        @Override
        public EClass resolveReturnType(DSLSymbol symbol) {
            var result = symbol.getEPossibleReturnTypes()
                    .stream()
                    .filter(c -> c.getEAllReferences().isEmpty())
                    .findFirst();
            return result.orElse(symbol.getEPossibleReturnTypes().getFirst());
        }

        @Override
        public EClass resolveParentType(DSLSymbol symbol) {
            return symbol.getEPossibleParentTypes().getFirst();
        }
    };
}
