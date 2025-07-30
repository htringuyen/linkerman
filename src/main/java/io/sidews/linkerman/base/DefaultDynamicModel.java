package io.sidews.linkerman.base;

import io.sidews.linkerman.DynamicModel;
import io.sidews.linkerman.ModelDescriptor;
import io.sidews.linkerman.util.EMFUtil;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import java.util.*;

public class DefaultDynamicModel<T extends EObject> implements DynamicModel<T> {

    private final ModelDescriptor<T> descriptor;

    private final T symbolInstance;

    public DefaultDynamicModel(T symbolInstance, ModelDescriptor<T> descriptor) {
        this.symbolInstance = symbolInstance;
        this.descriptor = descriptor;
    }

    @Override
    public T getSymbolInstance() {
        return symbolInstance;
    }

    @Override
    public ModelDescriptor<T> getDescriptor() {
        return descriptor;
    }

    @Override
    public void linkToParent(DynamicModel<? extends EObject> parent, LinkStrategy linkStrategy) {
        var containingRef = findContainingReference(parent, linkStrategy);
        if (containingRef.isMany()) {
            getContainingList(parent.getSymbolInstance(), containingRef, linkStrategy).addLast(this.getSymbolInstance());
        }
        else {
            if (!linkStrategy.shouldOverrideSingleFeatures() && parent.getSymbolInstance().eGet(containingRef) != null) {
                throw new DynamicProcessingException("Container is not available for linking");
            }
            parent.getSymbolInstance().eSet(containingRef, this.getSymbolInstance());
        }
    }

    @Override
    public void linkToParentAtFirst(DynamicModel<? extends EObject> parent, LinkStrategy linkStrategy) {
        getContainingList(parent.getSymbolInstance(), findContainingReference(parent, linkStrategy), linkStrategy)
                .addFirst(this.getSymbolInstance());
    }

    @Override
    public void linkToParentAtLast(DynamicModel<? extends EObject> parent, LinkStrategy linkStrategy) {
        getContainingList(parent.getSymbolInstance(), findContainingReference(parent, linkStrategy), linkStrategy)
                .addLast(this.getSymbolInstance());
    }

    @Override
    public void linkToParentAtIndex(int index, DynamicModel<? extends EObject> parent, LinkStrategy linkStrategy) {
        getContainingList(parent.getSymbolInstance(), findContainingReference(parent, linkStrategy), linkStrategy)
                .add(index, this.getSymbolInstance());
    }

    private EReference findContainingReference(DynamicModel<? extends EObject> parent, LinkStrategy linkStrategy) {
        var symbol = getDescriptor().getSymbol();
        var reference = linkStrategy.determineContainingReference(symbol.getEPossibleContainingReferences());
        if (reference == null) {
            throw new DynamicProcessingException(String.format(
                    "Failed to find link between %s and %s using %s",
                    parent.getSymbolInstance().eClass(), this.getSymbolInstance().eClass(), linkStrategy.getClass()));
        }
        return reference;
    }

    @SuppressWarnings("unchecked")
    private EList<EObject> getContainingList(EObject container, EReference reference, LinkStrategy strategy) {
        if (!reference.isMany()) {
            throw new DynamicProcessingException("Not multi reference container");
        }
        var list = (EList<EObject>) container.eGet(reference);
        if (list == null) {
            throw new DynamicProcessingException("Containing list is null");
        }
        if (strategy.shouldClearCollectionFeatures()) {
            list.clear();
        }
        return list;
    }

    public static class Registry implements DynamicModel.Registry {

        private final ModelDescriptor.Registry descriptorRegistry;

        private final HashMap<Class<?>, Factory<?>> factoryMap = new HashMap<>();

        private final SymbolConstructor symbolConstructor;

        public Registry(ModelDescriptor.Registry descriptorRegistry,
                        SymbolConstructor symbolConstructor) {
            this.descriptorRegistry = descriptorRegistry;
            this.symbolConstructor = symbolConstructor;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends EObject> Factory<T> getFactory(Class<T> symbolType) {
            return (Factory<T>) factoryMap.computeIfAbsent(symbolType, s -> new DefaultFactory(symbolType));
        }

        @Override
        public <T extends EObject> DynamicModel<T> createDefault(Class<T> symbolType) {
            return getFactory(symbolType).createDefault();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends EObject> DynamicModel<T> createWith(T symbol) {
            return getFactory((Class<T>) EMFUtil.getInstanceClass(symbol.eClass())).createWith(symbol);
        }

        private class DefaultFactory implements Factory<EObject> {

            private final Class<? extends EObject> symbolType;

            public DefaultFactory(Class<? extends EObject> symbolType) {
                this.symbolType = symbolType;
            }

            @Override
            public DynamicModel<EObject> createDefault() {
                return createWith(symbolConstructor.constructDefaultSymbol(symbolType));
            }

            @SuppressWarnings("unchecked")
            @Override
            public DynamicModel<EObject> createWith(EObject symbol) {
                return new DefaultDynamicModel<>(symbol,
                        (ModelDescriptor<EObject>) descriptorRegistry.getOrCreate(symbolType));
            }
        }
    }
}
