package io.sidews.linkerman.internal;

import io.sidews.linkerman.*;
import io.sidews.linkerman.base.LinkStrategies;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.ILocationInFileProvider;
import org.eclipse.xtext.validation.Issue;

import java.util.List;

public class CompilerImpl implements Compiler {

    private final DynamicModel.Registry modelRegistry;

    private final Serializer serializer;

    private final DSLLoader dslLoader;

    private final ILocationInFileProvider locationProvider;

    public CompilerImpl(ModelDescriptor.Registry descriptorRegistry,
                        DynamicModel.Registry modelRegistry,
                        Serializer serializer,
                        DSLContext dslContext) {
        this.modelRegistry = modelRegistry;
        this.serializer = serializer;
        this.dslLoader = new DSLLoader(dslContext);
        this.locationProvider = dslContext.getInjector().getInstance(ILocationInFileProvider.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends EObject> CompilationResult<T> compile(String snippet, ModelDescriptor<T> descriptor) {

        var targetModel = modelRegistry.createDefault(descriptor.getSymbolType());
        populateFullASTUpwardFrom(targetModel);
        var targetUriFragment = EcoreUtil.getURI(targetModel.getSymbolInstance()).fragment();

        var finalScript = composeValidScriptEncompass(
                snippet, serializer.serializeToDSL(targetModel), targetUriFragment);

        var loadResult = dslLoader.loadDSL(finalScript);

        var finalAST = loadResult.getRoot();

        var errors = toErrors(loadResult.getIssues());

        var targetNode = finalAST.eResource().getEObject(targetUriFragment);

        var resultXmi = serializer.serializeToXMI(targetNode);

        var resultNode = (T) serializer.deserializeFromXMI(resultXmi);

        var resultModel = modelRegistry.createWith(resultNode);

        return new CompilationResultImpl<>(List.of(resultModel), errors);
    }

    private List<CompilationError> toErrors(List<Issue> issues) {
        return issues.stream()
                .map(CompilationErrorImpl::createFrom)
                .toList();
    }

    String composeValidScriptEncompass(String snippet, String defaultScript, String targetURIFragment) {
        var rootNode = dslLoader.loadDSL(defaultScript).getRoot();
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
        if (node.getDescriptor().getSymbol().isASTRoot()) {
            return;
        }
        var parent = modelRegistry.createDefault(node.getDescriptor().getParentType());
        populateFullASTUpwardFrom(parent);
        node.linkToParent(parent, LinkStrategies.FIRST_MATCHED_CLEAN_ANY);
    }

    @Override
    public <T extends EObject> Compilation<T> prepareCompilation(String snippet, ModelDescriptor<T> descriptor, EmbeddingDefinition<?>... defs) {
        return null;
    }
}