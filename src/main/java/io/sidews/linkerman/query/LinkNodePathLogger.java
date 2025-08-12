package io.sidews.linkerman.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class LinkNodePathLogger {
    private static final Logger logger = LoggerFactory.getLogger(LinkNodePathLogger.class);

    /**
     * Logs the path from root to leaf for a LinkNode
     */
    public static void logPath(PathSearcherOld.LinkNode node) {
        if (node == null) {
            logger.warn("Cannot log path for null LinkNode");
            return;
        }

        List<String> pathElements = buildPathList(node);
        String path = String.join(" --> ", pathElements);
        logger.info("Path: {}", path);
    }

    /**
     * Logs the path from root to leaf for a List of LinkNodes
     */
    public static void logPaths(List<PathSearcherOld.LinkNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            logger.warn("Cannot log paths for null or empty LinkNode list");
            return;
        }

        logger.info("Logging {} LinkNode paths:", nodes.size());
        for (int i = 0; i < nodes.size(); i++) {
            PathSearcherOld.LinkNode node = nodes.get(i);
            List<String> pathElements = buildPathList(node);
            String path = String.join(" --> ", pathElements);
            logger.info("Path {}: {}", i + 1, path);
        }
    }

    /**
     * Builds a list of path elements from root to leaf
     */
    private static List<String> buildPathList(PathSearcherOld.LinkNode node) {
        List<String> pathElements = new ArrayList<>();

        // Traverse up to root and collect all nodes
        PathSearcherOld.LinkNode current = node;
        while (current != null) {
            if (current.value != null) {
                // Get the EClass name from the EReference
                String eClassName = current.value.getEType().getName();
                pathElements.add(eClassName);
            }
            current = current.getParent();
        }

        // Reverse to get root-to-leaf order
        Collections.reverse(pathElements);
        return pathElements;
    }

    /**
     * Alternative method that includes EReference names in the path
     */
    public static void logDetailedPath(PathSearcherOld.LinkNode node) {
        if (node == null) {
            logger.warn("Cannot log detailed path for null LinkNode");
            return;
        }

        List<String> pathElements = buildDetailedPathList(node);
        String path = String.join(" --> ", pathElements);
        logger.info("Detailed Path: {}", path);
    }

    /**
     * Builds a detailed path list including EReference names
     */
    private static List<String> buildDetailedPathList(PathSearcherOld.LinkNode node) {
        List<String> pathElements = new ArrayList<>();

        PathSearcherOld.LinkNode current = node;
        while (current != null) {
            if (current.value != null) {
                String eClassName = current.value.getEType().getName();
                String eReferenceName = current.value.getName();
                pathElements.add(eClassName + "." + eReferenceName);
            }
            current = current.getParent();
        }

        Collections.reverse(pathElements);
        return pathElements;
    }
}
