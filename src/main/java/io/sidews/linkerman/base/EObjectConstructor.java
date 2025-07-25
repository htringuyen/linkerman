package io.sidews.linkerman.base;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

public interface EObjectConstructor {

    <T extends EObject> T constructDefaultEObject(Class<T> instanceType);
}
