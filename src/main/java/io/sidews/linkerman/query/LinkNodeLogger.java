package io.sidews.linkerman.query;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LinkNodeLogger {
    private static final Logger logger = LoggerFactory.getLogger(LinkNodeLogger.class);

    public static void logPath(PathSearcherOld.LinkNode node, EClass leafType) {
        logPath(node, leafType, "");
    }

    public static void logPath(PathSearcherOld.LinkNode node, EClass leafType, String indent) {
        List<EReference> path = new ArrayList<>();
        PathSearcherOld.LinkNode current = node;

        while (current != null) {
            path.add(current.getValue());
            current = current.getParent();
        }

        // Reverse to get order: parent -> child
        Collections.reverse(path);

        // Build path string
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            EReference ref = path.get(i);
            EClass source = ref.getEContainingClass();

            sb.append(source.getName())
                    .append(" --")
                    .append(ref.getName())
                    .append("--> ");
        }

        // Append leafType as final target
        sb.append(leafType.getName());

        logger.info(indent + "Link path: {}", sb);
    }


}


