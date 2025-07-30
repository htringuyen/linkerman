package io.sidews.linkerman.internal;

import io.sidews.linkerman.DSLSymbol;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.List;

public final class DSLSymbolImpl implements DSLSymbol {

    private final boolean isASTRoot;

    private final EClass symbolType;

    private final List<EClass> possibleReturnTypes;

    private final List<EReference> possibleContainingReferences;

    public DSLSymbolImpl(boolean isASTRoot, EClass symbolType, List<EClass> possibleReturnTypes, List<EReference> possibleContainingReferences) {
        this.isASTRoot = isASTRoot;
        this.symbolType = symbolType;
        this.possibleReturnTypes = possibleReturnTypes;
        this.possibleContainingReferences = possibleContainingReferences;
    }

    @Override
    public boolean isASTRoot() {
        return isASTRoot;
    }

    @Override
    public EClass getESymbolType() {
        return symbolType;
    }

    @Override
    public List<EClass> getEPossibleReturnTypes() {
        return possibleReturnTypes;
    }

    @Override
    public List<EClass> getEPossibleParentTypes() {
        return possibleContainingReferences.stream()
                .map(EStructuralFeature::getEContainingClass).toList();
    }
    @Override
    public List<EReference> getEPossibleContainingReferences() {
        return possibleContainingReferences;
    }
}





















