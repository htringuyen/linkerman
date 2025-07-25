package io.sidews.linkerman.util;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import java.util.List;

public class EMFUtil {

    public static <T extends EObject> EClass findEClassOf(Class<T> instanceType, EPackage ePackage) {
        return (EClass) ePackage.getEClassifier(instanceType.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends EObject> getInstanceClass(EClass eClass) {
        return (Class<? extends EObject>) eClass.getInstanceClass();
    }

    public static List<EClass> getAllEClassIn(EPackage ePackage) {
        return ePackage.getEClassifiers().stream()
                .filter(c -> c instanceof EClass)
                .map(EClass.class::cast)
                .toList();
    }
}
