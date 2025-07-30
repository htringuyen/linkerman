package io.sidews.linkerman.base;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.List;
import java.util.Set;

public interface GrammarAnalyzer {

    List<EReference> getAllSymbolContainers(EClass eClass);

    List<EClass> getSymbolReturnTypes(EClass eClass);

    List<EReference> getSymbolContainers(EClass symbolEClass, EClass parentEClass);

    boolean isASTRoot(EClass eClass);

    boolean isFeatureRequired(EStructuralFeature feature);
}
