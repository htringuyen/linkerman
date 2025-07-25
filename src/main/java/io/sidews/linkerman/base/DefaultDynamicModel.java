package io.sidews.linkerman.base;

import io.sidews.linkerman.DynamicModel;
import io.sidews.linkerman.ModelDescriptor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultDynamicModel<T extends EObject> implements DynamicModel<T> {

    private final ModelDescriptor<T> descriptor;

    private final T source;

    private final NodeLinkStrategy defaultLinkStrategy;

    private final GrammarAnalyzer grammarAnalyzer;

    public DefaultDynamicModel(T source, ModelDescriptor<T> descriptor,
                               GrammarAnalyzer grammarAnalyzer, NodeLinkStrategy defaultLinkStrategy) {
        this.source = source;
        this.descriptor = descriptor;
        this.grammarAnalyzer = grammarAnalyzer;
        this.defaultLinkStrategy = defaultLinkStrategy;
    }

    @Override
    public T getSymbol() {
        return source;
    }

    @Override
    public ModelDescriptor<T> getDescriptor() {
        return descriptor;
    }

    @Override
    public void linkToParent(DynamicModel<? extends EObject> parent, NodeLinkStrategy resolver) {
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public void linkToParentAtFirst(DynamicModel<? extends EObject> parent, NodeLinkStrategy linkStrategy) {
        resolveParentContainer(parent, linkStrategy).addFirst(getSymbol());
    }

    @Override
    public void linkToParentAtFirst(DynamicModel<? extends EObject> parent) {
        linkToParentAtFirst(parent, defaultLinkStrategy);
    }

    @Override
    public void linkToParentAtLast(DynamicModel<? extends EObject> parent, NodeLinkStrategy resolver) {
        resolveParentContainer(parent, resolver).addLast(getSymbol());
    }

    @Override
    public void linkToParentAtLast(DynamicModel<? extends EObject> parent) {
        linkToParentAtLast(parent, defaultLinkStrategy);
    }

    @Override
    public void linkToParentAtIndex(int index, DynamicModel<? extends EObject> parent, NodeLinkStrategy resolver) {
        resolveParentContainer(parent, resolver).add(index, getSymbol());
    }

    @Override
    public void linkToParentAtIndex(int index, DynamicModel<? extends EObject> parent) {
        linkToParentAtIndex(index, parent, defaultLinkStrategy);
    }

    @SuppressWarnings("unchecked")
    private EList<T> resolveParentContainer(DynamicModel<? extends EObject> parent, NodeLinkStrategy linkStrategy) {
        var reference = linkStrategy.selectContainingReference(
                grammarAnalyzer.getSymbolContainers(descriptor.getSymbolEClass(), parent.getDescriptor().getSymbolEClass()));
        if (reference.isEmpty()) {
            throw new DynamicProcessingException(String.format(
                    "Failed to find link between %s and %s using %s",
                    parent.getSymbol().eClass(), this.getSymbol().eClass(), linkStrategy.getClass()));
        }
        return (EList<T>) parent.getSymbol().eGet(reference.get());
    }

    public static abstract class Registry implements DynamicModel.Registry {

        private static final NodeLinkStrategy GET_FIRST_STRATEGY = new DefaultNodeLinkStrategy();

        private final ModelDescriptor.Registry descriptorRegistry;

        private final GrammarAnalyzer grammarAnalyzer;

        private final HashMap<Class<?>, Factory<?>> factoryMap = new HashMap<>();

        private final AtomicLong autoFilledStringCounter = new AtomicLong(0);

        public Registry(ModelDescriptor.Registry descriptorRegistry, GrammarAnalyzer grammarAnalyzer) {
            this.descriptorRegistry = descriptorRegistry;
            this.grammarAnalyzer = grammarAnalyzer;
        }


        private class DefaultFactory implements Factory<EObject> {

            private final Class<? extends EObject> symbolType;

            public DefaultFactory(Class<? extends EObject> symbolType) {
                this.symbolType = symbolType;
            }

            @Override
            public DynamicModel<EObject> createDefault() {
                var descriptor = descriptorRegistry.getOrCreate(symbolType);
                descriptor.getParentType();
                return null;
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
