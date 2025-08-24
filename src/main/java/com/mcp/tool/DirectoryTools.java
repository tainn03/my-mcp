package com.mcp.tool;

import com.mcp.service.DirectoryService;
import com.mcp.service.PathService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class DirectoryTools {
    PathService pathService;
    DirectoryService directoryService;

    /**
     * Create a new directory, including any necessary but nonexistent parent directories.
     *
     * @param path the path of the directory to create
     * @return a message indicating success or failure
     */
    @Tool(name = "d01_create_directory", description = "Create a new directory, including any necessary but nonexistent parent directories.")
    public String createDirectory(@ToolParam(required = false) String path) {
        Path currentPath = (path == null || path.isBlank()) ? pathService.getCurrentWorkingDir() : pathService.validatePath(path);
        return directoryService.createDirectory(currentPath);
    }

    /**
     * List the contents of a directory.
     *
     * @param path the path of the directory to list; if null or blank, lists the first allowed directory
     * @return a formatted string listing files and directories
     */
    @Tool(name = "d02_list_directory", description = "List the contents of a directory.")
    public String listDirectory(@ToolParam(required = false) String path) {
        Path currentPath = (path == null || path.isBlank()) ? pathService.getCurrentWorkingDir() : pathService.validatePath(path);
        return directoryService.listDirectory(currentPath);
    }

    /**
     * Display the directory structure as a JSON tree.
     *
     * @param path the path of the directory to display
     * @return a JSON representation of the directory tree
     */
    @Tool(name = "d03_directory_tree", description = "Display the directory structure as a JSON tree.")
    public String directoryTree(@ToolParam(required = false) String path) {
        Path currentPath = (path == null || path.isBlank()) ? pathService.getCurrentWorkingDir() : pathService.validatePath(path);
        return directoryService.buildDirectoryTree(currentPath);
    }
}
