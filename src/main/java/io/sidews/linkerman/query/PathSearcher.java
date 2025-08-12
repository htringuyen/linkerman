package io.sidews.linkerman.query;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public interface PathSearcher {

    SegmentNode findPathTree(EClass fromType, EClass toType);

    List<LinkedList<EReference>> findPaths(EClass fromType, EClass toType);

}
