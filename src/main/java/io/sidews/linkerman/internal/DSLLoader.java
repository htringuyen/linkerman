package io.sidews.linkerman.internal;

import io.sidews.linkerman.DSLContext;
import io.sidews.linkerman.base.DynamicProcessingException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class DSLLoader {

    private final DSLContext dslContext;

    DSLLoader(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    EObject loadDSL(String content) {
        var resource = dslContext.getResourceSet()
                .createResource(InMemURIGenerator.createFor(dslContext.getExtension()));
        try {
            var inStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            resource.load(inStream, null);
        }
        catch (IOException e) {
            throw new DynamicProcessingException("Failed to load DSL");
        }
        var root = dslContext.getEFactory().create(dslContext.getRootEClass());
        resource.getContents().add(root);
        return root;
    }

    Resource createResourceAttachedTo(EObject eObject) {
        var resource = dslContext.getResourceSet()
                .createResource(InMemURIGenerator.createFor(dslContext.getExtension()));
        resource.getContents().add(eObject);
        return resource;
    }
}


