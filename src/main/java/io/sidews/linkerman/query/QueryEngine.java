package io.sidews.linkerman.query;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class QueryEngine {

    private static final Logger logger = LoggerFactory.getLogger(QueryEngine.class);

    private final PathAnalyzer pathAnalyzer;
    
    public QueryEngine(PathAnalyzer pathAnalyzer) {
        this.pathAnalyzer = pathAnalyzer;
    }
    
    public List<EObject> executeSinglePath(String query, EObject root) {
        var paths = pathAnalyzer.findPossiblePaths(query);
        if (paths.size() != 1) {
            throw new IllegalArgumentException("Invalid path count: " + paths.size());
        }
        var path = paths.getFirst();
        List<EObject> currentNodes = new ArrayList<>();
        currentNodes.add(root);
        for (var segment : path.reversed()) {
            currentNodes = currentNodes.stream()
                    .map(node -> {
                        if (!segment.getEContainingClass().isSuperTypeOf(node.eClass())) {
                            throw new IllegalStateException(String.format("Invalid state: Containing type = %s, "));
                        }
                        return getSingleFeatureInstance(node, segment);
                    })
                    .toList();
        }
        return currentNodes;
    }

    private EObject getSingleFeatureInstance(EObject target, EReference feature) {
        if (feature.isMany()) {
            var collection = (EList<EObject>) target.eGet(feature);
            if (collection.size() != 1) {
                throw new RuntimeException();
            }
            return collection.getFirst();
        }
        return (EObject) target.eGet(feature);
    }
}
















