package io.sidews.linkerman;

import org.eclipse.emf.ecore.EClass;

public interface SymbolResolver {

    EClass resolveReturnType(DSLSymbol symbol);

    EClass resolveParentType(DSLSymbol symbol);
}
