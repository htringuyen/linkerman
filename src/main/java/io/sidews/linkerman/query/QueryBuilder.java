package io.sidews.linkerman.query;

import org.eclipse.emf.ecore.EObject;

public interface QueryBuilder<T extends EObject> {

    // Static entry points
    static <T extends EObject> QueryBuilder<T> select(Class<T> nodeType) {
        throw new RuntimeException();
    }
    static QueryBuilder<EObject> selectAll() {
        throw new RuntimeException();
    }

    // Scope definition - returns ScopedQuery (no more from* available)
    ScopedQuery<T> fromRoot(EObject root);
}
