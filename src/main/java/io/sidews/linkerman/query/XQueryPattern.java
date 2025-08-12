package io.sidews.linkerman.query;

import java.util.List;

public record XQueryPattern(List<PathStep> pathSteps) {

    public String getQuery() {
        return String.join("", pathSteps.stream()
                .map(PathStep::getQuery)
                .toList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("XQueryPattern {\n  steps: [\n");
        for (int i = 0; i < pathSteps.size(); i++) {
            sb.append("    [").append(i).append("] ").append(pathSteps.get(i)).append("\n");
        }
        sb.append("  ]\n}");
        return sb.toString();
    }
}

