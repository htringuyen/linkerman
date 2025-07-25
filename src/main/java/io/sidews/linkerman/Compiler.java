package io.sidews.linkerman;

import org.eclipse.emf.ecore.EObject;

public interface Compiler {

    <T extends EObject> DynamicModel<T> compile(String snippet, ModelDescriptor<T> descriptor);

}
