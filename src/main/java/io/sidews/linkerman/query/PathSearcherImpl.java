package io.sidews.linkerman.query;

import io.sidews.linkerman.DSLContext;
import io.sidews.linkerman.base.SymbolManager;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import java.util.*;
import java.util.function.Predicate;

public class PathSearcherImpl implements PathSearcher {

    private final SymbolManager symbolManager;

    private final DSLContext context;

    PathSearcherImpl(DSLContext context, SymbolManager symbolManager) {
        this.context = context;
        this.symbolManager = symbolManager;
    }

    @Override
    public SegmentNode findPathTree(EClass fromPoint, EClass toPoint) {
        var rootNode = new SegmentNode(null);
        var nodeCache = new HashMap<EReference, SegmentNode>();
        for (var startSegment : getUpSegments(fromPoint)) {
            findPathTree(startSegment, nodeCache, rootNode,
                    point -> matchTargetPoint(point, toPoint));
        }
        return rootNode;
    }

    @Override
    public List<LinkedList<EReference>> findPaths(EClass fromPoint, EClass toPoint) {
        var rootNode = findPathTree(fromPoint, toPoint);
        var result = new ArrayList<LinkedList<EReference>>();
        for (var startNode : rootNode.children()) {
            result.addAll(expandNode(startNode));
        }
        return result;
    }

    private List<LinkedList<EReference>> expandNode(SegmentNode node) {
        var segment = node.value();
        var result = new ArrayList<LinkedList<EReference>>();
        if (node.children().isEmpty()) {
            var path = new LinkedList<EReference>();
            path.add(segment);
            result.add(path);
            return result;
        }

        for (var childNode : node.children()) {
            var paths = expandNode(childNode);
            paths.forEach(path -> path.add(segment));
            result.addAll(paths);
        }
        return result;
    }

    private boolean matchTargetPoint(EClass point, EClass targetPoint) {
        return targetPoint.isSuperTypeOf(point);
    }

    private SegmentNode findPathTree(EReference segment, Map<EReference, SegmentNode> nodeCache,
                                     SegmentNode rootNode, Predicate<EClass> rootPointMatcher) {
        if (!isStrictCyclicSafe(segment)) {
            return null;
        }

        var highPoint = segment.getEContainingClass();

        var resultNode = new SegmentNode(segment);

        if (rootPointMatcher.test(highPoint)) {
            rootNode.addChild(resultNode);
            nodeCache.put(segment, resultNode);
            return resultNode;
        }

        if (segment.getEReferenceType() == context.getRootEClass()) {
            throw new IllegalStateException("Unknown state");
        }

        for (var highSegment : getUpSegments(highPoint)) {
            var highNode = nodeCache.get(highSegment);
            if (highNode == null) {
                highNode = findPathTree(highSegment, nodeCache, rootNode, rootPointMatcher);
            }
            if (highNode != null) {
                highNode.addChild(resultNode);
                nodeCache.put(highSegment, highNode);
            }
        }
        return resultNode;
    }

    private List<EReference> getUpSegments(EClass highEnd) {
        return symbolManager.getSymbol(highEnd)
                .getEPossibleContainingReferences();
    }

    private boolean isStrictCyclicSafe(EReference segment) {
        var parent = segment.getEContainingClass();
        var child = segment.getEReferenceType();
        return !parent.isSuperTypeOf(child) && !child.isSuperTypeOf(parent);
    }
}
