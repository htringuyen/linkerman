package io.sidews.linkerman;

import com.google.inject.Injector;
import org.eclipse.cdt.linkerscript.LinkerScriptStandaloneSetup;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptPackage;
import org.eclipse.cdt.linkerscript.services.LinkerScriptGrammarAccess;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.serializer.analysis.IGrammarConstraintProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class XtextContext {

    private static final Injector injector;

    private static final ResourceSet resourceSet;

    private static final AtomicLong resourceCounter = new AtomicLong(0);

    static {
        injector = new LinkerScriptStandaloneSetup().createInjectorAndDoEMFRegistration();
        resourceSet = injector.getInstance(ResourceSet.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public static ResourceSet getResourceSet() {
        return injector.getInstance(ResourceSet.class);
    }

    public static XtextResource createResourceAndLoad(String content) {
        var resource = (XtextResource) resourceSet.createResource(generateResourceUri());
        try {
            resource.load(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), null);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resource;
    }

    public static XtextResource createDummyResource() {
        return (XtextResource) resourceSet.createResource(generateResourceUri());
    }

    private static URI generateResourceUri() {
        var uriStr = "inmemory:/dummy/content-" + resourceCounter.getAndIncrement() + ".ld";
        return URI.createURI(uriStr);
    }

    public static void attachResourceToAST(EObject node) {
        var root = EcoreUtil.getRootContainer(node);
        XtextContext.createDummyResource().getContents().add(root);
    }

    public static void load() {

    }

    public static Grammar getGrammar() {
        return injector.getInstance(LinkerScriptGrammarAccess.class).getGrammar();
    }

    public static LinkerScriptPackage getEPackage() {
        return LinkerScriptPackage.eINSTANCE;
    }

    public static List<EClass> getEAllClasses() {
        return getEPackage().getEClassifiers()
                .stream()
                .filter(c -> c instanceof EClass)
                .map(EClass.class::cast)
                .collect(Collectors.toList());
    }

    public static IGrammarConstraintProvider getGrammarConstraintProvider() {
        return getInjector().getInstance(IGrammarConstraintProvider.class);
    }
}




















