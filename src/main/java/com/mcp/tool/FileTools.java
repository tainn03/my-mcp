package com.mcp.tool;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
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
     * @return The contents of the file, or null if an error occurs
     */
    @Tool(name = "read_file", description = "Read the contents of a file")
    public String readFile(@ToolParam String path) {
        Path validPath = pathValidator.validatePath(path);
        try {
            return Files.readString(validPath);
        } catch (IOException e) {
            return null;
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
                try {
                    Files.walk(validPath)
                            .filter(Files::isRegularFile)
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
}
