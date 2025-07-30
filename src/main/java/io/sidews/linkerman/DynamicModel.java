package io.sidews.linkerman;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import java.util.List;

public interface DynamicModel<T extends EObject> {

    ModelDescriptor<T> getDescriptor();

    T getSymbolInstance();

    void linkToParent(DynamicModel<? extends EObject> parent, LinkStrategy linkStrategy);

    void linkToParentAtFirst(DynamicModel<? extends EObject> parent, LinkStrategy linkStrategy);

    void linkToParentAtLast(DynamicModel<? extends EObject> parent, LinkStrategy linkStrategy);

    void linkToParentAtIndex(int index, DynamicModel<? extends EObject> parent, LinkStrategy linkStrategy);

    @FunctionalInterface
    interface LinkStrategy {

        EReference determineContainingReference(List<EReference> possibleReferences);

        default boolean shouldOverrideSingleFeatures() {
            return false;
        }

        default boolean shouldClearCollectionFeatures() {
            return false;
        }
    }

    interface Factory<T extends EObject> {
        DynamicModel<T> createDefault();
        DynamicModel<T> createWith(T symbol);
    }

    interface Registry {
        <T extends EObject> Factory<T> getFactory(Class<T> symbolType);
        <T extends EObject> DynamicModel<T> createDefault(Class<T> symbolType);
        <T extends EObject> DynamicModel<T> createWith(T symbol);
    }
}


