package io.sidews.linkerman.internal;

import io.sidews.linkerman.DSLContext;
import io.sidews.linkerman.base.DynamicProcessingException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.Issue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DSLLoader {

    private final DSLContext dslContext;

    public DSLLoader(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public LoadResult loadDSL(String content) {
        var resource = dslContext.getResourceSet()
                .createResource(InMemURIGenerator.createFor(dslContext.getExtension()));
        try {
            var inStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            resource.load(inStream, null);
        }
        catch (IOException e) {
            throw new DynamicProcessingException("Failed to load DSL");
        }
        if (resource.getContents().isEmpty()) {
            throw new DynamicProcessingException("The parsed AST have no node");
        }
        var issues = dslContext.getValidator()
                .validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
        return new LoadResult(resource.getContents().getFirst(), issues);
    }

    public Resource createResourceAttachedTo(EObject eObject) {
        var resource = dslContext.getResourceSet()
                .createResource(InMemURIGenerator.createFor(dslContext.getExtension()));
        resource.getContents().add(eObject);
        return resource;
    }

    public static final class LoadResult {
        private final EObject root;
        private final List<Issue> issues;

        private LoadResult(EObject root, List<Issue> issues) {
            this.root = root;
            this.issues = issues;
        }

        public EObject getRoot() {
            return root;
        }

        public List<Issue> getIssues() {
            return issues;
        }
    }
}


