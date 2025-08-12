package io.sidews.linkerman.query;

import org.eclipse.emf.ecore.EReference;

import java.util.ArrayList;
import java.util.List;

public record SegmentNode(EReference value, List<SegmentNode> children) {

    public SegmentNode(EReference value) {
        this(value, new ArrayList<>());
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SegmentNode other) {
            return this.value.equals(other.value);
        }
        return false;
    }

    public void addChild(SegmentNode childNode) {
        children.add(childNode);
    }
}
