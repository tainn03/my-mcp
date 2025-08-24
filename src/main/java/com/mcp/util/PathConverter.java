package com.mcp.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Converts file paths between the host machine and the container environment.
 * <p>
 * This utility reads environment variables to determine the workspace paths
 * on both the host and inside the container, allowing for seamless path
 * translation when the application is running inside Docker.
 */
public class PathConverter {

    private static final String HOST_WORKSPACE_ENV = "HOST_WORKSPACE";
    private static final String CONTAINER_WORKSPACE_ENV = "CONTAINER_WORKSPACE";

    private static final String hostWorkspace;
    private static final String containerWorkspace;
    private static final boolean isContainerized;

    static {
        hostWorkspace = System.getenv(HOST_WORKSPACE_ENV);
        containerWorkspace = System.getenv(CONTAINER_WORKSPACE_ENV);
        // The application is considered containerized if both variables are set.
        isContainerized = hostWorkspace != null && !hostWorkspace.isEmpty()
                && containerWorkspace != null && !containerWorkspace.isEmpty();
    }

    private PathConverter() {
        // Private constructor to prevent instantiation.
    }

    /**
     * Converts a given host path to its corresponding path inside the container.
     * <p>
     * If the application is not running in a container (i.e., environment variables
     * are not set), it returns the original path unchanged.
     *
     * @param hostPath The absolute path from the host machine.
     * @return The equivalent absolute path inside the container, or the original path
     * if not running in a container.
     */
    public static String toContainerPath(String hostPath) {
        if (!isContainerized || hostPath == null) {
            return hostPath;
        }

        // Normalize paths to handle different OS path separators ('\' vs '/')
        Path normalizedHostPath = Paths.get(hostPath).normalize();
        Path normalizedHostWorkspace = Paths.get(hostWorkspace).normalize();

        // Check if the provided path is within the host workspace
        if (normalizedHostPath.startsWith(normalizedHostWorkspace)) {
            // Get the relative path from the host workspace
            Path relativePath = normalizedHostWorkspace.relativize(normalizedHostPath);
            // Join it with the container workspace path
            Path containerPath = Paths.get(containerWorkspace).resolve(relativePath);
            return containerPath.normalize().toString();
        }

        // If the path is outside the mapped workspace, return it as is.
        // The application's security logic (e.g., ALLOWED_DIRS) should handle this case.
        return hostPath;
    }
}