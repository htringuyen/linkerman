package io.sidews.linkerman.util;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

public class EMFUtil {

    public static <T extends EObject> EClass findEClassOf(Class<T> instanceType, EPackage ePackage) {
        return (EClass) ePackage.getEClassifier(instanceType.getSimpleName());
    }
}
