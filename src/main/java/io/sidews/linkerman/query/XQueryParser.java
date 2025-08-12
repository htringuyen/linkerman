package io.sidews.linkerman.query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Compact parser implementation
public class XQueryParser {

    // Regex pattern: matches each path step
    private static final Pattern STEP_PATTERN = Pattern.compile(
            "(/{1,2})" +                                      // 1. Axis: / or //
            "(?:(\\w+)::)?" +                                  // 2. FieldName::
            "(\\*|[A-Za-z_][A-Za-z0-9_]*)" +                   // 3. NodeType
            "(?:\\[((?:[^\\[\\]]+|\\[[^\\]]*])*?)\\])?"        // 4. [Predicate]
    );

    public static XQueryPattern parse(String query) {
        List<PathStep> steps = new ArrayList<>();
        Matcher matcher = STEP_PATTERN.matcher(query);

        int index = 0;
        while (matcher.find()) {
            if (matcher.start() != index) {
                throw new IllegalArgumentException("Unexpected character at index " + index);
            }

            String axis = matcher.group(1);
            String field = matcher.group(2);
            String node = matcher.group(3);
            String pred = matcher.group(4);

            boolean descendant = axis.equals("//");
            steps.add(new PathStep(descendant, node, field, pred != null && !pred.isBlank() ? pred : null));

            index = matcher.end();
        }

        if (index != query.length()) {
            throw new IllegalArgumentException("Trailing characters after valid query: " + query.substring(index));
        }

        return new XQueryPattern(steps);
    }
}