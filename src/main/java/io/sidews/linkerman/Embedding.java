package io.sidews.linkerman;

import org.eclipse.emf.ecore.EObject;

public interface Embedding<T extends EObject> {
    DynamicModel<T> getValue();
    EmbeddingDefinition<T> getDefinition();
}

