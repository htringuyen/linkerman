package io.sidews.linkerman;

import org.eclipse.emf.ecore.EObject;

public interface Compiler {

    <T extends EObject> CompilationResult<T> compile(String snippet, ModelDescriptor<T> descriptor);

    <T extends EObject> Compilation<T> prepareCompilation(String snippet, ModelDescriptor<T> descriptor, EmbeddingDefinition<?>... defs);

}
