package io.sidews.linkerman.query;

import io.sidews.linkerman.base.SymbolManager;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import java.util.*;
import java.util.function.Predicate;

public class AdvancedPathSearcher {

    private final SymbolManager symbolManager;

    public AdvancedPathSearcher(SymbolManager symbolManager) {
        this.symbolManager = symbolManager;
    }

    public ExpandingEdge findPathsBound(EClass fromType, EClass toType) {
        var fromSymbol = symbolManager.getSymbol(fromType);
        var result = new HashMap<EReference, ExpandingEdge>();
        var rootEdge = new ExpandingEdge(null, new ArrayList<>());
        for (var startRef : fromSymbol.getEPossibleContainingReferences()) {
            var child = computePaths(startRef, result, t -> t.isSuperTypeOf(toType) || toType.isSuperTypeOf(t));
        }
        for (var edge : result.values()) {
            if (edge.value().getEContainingClass().isSuperTypeOf(toType)) {
                rootEdge.addChild(edge);
            }
        }
        return rootEdge;
    }

    public List<Stack<EReference>> findExplicitPathsBound(EClass fromType, EClass toType) {
        var rootEdge = findPathsBound(fromType, toType);
        return findExplicitPathsBound(rootEdge);
    }

    private List<Stack<EReference>> findExplicitPathsBound(ExpandingEdge rootEdge) {
        var rootValue = rootEdge.value();
        var result = new ArrayList<Stack<EReference>>();
        if (rootEdge.childEdges().isEmpty()) {
            if (rootValue != null) {
                var path = new Stack<EReference>();
                path.push(rootValue);
                result.add(path);
            }
            return result;
        }
        for (var childEdge : rootEdge.childEdges()) {
            var paths = findExplicitPathsBound(childEdge);
            if (rootValue != null) {
                for (var path : paths) {
                    path.push(rootValue);
                }
            }
            result.addAll(paths);
        }
        return result;
    }

    private ExpandingEdge computePaths(EReference currentEdge, Map<EReference, ExpandingEdge> resultMap, Predicate<EClass> targetMatcher) {

        var result = resultMap.get(currentEdge);
        /*if (result != null) {
            return result;
        }*/

        if (!isStrictCyclicSafe(currentEdge)) {
            return null;
        }

        var parentEType = currentEdge.getEContainingClass();
        var parentSymbol = symbolManager.getSymbol(parentEType);

        if (targetMatcher.test(parentEType)) {
            var edge = new ExpandingEdge(currentEdge, new ArrayList<>());
            resultMap.put(currentEdge, edge);
            return edge;
        }

        if (parentSymbol.isASTRoot()) {
            return null;
        }

        result = new ExpandingEdge(currentEdge, new ArrayList<>());
        for (var containingRef : parentSymbol.getEPossibleContainingReferences()) {
            ExpandingEdge parentEdge = null;
            if (!resultMap.containsKey(containingRef)) {
                parentEdge = computePaths(containingRef, resultMap, targetMatcher);
                resultMap.put(containingRef, parentEdge);
            }
            else {
                parentEdge = resultMap.get(containingRef);
            }

            parentEdge.addChild(result);

//            if (parentEdge != null) {
//                parentEdge.addChild(result);
//            }
        }
        return result;
    }

    public record ExpandingEdge(EReference value, List<ExpandingEdge> childEdges) {
        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ExpandingEdge other) {
                return this.value.equals(other.value);
            }
            return false;
        }

        private void addChild(ExpandingEdge edge) {
            childEdges.add(edge);
        }
    }

    private boolean isStrictCyclicSafe(EReference edge) {
        var parent = edge.getEContainingClass();
        var child = edge.getEReferenceType();
        return !parent.isSuperTypeOf(child) && !child.isSuperTypeOf(parent);
    }
}
