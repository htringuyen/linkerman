package io.sidews.linkerman.internal;

import io.sidews.linkerman.CompilationError;
import io.sidews.linkerman.CompilationResult;
import io.sidews.linkerman.DynamicModel;
import io.sidews.linkerman.base.DynamicProcessingException;
import org.eclipse.emf.ecore.EObject;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class CompilationResultImpl<T extends EObject> implements CompilationResult<T> {

    private final List<DynamicModel<T>> compiledModels;

    private final List<CompilationError> errors;

    public CompilationResultImpl(@Nonnull List<DynamicModel<T>> compiledModels, @Nonnull List<CompilationError> errors) {
        Objects.requireNonNull(compiledModels);
        Objects.requireNonNull(errors);
        this.compiledModels = compiledModels;
        this.errors = errors;
    }

    @Override
    public boolean hasError() {
        return !errors.isEmpty();
    }

    @Override
    public Stream<DynamicModel<T>> stream() {
        if (compiledModels.isEmpty()) {
            throw new DynamicProcessingException("The result contains no models");
        }
        return compiledModels.stream();
    }

    @Override
    public DynamicModel<T> single() {
        if (compiledModels.size() != 1) {
            throw new DynamicProcessingException(String.format(
                    "The result is not single model container (actually %d)", compiledModels.size()));
        }
        return compiledModels.getFirst();
    }

    @Override
    public DynamicModel<T> getFirst() {
        if (compiledModels.isEmpty()) {
            throw new DynamicProcessingException("The result contains no models");
        }
        return compiledModels.getFirst();
    }

    @Override
    public int size() {
        return compiledModels.size();
    }

    @Override
    public List<CompilationError> getErrors() {
        return errors;
    }
}


















