package com.mcp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.service.DirectoryService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DirectoryServiceImpl implements DirectoryService {
    ObjectMapper mapper = new ObjectMapper();

    /**
     * Create a new directory, including any necessary but nonexistent parent directories.
     *
     * @param validPath the path of the directory to create
     * @return a message indicating success or failure
     */
    @Override
    public String createDirectory(Path validPath) {
        try {
            Files.createDirectories(validPath);
            return "SUCCESSFULLY CREATED DIRECTORY: " + validPath;
        } catch (IOException e) {
            return "ERROR CREATING DIRECTORY: " + e.getMessage();
        }
    }

    /**
     * List the contents of a directory.
     *
     * @param validPath the path of the directory to list
     * @return a formatted string listing files and directories
     */
    @Override
    public String listDirectory(Path validPath) {
        try (Stream<Path> stream = Files.list(validPath)) {
            return stream
                    .map(p -> (Files.isDirectory(p) ? "[DIR] " : "[FILE] ") + p.getFileName().toString())
                    .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "ERROR LISTING DIRECTORY: " + e.getMessage();
        }
    }

    /**
     * Display the directory structure as a JSON tree at the current working directory.
     *
     * @return a JSON representation of the directory tree
     */
    @Override
    public String buildDirectoryTree(Path currentPath) {
        Map<String, Object> tree = buildTree(currentPath);
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
        } catch (JsonProcessingException e) {
            return "ERROR CONVERTING TO JSON: " + e.getMessage();
        }
    }

    /**
     * Recursively build a tree structure of the directory.
     *
     * @param currentPath the current path being processed
     * @return a map representing the directory tree
     */
    private Map<String, Object> buildTree(final Path currentPath) {
        final Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("name", currentPath.getFileName().toString());
        final boolean isDir = Files.isDirectory(currentPath);
        entry.put("type", isDir ? "directory" : "file");

        if (isDir) {
            final List<Map<String, Object>> children = new ArrayList<>();
            try (Stream<Path> stream = Files.list(currentPath)) {
                stream.forEach(child -> children.add(buildTree(child)));
            } catch (IOException e) {
                return Map.of("error", "COULD NOT READ DIRECTORY: " + e.getMessage());
            }
            entry.put("children", children);
        } else {
            entry.put("children", List.of());
        }
        return entry;
    }
}
