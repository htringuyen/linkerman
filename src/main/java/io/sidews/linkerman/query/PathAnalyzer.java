package io.sidews.linkerman.query;

import io.sidews.linkerman.DSLContext;
import io.sidews.linkerman.util.EMFUtil;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PathAnalyzer {

    private final PathSearcher pathSearcher;

    private final DSLContext context;

    public PathAnalyzer(DSLContext context, PathSearcher pathSearcher) {
        this.context = context;
        this.pathSearcher = pathSearcher;
    }

    public List<LinkedList<EReference>> findPossiblePaths(String query) {
        var pattern = XQueryParser.parse(query);
        List<LinkedList<EReference>> result = new ArrayList<>();
        EClass currenPoint = null;
        for (var step : pattern.pathSteps()) {
            var nextPoint = getPoint(step.nodeType());
            if (currenPoint != null) {
                var paths = pathSearcher.findPaths(nextPoint, currenPoint);
                result = concatPaths(paths, result, step);
            }
            currenPoint = getPoint(step.nodeType());
        }
        return result;
    }

    private List<LinkedList<EReference>> concatPaths(
            List<LinkedList<EReference>> firstPaths, List<LinkedList<EReference>> lastPaths, PathStep step) {
        List<LinkedList<EReference>> result = new ArrayList<>();
        for (var firstPath : firstPaths) {
            if (!validatePath(firstPath, !step.descendant(), step.fieldName())) {
                continue;
            }
            for (var lastPath : lastPaths) {
                firstPath.addAll(lastPath);
            }
            result.add(firstPath);
        }
        return result;
    }

    private boolean validatePath(LinkedList<EReference> path, boolean directRel, String fieldName) {
        if (directRel) {
            if (path.size() != 1) {
                return false;
            }
        }
        if (fieldName != null) {
            var lastSegment = path.getFirst();
            return lastSegment.getName().equals(fieldName);
        }
        return true;
    }

    private EClass getPoint(String pointName) {
        return (EClass) context.getEPackage().getEClassifier(pointName);
    }
}
