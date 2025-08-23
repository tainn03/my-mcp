package com.mcp.tool;

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
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FileTools {
    PathValidator pathValidator;

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
            log.error("FAILED TO READ FILE {}", path, e);
            return null;
        }
    }

    /**
     * Tool to read the contents of multiple files or all files in a directory
     *
     * @param paths A list of file or directory paths to read. If empty, reads from all allowed directories.
     * @return The contents of the files, or error messages if any occur
     */
    @Tool(name = "readMultipleFiles", description = "Read the contents of multiple files or all files in a directory.")
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
                    log.error("FAILED TO READ FILE {}", pathStr, e);
                    AppendUtils.appendError(results, validPath, e);
                }
            } else {
                AppendUtils.appendFileContent(results, validPath);
            }
        }
        return results.toString();
    }
}
