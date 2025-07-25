package io.sidews.linkerman;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

public interface ModelDescriptor<T extends EObject> {

    boolean isASTRoot();

    Class<T> getSymbolType();

    EClass getSymbolEClass();

    Class<? extends T> getReturnType();

    Class<? extends EObject> getParentType();

    @FunctionalInterface
    interface Factory<T extends EObject> {
        ModelDescriptor<T> getOrCreate();
    }

    interface Registry {
        <T extends EObject> Factory<T> getFactory(Class<T> symbolType);
        <T extends EObject> ModelDescriptor<T> getOrCreate(Class<T> symbolType);
        <T extends EObject> void registerFactory(Class<T> symbolType, Factory<T> factory);
    }
}