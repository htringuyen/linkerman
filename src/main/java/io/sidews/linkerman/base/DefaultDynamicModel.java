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

    private final T symbol;

    private final NodeLinkStrategy defaultLinkStrategy;

    private final GrammarAnalyzer grammarAnalyzer;

    public DefaultDynamicModel(T symbol, ModelDescriptor<T> descriptor,
                               GrammarAnalyzer grammarAnalyzer, NodeLinkStrategy defaultLinkStrategy) {
        this.symbol = symbol;
        this.descriptor = descriptor;
        this.grammarAnalyzer = grammarAnalyzer;
        this.defaultLinkStrategy = defaultLinkStrategy;
    }

    @Override
    public T getSymbol() {
        return symbol;
    }

    @Override
    public ModelDescriptor<T> getDescriptor() {
        return descriptor;
    }

    @Override
    public void linkToParent(DynamicModel<? extends EObject> parent, NodeLinkStrategy linkStrategy) {
        var containingRef = inferContainingReference(parent, linkStrategy);
        if (containingRef.isMany()) {
            getContainingList(parent.getSymbol(), containingRef, linkStrategy).addLast(this.getSymbol());
        }
        else {
            if (!linkStrategy.shouldOverrideSingular() && parent.getSymbol().eGet(containingRef) != null) {
                throw new DynamicProcessingException("Container is not available for linking");
            }
            parent.getSymbol().eSet(containingRef, this.getSymbol());
        }
    }

    @Override
    public void linkToParent(DynamicModel<? extends EObject> parent) {
        linkToParent(parent, defaultLinkStrategy);
    }

    @Override
    public void linkToParentAtFirst(DynamicModel<? extends EObject> parent, NodeLinkStrategy linkStrategy) {
        getContainingList(parent.getSymbol(), inferContainingReference(parent, linkStrategy), linkStrategy)
                .addFirst(this.getSymbol());
    }

    @Override
    public void linkToParentAtFirst(DynamicModel<? extends EObject> parent) {
        linkToParentAtFirst(parent, defaultLinkStrategy);
    }

    @Override
    public void linkToParentAtLast(DynamicModel<? extends EObject> parent, NodeLinkStrategy linkStrategy) {
        getContainingList(parent.getSymbol(), inferContainingReference(parent, linkStrategy), linkStrategy)
                .addLast(this.getSymbol());
    }

    @Override
    public void linkToParentAtLast(DynamicModel<? extends EObject> parent) {
        linkToParentAtLast(parent, defaultLinkStrategy);
    }

    @Override
    public void linkToParentAtIndex(int index, DynamicModel<? extends EObject> parent, NodeLinkStrategy linkStrategy) {
        getContainingList(parent.getSymbol(), inferContainingReference(parent, linkStrategy), linkStrategy)
                .add(index, this.getSymbol());
    }

    @Override
    public void linkToParentAtIndex(int index, DynamicModel<? extends EObject> parent) {
        linkToParentAtIndex(index, parent, defaultLinkStrategy);
    }

    private EReference inferContainingReference(DynamicModel<? extends EObject> parent, NodeLinkStrategy linkStrategy) {
        var reference = linkStrategy.selectContainingReference(
                grammarAnalyzer.getSymbolContainers(descriptor.getSymbolEClass(), parent.getDescriptor().getSymbolEClass()));
        if (reference.isEmpty()) {
            throw new DynamicProcessingException(String.format(
                    "Failed to find link between %s and %s using %s",
                    parent.getSymbol().eClass(), this.getSymbol().eClass(), linkStrategy.getClass()));
        }
        return reference.get();
    }

    @SuppressWarnings("unchecked")
    private EList<EObject> getContainingList(EObject container, EReference reference, NodeLinkStrategy strategy) {
        if (!reference.isMany()) {
            throw new DynamicProcessingException("Not multi reference container");
        }
        var list = (EList<EObject>) container.eGet(reference);
        if (list == null) {
            throw new DynamicProcessingException("Containing list is null");
        }
        if (strategy.shouldClearCollection()) {
            list.clear();
        }
        return list;
    }

    public static class Registry implements DynamicModel.Registry {

        private static final NodeLinkStrategy GET_FIRST_STRATEGY = new DefaultNodeLinkStrategy();

        private final ModelDescriptor.Registry descriptorRegistry;

        private final GrammarAnalyzer grammarAnalyzer;

        private final HashMap<Class<?>, Factory<?>> factoryMap = new HashMap<>();

        private final SymbolConstructor symbolConstructor;

        public Registry(ModelDescriptor.Registry descriptorRegistry,
                        GrammarAnalyzer grammarAnalyzer,
                        SymbolConstructor symbolConstructor) {
            this.descriptorRegistry = descriptorRegistry;
            this.grammarAnalyzer = grammarAnalyzer;
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
                        (ModelDescriptor<EObject>) descriptorRegistry.getOrCreate(symbolType),
                        grammarAnalyzer, GET_FIRST_STRATEGY);
            }
        }

        private static class DefaultNodeLinkStrategy implements NodeLinkStrategy {
            @Override
            public Optional<EReference> selectContainingReference(Set<EReference> possibleReferences) {
                if (possibleReferences.isEmpty()) {
                    return Optional.empty();
                }
                return possibleReferences.stream().findFirst();
            }
        }
    }
}
