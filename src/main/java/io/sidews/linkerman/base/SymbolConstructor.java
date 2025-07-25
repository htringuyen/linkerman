package io.sidews.linkerman.base;

import org.eclipse.emf.ecore.EObject;

public interface SymbolConstructor {
    <T extends EObject> T constructDefaultSymbol(Class<T> symbolType);
}
