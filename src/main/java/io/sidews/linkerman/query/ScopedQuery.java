package io.sidews.linkerman.query;

import org.eclipse.emf.ecore.EObject;

import javax.xml.xpath.XPath;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface ScopedQuery<T extends EObject> {

    // Structural filtering
    ScopedQuery<T> hasParent(Class<? extends EObject> parentType);
    ScopedQuery<T> hasParent(QueryPredicate<EObject> parentPredicate);
    ScopedQuery<T> hasAncestor(Class<? extends EObject> ancestorType);
    ScopedQuery<T> hasChild(Class<? extends EObject> childType);

    // Content filtering
    ScopedQuery<T> where(QueryPredicate<T> predicate);
    ScopedQuery<T> whereAttribute(String attributeName, Object value);
    ScopedQuery<T> whereReference(String referenceName, EObject target);

    // Existential quantifiers for relationships
    ScopedQuery<T> whereAnyChild(Class<? extends EObject> childType, QueryPredicate<? extends EObject> childPredicate);
    ScopedQuery<T> whereAllChildren(Class<? extends EObject> childType, QueryPredicate<? extends EObject> childPredicate);
    ScopedQuery<T> whereAnyDescendant(Class<? extends EObject> descendantType, QueryPredicate<? extends EObject> descendantPredicate);
    ScopedQuery<T> whereNoChild(Class<? extends EObject> childType, QueryPredicate<? extends EObject> childPredicate);

    // Convenient overloads
    ScopedQuery<T> whereAnyChild(QueryPredicate<EObject> childPredicate);
    ScopedQuery<T> whereChildExists(Class<? extends EObject> childType);

    // Type transformation
    <R extends EObject> ScopedQuery<R> cast(Class<R> targetType);

    // Terminal operations
    List<T> toList();
    Stream<T> stream();
    Optional<T> first();
    long count();
    boolean exists();
}
