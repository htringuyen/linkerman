package io.sidews.linkerman.base;

import com.google.inject.Injector;
import io.sidews.linkerman.DSLContext;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.serializer.ISerializer;
import org.eclipse.xtext.serializer.analysis.IGrammarConstraintProvider;
import org.eclipse.xtext.validation.IResourceValidator;

public class BaseDSLContext implements DSLContext {

    private final Injector injector;

    private final EClass rootEClass;

    private final EPackage ePackage;

    private final EFactory eFactory;

    private final ResourceSet resourceSet;

    private final Grammar grammar;

    private final IGrammarConstraintProvider grammarConstraintProvider;

    private final String extension;

    public BaseDSLContext(EClass rootEClass, Injector injector, String extension) {
        this.rootEClass = rootEClass;
        this.ePackage = rootEClass.getEPackage();
        this.eFactory = ePackage.getEFactoryInstance();
        this.injector = injector;
        this.resourceSet = injector.getInstance(ResourceSet.class);
        this.grammar = injector.getInstance(IGrammarAccess.class).getGrammar();
        this.grammarConstraintProvider = injector.getInstance(IGrammarConstraintProvider.class);
        if (extension == null) {
            this.extension = resourceSet.getResourceFactoryRegistry()
                    .getExtensionToFactoryMap()
                    .keySet()
                    .stream()
                    .findFirst()
                    .orElseThrow();
        }
        else {
            this.extension = extension;
        }
    }

    public BaseDSLContext(EClass rootEClass, Injector injector) {
        this(rootEClass, injector, null);
    }

    @Override
    public Injector getInjector() {
        return injector;
    }

    @Override
    public EClass getRootEClass() {
        return rootEClass;
    }

    @Override
    public String getExtension() {
        return extension;
    }

    @Override
    public ResourceSet getResourceSet() {
        return resourceSet;
    }

    @Override
    public EPackage getEPackage() {
        return ePackage;
    }

    @Override
    public EFactory getEFactory() {
        return eFactory;
    }

    @Override
    public Grammar getGrammar() {
        return grammar;
    }

    @Override
    public IGrammarConstraintProvider getGrammarConstraintProvider() {
        return grammarConstraintProvider;
    }

    @Override
    public ISerializer getXtextSerializer() {
        return getInjector().getInstance(ISerializer.class);
    }

    @Override
    public IResourceValidator getValidator() {
        return getInjector().getInstance(IResourceValidator.class);
    }
}
