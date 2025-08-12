package io.sidews.linkerman.query;

import io.sidews.linkerman.base.SymbolManager;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import java.util.*;
import java.util.function.Predicate;

public class PathSearcherOld {

    private final SymbolManager symbolManager;

    public PathSearcherOld(SymbolManager symbolManager) {
        this.symbolManager = symbolManager;
    }

    public List<LinkNode> searchPaths(Class<? extends EObject> symbolType) {
        var symbol = symbolManager.getSymbol(symbolType);
        var result = new ArrayList<LinkNode>();
        for (var containingRef : symbol.getEPossibleContainingReferences()) {
            result.addAll(searchPaths(containingRef));
        }
        return result;
    }

    public List<LinkNode> searchPaths(EReference reference) {
        var parentEType = reference.getEContainingClass();
        var parentSymbol = symbolManager.getSymbol(parentEType);
        var result = new ArrayList<LinkNode>();
        if (parentSymbol.isASTRoot()) {
            result.add(new LinkNode(null, reference));
            return result;
        }
        for (var containingRef : parentSymbol.getEPossibleContainingReferences()) {
            if (containingRef.getEReferenceType().isSuperTypeOf(reference.getEReferenceType())) {
                continue;
            }
            var parentNodes = searchPaths(containingRef);
            for (var parentNode : parentNodes) {
                result.add(new LinkNode(parentNode, reference));
            }
        }
        return result;
    }

    public static final class LinkNode {

        private final LinkNode parent;

        public final EReference value;

        public LinkNode(LinkNode parent, EReference value) {
            this.parent = parent;
            this.value = value;
        }

        LinkNode getParent() {
            return parent;
        }

        EReference getValue() {
            return value;
        }
    }

    public List<Stack<EReference>> searchPathsBound(EClass fromType, EClass toType) {
        var startSymbol = symbolManager.getSymbol(fromType);
        var result = new ArrayList<Stack<EReference>>();
        for (var startEdge : startSymbol.getEPossibleContainingReferences()) {
            result.addAll(searchPathsBound(startEdge,
                    t -> t.isSuperTypeOf(toType) || toType.isSuperTypeOf(t)));
        }
        return result;
    }

    public List<Stack<EReference>> searchPathsBound(EReference lastEdge, Predicate<EClass> targetMatcher) {
        var result = new LinkedList<Stack<EReference>>();
        if (!isStrictCyclicSafe(lastEdge)) {
            return result;
        }
        var parentEType = lastEdge.getEContainingClass();
        var parentSymbol = symbolManager.getSymbol(parentEType);
        if (targetMatcher.test(parentEType)) {
            var path = new Stack<EReference>();
            path.push(lastEdge);
            result.add(path);
            return result;
        }
        if (parentSymbol.isASTRoot()) {
            return result;
        }
        for (var nextEdge : parentSymbol.getEPossibleContainingReferences()) {
            if (isCyclicSafe(nextEdge, parentEType)) {
                var nextPaths = searchPathsBound(nextEdge, targetMatcher);
                for (var path : nextPaths) {
                    if (!path.isEmpty()) {
                        path.push(lastEdge);
                        result.add(path);
                    }
                }
            }
        }
        return result;
    }

    private boolean isCyclicSafe(EReference edge, EClass childEnd) {
        return symbolManager.getSymbol(edge.getEContainingClass())
                .getEPossibleParentTypes()
                .stream().noneMatch(grandParent -> grandParent.isSuperTypeOf(childEnd));
    }

    private boolean isStrictCyclicSafe(EReference edge) {
        var parent = edge.getEContainingClass();
        var child = edge.getEReferenceType();
        return !parent.isSuperTypeOf(child) && !child.isSuperTypeOf(parent);
    }
}















