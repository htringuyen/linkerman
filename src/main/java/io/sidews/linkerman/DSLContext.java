package io.sidews.linkerman;

import com.google.inject.Injector;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.serializer.ISerializer;
import org.eclipse.xtext.serializer.analysis.GrammarConstraintProvider;
import org.eclipse.xtext.serializer.analysis.IGrammarConstraintProvider;

public interface DSLContext {

    EClass getRootEClass();

    String getExtension();

    ResourceSet getResourceSet();

    EPackage getEPackage();

    EFactory getEFactory();

    Injector getInjector();

    Grammar getGrammar();

    IGrammarConstraintProvider getGrammarConstraintProvider();

    ISerializer getXtextSerializer();
}
