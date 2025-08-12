package io.sidews.linkerman.query;

public record PathStep(boolean descendant, String nodeType, String fieldName, String predicate) {

    public String getQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append(descendant ? "//" : "/");
        if (fieldName != null) sb.append(fieldName).append("::");
        sb.append(nodeType);
        if (predicate != null) sb.append("[").append(predicate).append("]");
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("PathStep { axis: %s, nodeType: '%s', fieldName: %s, predicate: %s }",
                descendant ? "DESCENDANT" : "CHILD",
                nodeType,
                fieldName != null ? "'" + fieldName + "'" : "null",
                predicate != null ? "'" + predicate + "'" : "null"
        );
    }
}
