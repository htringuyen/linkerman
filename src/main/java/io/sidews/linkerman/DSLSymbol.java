package io.sidews.linkerman;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import java.util.List;

public interface DSLSymbol {

    boolean isASTRoot();

    EClass getESymbolType();

    List<EClass> getEPossibleReturnTypes();

    List<EClass> getEPossibleParentTypes();

    List<EReference> getEPossibleContainingReferences();
}
