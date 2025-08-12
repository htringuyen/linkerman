package io.sidews.linkerman;

import org.eclipse.emf.ecore.EObject;

public interface Compiler {

    /**
     * Compile a DSL snippet into DynamicModel(s).
     * @param snippet A snippet of the target DSL that could be a fully working or partial content.
     *                For example, if the target DSL of this compiler is Java, the snippet could be a valid Class file or just a method.
     * @param descriptor The descriptor of the model being constructed.
     * @return The result of this compilation, that could be single or multiple DynamicModel(s).
     * @param <T> The Java Type of the grammar symbol (symbol is special term defined by this library).
     */
    <T extends EObject> CompilationResult<T> compile(String snippet, ModelDescriptor<T> descriptor);

    /**
     * Prepare a compilation for a template snippet that could contain embeddings.
     * @param snippet A snippet of the target DSL that could be a fully working or partial content.
     *                For example, if the target DSL of this compiler is Java, the snippet could be a valid Class file or just a method.
     * @param descriptor The descriptor of the model being constructed.
     * @param defs Embedding definition
     * @return The result of this compilation, there could be single or multiple results.
     * @param <T> The Java Type of the grammar symbol (symbol is special term defined by this library).
     */
    <T extends EObject> Compilation<T> prepareCompilation(String snippet, ModelDescriptor<T> descriptor, EmbeddingDefinition<?>... defs);
}
