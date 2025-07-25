package io.sidews.linkerman;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DynamicModel<T extends EObject> {

    ModelDescriptor<T> getDescriptor();

    T getSymbol();

    void linkToParent(DynamicModel<? extends EObject> parent, NodeLinkStrategy linkStrategy);

    void linkToParentAtFirst(DynamicModel<? extends EObject> parent);

    void linkToParentAtFirst(DynamicModel<? extends EObject> parent, NodeLinkStrategy linkStrategy);

    void linkToParentAtLast(DynamicModel<? extends EObject> parent);

    void linkToParentAtLast(DynamicModel<? extends EObject> parent, NodeLinkStrategy linkStrategy);

    void linkToParentAtIndex(int index, DynamicModel<? extends EObject> parent);

    void linkToParentAtIndex(int index, DynamicModel<? extends EObject> parent, NodeLinkStrategy linkStrategy);

    @FunctionalInterface
    interface NodeLinkStrategy {
        Optional<EReference> selectContainingReference(Set<EReference> possibleReferences);
    }

    interface Factory<T extends EObject> {
        DynamicModel<T> createDefault();
        DynamicModel<T> createWith(T symbol);
    }

    interface Registry {
        <T extends EObject> Factory<T> getFactory(Class<T> symbolType);
        <T extends EObject> DynamicModel<T> createDefault(Class<T> symbolType);
        <T extends EObject> DynamicModel<T> createWith(Class<T> symbolType, T symbol);
    }
}
























