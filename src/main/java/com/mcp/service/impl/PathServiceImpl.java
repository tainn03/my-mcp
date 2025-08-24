package com.mcp.service.impl;

import com.mcp.service.PathService;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PathServiceImpl implements PathService {
    String containerWorkspace;
    String hostWorkspace;
    boolean isContainerized;
    List<String> allowedDirsString;
    List<Path> allowedPaths;

    @Autowired
    public PathServiceImpl(@Value("${app.allowed.dirs:}") String allowedDirs,
                           @Value("${app.host.workspace:}") String hostWorkspace,
                           @Value("${app.container.workspace:}") String containerWorkspace) {
        if (allowedDirs == null || allowedDirs.isBlank()) {
            throw new IllegalStateException("allowed.dirs property not set or empty");
        }
        List<String> allowedDirsList = Arrays.asList(allowedDirs.split(","));
        this.allowedDirsString = allowedDirsList;
        this.allowedPaths = allowedDirsList.stream()
                .map(p -> Paths.get(p).toAbsolutePath().normalize())
                .toList();
        this.hostWorkspace = hostWorkspace;
        this.containerWorkspace = containerWorkspace;
        this.isContainerized = hostWorkspace != null && !hostWorkspace.isEmpty()
                && containerWorkspace != null && !containerWorkspace.isEmpty();
    }

    @PostConstruct
    public void init() {
        for (Path path : allowedPaths) {
            if (Files.exists(path) && !Files.isDirectory(path)) {
                throw new IllegalStateException(path.toAbsolutePath().normalize() + " IS NOT A DIRECTORY");
            } else {
                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    throw new RuntimeException("FAILED TO CREATE DIRECTORY " + path.toAbsolutePath().normalize(), e);
                }
            }
        }
    }

    /**
     * Check if the given path is within any of the allowed directories
     *
     * @param path The path to check
     * @return true if the path is allowed, false otherwise
     */
    public boolean isAllowed(Path path) {
        return allowedPaths.stream()
                .anyMatch(path::startsWith);
    }

    /**
     * Validate the given path and return the normalized absolute path if valid
     *
     * @param inputPath The input path as a string
     * @return The normalized absolute path if valid
     * @throws SecurityException if the path is not allowed
     */
    public Path validatePath(String inputPath) {
        String containerPath = toContainerPath(inputPath);
        Path path = Paths.get(containerPath).toAbsolutePath().normalize();
        if (isAllowed(path)) {
            return path;
        }
        throw new SecurityException("ACCESS DENIED TO PATH: " + containerPath + ". ALLOWED DIRECTORIES: " + allowedDirsString);
    }

    /**
     * Get the list of allowed directories as strings
     *
     * @return List of allowed directories
     */
    public List<String> getAllowedDirsAsString() {
        return allowedDirsString;
    }

    /**
     * Get the base path for relative paths (current working directory)
     *
     * @return The base path
     */
    @Override
    public Path getCurrentWorkingDir() {
        return Paths.get(".").toAbsolutePath().normalize();
    }

    /**
     * Converts a given host path to its corresponding path inside the container.
     *
     * @param hostPath The absolute path from the host machine.
     * @return The equivalent absolute path inside the container, or the original path if not running in a container.
     */
    @Override
    public String toContainerPath(String hostPath) {
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
