package io.sidews.linkerman;

import com.google.inject.Injector;
import io.sidews.linkerman.base.BaseDSLContext;
import org.eclipse.cdt.linkerscript.LinkerScriptStandaloneSetup;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptPackage;
import org.eclipse.emf.ecore.EClass;

public class LinkerScriptContext extends BaseDSLContext {

    private static final EClass rootEClass;

    private static final Injector injector;

    private static final String extension = "ld";

    static {
        injector = new LinkerScriptStandaloneSetup().createInjectorAndDoEMFRegistration();
        rootEClass = LinkerScriptPackage.eINSTANCE.getLinkerScript();
    }

    public LinkerScriptContext() {
        super("LinkerScript", rootEClass, injector, extension);
    }

    public LinkerScriptPackage getLinkerScriptPackage() {
        return (LinkerScriptPackage) getEPackage();
    }
}
