package io.sidews.linkerman;

import org.eclipse.emf.ecore.EObject;

import java.util.List;

public interface EmbeddingDefinition<T extends EObject> {
    String getKey();
    ModelDescriptor<T> getModelDescriptor();
    Embedding<T> createEmbedding(DynamicModel<T> value);
    Embedding<T> createEmbedding(List<DynamicModel<T>> values);
}

