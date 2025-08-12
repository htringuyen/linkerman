package io.sidews.linkerman.query;

import org.eclipse.emf.ecore.EObject;

import java.util.List;

public interface XPathQuery {

    List<EObject> execute(String query);
}
