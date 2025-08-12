package io.sidews.linkerman.query;

import org.eclipse.emf.ecore.EObject;

import java.util.function.Predicate;

public interface QueryPredicate<T extends EObject> {

    boolean test(T node);

    // Predicate combinators
    /*default QueryPredicate<T> and(QueryPredicate<T> other) { ... }
    default QueryPredicate<T> or(QueryPredicate<T> other) { ... }
    default QueryPredicate<T> negate() { ... }

    // Common predicate factories
    static <T extends EObject> QueryPredicate<T> hasFeature(String featureName) { ... }
    static <T extends EObject> QueryPredicate<T> instanceOf(Class<?> type) { ... }
    static <T extends EObject> QueryPredicate<T> nameEquals(String name) { ... }*/
}
