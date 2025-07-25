package io.sidews.linkerman;

import org.eclipse.emf.ecore.EObject;

public interface Serializer {

    String serializeToDSL(DynamicModel<? extends EObject> model);

    String serializeToXMI(DynamicModel<? extends EObject> model);

    String serializeToXMI(EObject symbol);

    EObject deserializeFromXMI(String xmi);
}
