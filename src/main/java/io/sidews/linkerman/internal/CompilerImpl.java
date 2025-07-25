package io.sidews.linkerman.internal;

import io.sidews.linkerman.*;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.ILocationInFileProvider;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CompilerImpl implements Compiler {

    private static final DynamicModel.NodeLinkStrategy RESET_ALL = new ResetAllNodeLinkStrategy();

    private final ModelDescriptor.Registry descriptorRegistry;

    private final DynamicModel.Registry modelRegistry;

    private final Serializer serializer;

    private final DSLContext dslContext;

    private final DSLLoader dslLoader;

    private final ILocationInFileProvider locationProvider;

    public CompilerImpl(ModelDescriptor.Registry descriptorRegistry,
                        DynamicModel.Registry modelRegistry,
                        Serializer serializer,
                        DSLContext dslContext) {
        this.descriptorRegistry = descriptorRegistry;
        this.modelRegistry = modelRegistry;
        this.serializer = serializer;
        this.dslContext = dslContext;
        this.dslLoader = new DSLLoader(dslContext);
        this.locationProvider = dslContext.getInjector().getInstance(ILocationInFileProvider.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends EObject> CompilationResult<T> compile(String snippet, ModelDescriptor<T> descriptor) {

        var targetModel = modelRegistry.createDefault(descriptor.getSymbolType());
        populateFullASTUpwardFrom(targetModel);
        var targetUriFragment = EcoreUtil.getURI(targetModel.getSymbol()).fragment();

        var finalScript = composeValidScriptEncompass(
                snippet, serializer.serializeToDSL(targetModel), targetUriFragment);

        var finalAST = dslLoader.loadDSL(finalScript);

        var targetNode = finalAST.eResource().getEObject(targetUriFragment);

        var resultXmi = serializer.serializeToXMI(targetNode);

        var resultNode = (T) serializer.deserializeFromXMI(resultXmi);

        var resultModel = modelRegistry.createWith(resultNode);

        return new CompilationResultImpl<>(List.of(resultModel), List.of());
    }

    String composeValidScriptEncompass(String snippet, String defaultScript, String targetURIFragment) {
        var rootNode = dslLoader.loadDSL(defaultScript);
        var targetNode = rootNode.eResource().getEObject(targetURIFragment);

        var targetRegion = locationProvider.getFullTextRegion(targetNode);
        var targetOffset = targetRegion.getOffset();
        var targetLength = targetRegion.getLength();

        return defaultScript.substring(0, targetOffset)
                + snippet
                + defaultScript.substring(targetOffset + targetLength);
    }

    String buildDefaultScriptEncompass(DynamicModel<? extends EObject> model) {
        populateFullASTUpwardFrom(model);
        return serializer.serializeToDSL(model);
    }

    private void populateFullASTUpwardFrom(DynamicModel<? extends EObject> node) {
        if (node.getDescriptor().isASTRoot()) {
            return;
        }
        var parent = modelRegistry.createDefault(node.getDescriptor().getParentType());
        populateFullASTUpwardFrom(parent);
        node.linkToParent(parent, RESET_ALL);
    }

    private static final class ResetAllNodeLinkStrategy implements DynamicModel.NodeLinkStrategy {
        @Override
        public boolean shouldOverrideSingular() {
            return true;
        }

        @Override
        public boolean shouldClearCollection() {
            return true;
        }

        @Override
        public Optional<EReference> selectContainingReference(Set<EReference> possibleReferences) {
            return possibleReferences.stream().findFirst();
        }
    }
}