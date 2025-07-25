package io.sidews.linkerman.base;

import io.sidews.linkerman.ModelDescriptor;
import io.sidews.linkerman.util.EMFUtil;
import org.eclipse.emf.ecore.*;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultModelDescriptor<T extends EObject> implements ModelDescriptor<T> {

    public interface InferenceStrategy {

        Optional<Boolean> getIsASTRoot();

        Optional<EClass> postSelectParentType(@Nonnull Set<EClass> parentTypes);

        Optional<EClass> postSelectReturnType(@Nonnull Set<EClass> returnTypes);
    }


    private final Class<T> symbolType;

    private final EClass symbolEClass;

    private final GrammarAnalyzer analyzer;

    private Class<? extends EObject> parentType;

    private Class<? extends T> returnType;

    private final InferenceStrategy inferenceStrategy;

    private Boolean isASTRoot;

    public DefaultModelDescriptor(Class<T> symbolType, GrammarAnalyzer analyzer,
                                  EPackage ePackage, InferenceStrategy inferenceStrategy) {
        this.symbolType = symbolType;
        this.symbolEClass = EMFUtil.findEClassOf(symbolType, ePackage);
        this.analyzer = analyzer;
        this.inferenceStrategy = inferenceStrategy;
    }

    @Override
    public boolean isASTRoot() {
        if (isASTRoot == null) {
            isASTRoot = inferIsASTRoot();
        }
        return isASTRoot;
    }

    @Override
    public Class<T> getSymbolType() {
        return symbolType;
    }

    @Override
    public EClass getSymbolEClass() {
        return symbolEClass;
    }

    @Override
    public Class<? extends EObject> getParentType() {
        if (parentType == null) {
            parentType = inferParentType();
        }
        return parentType;
    }

    @Override
    public Class<? extends T> getReturnType() {
        if (returnType == null) {
            returnType = inferReturnType();
        }
        return returnType;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends EObject> inferParentType() {
        var possibleParents = analyzer.getAllSymbolContainers(symbolEClass)
                .stream()
                .map(EReference::getEContainingClass)
                .collect(Collectors.toSet());
        var parent = inferenceStrategy.postSelectParentType(possibleParents);
        if (parent.isEmpty() && !isASTRoot()) {
            throw new DynamicProcessingException(
                    "Cannot infer parent type for symbol: " + symbolType);
        }
        else if (parent.isEmpty()) {
            return null;
        }
        return (Class<? extends EObject>) parent.get().getInstanceClass();
    }

    @SuppressWarnings("unchecked")
    private Class<? extends T> inferReturnType() {
        var possibleReturnTypes = analyzer.getSymbolReturnTypes(symbolEClass);
        var returnType = inferenceStrategy.postSelectReturnType(possibleReturnTypes);
        return (Class<? extends T>) returnType.orElseThrow(() -> new DynamicProcessingException(
                "Cannot infer return type for symbol: " + symbolType)).getInstanceClass();
    }

    private boolean inferIsASTRoot() {
        var strategyOption = inferenceStrategy.getIsASTRoot();
        return strategyOption.orElseGet(() -> analyzer.isASTRoot(symbolEClass));
    }

    public static class Registry implements ModelDescriptor.Registry {

        private static final InferenceStrategy GET_FIRST = new GetFirstInferenceStrategy();

        private final Map<Class<?>, Factory<?>> factoryMap = new HashMap<>();

        private final GrammarAnalyzer grammarAnalyzer;

        private final EPackage ePackage;

        private final InferenceStrategy inferenceStrategy;

        public Registry(GrammarAnalyzer grammarAnalyzer, EPackage ePackage, InferenceStrategy inferenceStrategy) {
            this.grammarAnalyzer = grammarAnalyzer;
            this.ePackage = ePackage;
            this.inferenceStrategy = inferenceStrategy;
        }

        public Registry(GrammarAnalyzer grammarAnalyzer, EPackage ePackage) {
            this(grammarAnalyzer, ePackage, GET_FIRST);
        }


        @SuppressWarnings("unchecked")
        @Override
        public <T extends EObject> Factory<T> getFactory(Class<T> symbolType) {
            return (Factory<T>) factoryMap.computeIfAbsent(symbolType, s -> createDefaultFactory(symbolType));
        }

        @Override
        public <T extends EObject> ModelDescriptor<T> getOrCreate(Class<T> symbolType) {
            return getFactory(symbolType).getOrCreate();
        }

        @Override
        public <T extends EObject> void registerFactory(Class<T> symbolType, Factory<T> factory) {
            factoryMap.put(symbolType, factory);
        }

        private <T extends EObject> Factory<T> createDefaultFactory(Class<T> symbolType) {
            return () -> new DefaultModelDescriptor<>(symbolType, grammarAnalyzer, ePackage, inferenceStrategy);
        }

        private static class GetFirstInferenceStrategy  implements InferenceStrategy {
            @Override
            public Optional<Boolean> getIsASTRoot() {
                return Optional.empty();
            }

            @Override
            public Optional<EClass> postSelectParentType(@Nonnull Set<EClass> parentTypes) {
                return parentTypes.stream().findFirst();
            }

            @Override
            public Optional<EClass> postSelectReturnType(@Nonnull Set<EClass> returnTypes) {
                return returnTypes.stream().findFirst();
            }
        }
    }
}