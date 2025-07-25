package io.sidews.linkerman;

import org.eclipse.emf.ecore.EObject;

import java.util.List;
import java.util.stream.Stream;

public interface CompilationResult<T extends EObject> {

    Stream<DynamicModel<T>> stream();

    DynamicModel<T> getFirst();

    DynamicModel<T> single();

    int size();

    boolean hasError();

    List<CompilationError> getErrors();
}
