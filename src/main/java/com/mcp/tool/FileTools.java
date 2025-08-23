package com.mcp.tool;

import com.mcp.service.FileVisitorService;
import com.mcp.service.FileWatcherService;
import com.mcp.util.AppendUtils;
import com.mcp.util.PathValidator;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FileTools {
    PathValidator pathValidator;
    FileWatcherService fileWatcherService;

    /**
     * Tool to read the contents of a file
     *
     * @param path The path to the file
     * @return The contents of the file, or an error message if an error occurs
     */
    @Tool(name = "read_file", description = "Read the contents of a file")
    public String readFile(@ToolParam String path) {
        Path validPath = pathValidator.validatePath(path);
        try {
            return Files.readString(validPath);
        } catch (IOException e) {
            return "ERROR READING FILE: " + validPath + " - " + e.getMessage();
        }
    }

    /**
     * Tool to read the contents of multiple files or all files in a directory
     *
     * @param paths A list of file or directory paths to read. If empty, reads from all allowed directories.
     * @return The contents of the files, or error messages if any occur
     */
    @Tool(name = "read_multiple_files", description = "Read the contents of multiple files or all files in a directory")
    public String readMultipleFiles(@ToolParam List<String> paths) {
        List<String> pathsToRead = (paths == null || paths.isEmpty()) ? pathValidator.getAllowedDirsAsString() : paths;
        StringBuilder results = new StringBuilder();
        for (String pathStr : pathsToRead) {
            Path validPath = pathValidator.validatePath(pathStr);
            if (Files.isDirectory(validPath)) {
                try (java.util.stream.Stream<Path> stream = Files.walk(validPath)) {
                    stream.filter(Files::isRegularFile)
                            .forEach(file -> AppendUtils.appendFileContent(results, file));
                } catch (IOException e) {
                    AppendUtils.appendError(results, validPath, e);
                }
            } else {
                AppendUtils.appendFileContent(results, validPath);
            }
        }
        return results.toString();
    }

    /**
     * Tool to write content to a file, creating it if it doesn't exist or overwriting it if it does
     *
     * @param path    The path to the file
     * @param content The content to write to the file
     * @return A success message or an error message if an error occurs
     */
    @Tool(name = "write_file", description = "Write content to a file, creating it if it doesn't exist or overwriting it if it does")
    public String writeFile(@ToolParam String path, @ToolParam String content) {
        Path validPath = pathValidator.validatePath(path);
        try {
            Files.writeString(validPath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            fileWatcherService.handleFileEvent(StandardWatchEventKinds.ENTRY_MODIFY, validPath);
            return "SUCCESS WROTE TO FILE: " + validPath;
        } catch (IOException e) {
            return "ERROR WRITING TO FILE: " + validPath + " - " + e.getMessage();
        }
    }

    /**
     * Tool to move or rename a file or directory
     *
     * @param sourcePath The path to the source file or directory
     * @param targetPath The path to the target file or directory
     * @return A success message or an error message if an error occurs
     */
    @Tool(name = "move_file", description = "Move or rename a file or directory")
    public String moveFile(@ToolParam String sourcePath, @ToolParam String targetPath) {
        Path validSourcePath = pathValidator.validatePath(sourcePath);
        Path validTargetPath = pathValidator.validatePath(targetPath);
        try {
            Files.move(validSourcePath, validTargetPath);
            fileWatcherService.handleFileEvent(StandardWatchEventKinds.ENTRY_DELETE, validSourcePath);
            fileWatcherService.handleFileEvent(StandardWatchEventKinds.ENTRY_CREATE, validTargetPath);
            return "SUCCESS MOVED FILE FROM: " + validSourcePath + " TO: " + validTargetPath;
        } catch (IOException e) {
            return "ERROR MOVING FILE FROM: " + validSourcePath + " TO: " + validTargetPath + " - " + e.getMessage();
        }
    }

    /**
     * Tool to get detailed information about a file or directory
     *
     * @param path The path to the file or directory
     * @return A string containing detailed information about the file or directory, or an error message if an error occurs
     */
    @Tool(name = "get_file_info", description = "Get detailed information about a file or directory.")
    public String getFileInfo(@ToolParam String path) {
        Path validPath = pathValidator.validatePath(path);
        try {
            BasicFileAttributes attrs = Files.readAttributes(validPath, BasicFileAttributes.class);
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault());
            String permissions;
            try {
                permissions = PosixFilePermissions.toString(Files.getPosixFilePermissions(validPath));
            } catch (UnsupportedOperationException e) {
                permissions = "N/A";
            }
            return String.format(
                    "Size: %d bytes%nCreated: %s%nModified: %s%nAccessed: %s%nIs Directory: %b%nIs File: %b%nPermissions: %s",
                    attrs.size(),
                    formatter.format(attrs.creationTime().toInstant()),
                    formatter.format(attrs.lastModifiedTime().toInstant()),
                    formatter.format(attrs.lastAccessTime().toInstant()),
                    attrs.isDirectory(),
                    attrs.isRegularFile(),
                    permissions
            );
        } catch (IOException e) {
            return String.format("ERROR GETTING INFO FOR FILE: %s - %s", validPath, e.getMessage());
        }
    }

    /**
     * Tool to search for files and directories matching a glob pattern, with optional exclusion patterns
     *
     * @param path            The starting directory path for the search
     * @param pattern         The glob pattern to match files and directories
     * @param excludePatterns A list of glob patterns to exclude from the search
     * @return A list of matching file and directory paths, or an error message if an error occurs
     */
    @Tool(name = "search_files", description = "Search for files and directories matching a glob pattern.")
    public String searchFiles(@ToolParam String path, @ToolParam String pattern, @ToolParam List<String> excludePatterns) {
        Path startPath = pathValidator.validatePath(path);
        PathMatcher patternMatcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        List<PathMatcher> excludeMatchers = (excludePatterns == null) ? Collections.emptyList() : excludePatterns.stream()
                .map(p -> FileSystems.getDefault().getPathMatcher("glob:" + p))
                .toList();

        FileVisitorService visitor = new FileVisitorService(startPath, patternMatcher, excludeMatchers);
        try {
            Files.walkFileTree(startPath, visitor);
        } catch (IOException e) {
            return "ERROR SEARCHING FILES: " + e.getMessage();
        }
        List<String> results = visitor.getResults();
        return results.isEmpty() ? "NO MATCHES FOUND" : String.join("\n", results);
    }
}
