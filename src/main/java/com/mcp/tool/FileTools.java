package com.mcp.tool;

import com.mcp.model.EditFileArgs;
import com.mcp.service.FileService;
import com.mcp.service.FileWatcherService;
import com.mcp.util.PathValidator;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardWatchEventKinds;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class FileTools {
    PathValidator pathValidator;
    FileWatcherService fileWatcherService;
    FileService fileService;

    /**
     * Tool to read the contents of a file
     *
     * @param path The path to the file
     * @return The contents of the file, or an error message if an error occurs
     */
    @Tool(name = "read_file", description = "Read the contents of a file")
    public String readFile(@ToolParam String path) {
        Path validPath = pathValidator.validatePath(path);
        return fileService.readFile(validPath);
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
        List<Path> validPaths = pathsToRead.stream().map(pathValidator::validatePath).toList();
        return fileService.readMultipleFiles(validPaths);
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
        String result = fileService.writeFile(validPath, content);
        if (result.startsWith("SUCCESS")) {
            fileWatcherService.handleFileEvent(StandardWatchEventKinds.ENTRY_MODIFY, validPath);
        }
        return result;
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
        String result = fileService.moveFile(validSourcePath, validTargetPath);
        if (result.startsWith("SUCCESS")) {
            fileWatcherService.handleFileEvent(StandardWatchEventKinds.ENTRY_DELETE, validSourcePath);
            fileWatcherService.handleFileEvent(StandardWatchEventKinds.ENTRY_CREATE, validTargetPath);
        }
        return result;
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
        return fileService.getFileInfo(validPath);
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
        return fileService.searchFiles(startPath, patternMatcher, excludeMatchers);
    }

    /**
     * Tool to perform a series of text replacements in a file, with an option for a dry run
     *
     * @param editFileArgs An EditFileArgs object containing the path to the file, a list of Edit objects specifying the text replacements, and an optional dryRun flag
     * @return A unified diff of the changes made, or an error message if an error occurs
     */
    @Tool(name = "edit_file", description = "Perform a series of text replacements in a file.")
    public String editFile(EditFileArgs editFileArgs) {
        Path validPath = pathValidator.validatePath(editFileArgs.path());
        return fileService.editFile(validPath, editFileArgs.edits(), editFileArgs.dryRun());
    }
}
