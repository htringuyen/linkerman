package io.sidews.linkerman.base;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.Set;

public interface GrammarAnalyzer {

    Set<EReference> getAllSymbolContainers(EClass eClass);

    Set<EClass> getSymbolReturnTypes(EClass eClass);

    Set<EReference> getSymbolContainers(EClass symbolEClass, EClass parentEClass);

    boolean isASTRoot(EClass eClass);

    boolean isFeatureRequired(EStructuralFeature feature);
}
