package io.sidews.linkerman;

import org.eclipse.emf.ecore.EObject;

public interface Compilation<T extends EObject> {

    CompilationResult<T> compile();

    void setEmbedding(Embedding<? extends EObject> embedding);
}

