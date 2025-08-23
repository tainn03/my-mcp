package com.mcp.util;

import lombok.experimental.UtilityClass;

import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class AppendUtils {

    /**
     * Append the content of a file to the provided StringBuilder
     *
     * @param builder The StringBuilder to append to
     * @param file    The file whose content to append
     */
    public void appendFileContent(StringBuilder builder, Path file) {
        try {
            String content = Files.readString(file);
            appendContent(builder, file, content);
        } catch (Exception e) {
            appendError(builder, file, e);
        }
    }

    /**
     * Append file content to the results with formatting
     *
     * @param builder The StringBuilder to append to
     * @param path    The path of the file
     * @param content The content of the file
     */
    public void appendContent(StringBuilder builder, Path path, String content) {
        builder.append(path.toString())
                .append(":\n")
                .append(content)
                .append("\n\n---\n");
    }

    /**
     * Append an error message to the results with formatting
     *
     * @param builder The StringBuilder to append to
     * @param path    The path of the file
     * @param e       The exception that occurred
     */
    public void appendError(StringBuilder builder, Path path, Exception e) {
        builder.append(path.toString())
                .append(": Error - ")
                .append(e.getMessage())
                .append("\n\n---\n");
    }
}
