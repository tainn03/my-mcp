package com.mcp.service.impl;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.mcp.model.Edit;
import com.mcp.model.EditResult;
import com.mcp.service.FileService;
import com.mcp.service.FileVisitorService;
import com.mcp.util.AppendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class FileServiceImpl implements FileService {
    /**
     * Reads the contents of a file at the given path.
     *
     * @param path The path to the file.
     * @return The contents of the file, or an error message if an error occurs.
     */
    @Override
    public String readFile(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            return "ERROR READING FILE: " + path + " - " + e.getMessage();
        }
    }

    /**
     * Reads the contents of multiple files or all files in a directory.
     *
     * @param paths A list of file or directory paths to read.
     * @return The contents of the files, or error messages if any occur.
     */
    @Override
    public String readMultipleFiles(List<Path> paths) {
        StringBuilder results = new StringBuilder();
        for (Path validPath : paths) {
            if (Files.isDirectory(validPath)) {
                try (Stream<Path> stream = Files.walk(validPath)) {
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
     * Writes content to a file, creating it if it doesn't exist or overwriting it if it does.
     *
     * @param path    The path to the file.
     * @param content The content to write to the file.
     * @return A success message or an error message if an error occurs.
     */
    @Override
    public String writeFile(Path path, String content) {
        try {
            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return "SUCCESS WROTE TO FILE: " + path;
        } catch (IOException e) {
            return "ERROR WRITING TO FILE: " + path + " - " + e.getMessage();
        }
    }

    /**
     * Appends content to a file, creating it if it doesn't exist.
     *
     * @param sourcePath The path to the source file or directory.
     * @param targetPath The path to the target file or directory.
     * @return A success message or an error message if an error occurs.
     */
    @Override
    public String moveFile(Path sourcePath, Path targetPath) {
        try {
            Files.move(sourcePath, targetPath);
            return "SUCCESS MOVED FILE FROM: " + sourcePath + " TO: " + targetPath;
        } catch (IOException e) {
            return "ERROR MOVING FILE FROM: " + sourcePath + " TO: " + targetPath + " - " + e.getMessage();
        }
    }

    /**
     * Gets information about a file, including size, creation date, modification date, access date, and permissions.
     *
     * @param path The path to the file.
     * @return A string containing the file information, or an error message if an error occurs.
     */
    @Override
    public String getFileInfo(Path path) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault());
            String permissions = getPermissions(path);
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
            return String.format("ERROR GETTING INFO FOR FILE: %s - %s", path, e.getMessage());
        }
    }

    /**
     * Searches for files and directories matching a glob pattern, with optional exclusion patterns.
     *
     * @param startPath       The starting directory path for the search.
     * @param patternMatcher  The glob pattern to match files and directories.
     * @param excludeMatchers A list of glob patterns to exclude from the search.
     * @return A list of matching file and directory paths, or an error message if an error occurs.
     */
    @Override
    public String searchFiles(Path startPath, PathMatcher patternMatcher, List<PathMatcher> excludeMatchers) {
        FileVisitorService visitor = new FileVisitorService(startPath, patternMatcher, excludeMatchers);
        try {
            Files.walkFileTree(startPath, visitor);
        } catch (IOException e) {
            return "ERROR SEARCHING FILES: " + e.getMessage();
        }
        List<String> results = visitor.getResults();
        return results.isEmpty() ? "NO MATCHES FOUND" : String.join("\n", results);
    }

    /**
     * Edits a file by applying a list of text replacements, with an option for a dry run.
     *
     * @param path   The path to the file to edit.
     * @param edits  A list of Edit objects containing old and new text.
     * @param dryRun If true, the file will not be modified, but a diff will be returned.
     * @return A diff of the changes made, or an error message if an error occurs.
     */
    @Override
    public String editFile(Path path, List<Edit> edits, Boolean dryRun) {
        String originalContent = readFileContent(path);
        if (originalContent == null) {
            return "ERROR READING FILE: " + path;
        }
        EditResult editResult = applyEdits(originalContent, edits, path);
        if (editResult.getError() != null) {
            return editResult.getError();
        }
        String diff = generateDiff(path, editResult.getOriginalLines(), editResult.getOriginalLines());
        if (dryRun == null || !dryRun) {
            String writeError = writeFileContent(path, editResult.getModifiedContent());
            if (writeError != null) {
                return writeError;
            }
        }
        return diff;
    }

    /**
     * Reads the content of a file, returning null if an error occurs.
     *
     * @param path The path to the file.
     * @return The file content, or null if an error occurs.
     */
    private String readFileContent(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Gets the POSIX permissions of a file, returning "N/A" if not supported or "ERROR" if an error occurs.
     *
     * @param path The path to the file.
     * @return A string representing the file permissions.
     */
    private String getPermissions(Path path) {
        try {
            return PosixFilePermissions.toString(Files.getPosixFilePermissions(path));
        } catch (UnsupportedOperationException e) {
            return "N/A";
        } catch (IOException e) {
            return "ERROR";
        }
    }

    /**
     * Applies a list of text edits to the original content.
     *
     * @param originalContent The original file content.
     * @param edits           A list of Edit objects containing old and new text.
     * @param path            The path to the file being edited (for error messages).
     * @return An EditResult containing original lines, modified lines, modified content, or an error message.
     */
    private EditResult applyEdits(String originalContent, List<Edit> edits, Path path) {
        String modifiedContent = originalContent;
        for (Edit edit : edits) {
            String oldText = edit.oldText().replace("\r\n", "\n");
            String newText = edit.newText().replace("\r\n", "\n");
            if (!modifiedContent.contains(oldText)) {
                return new EditResult(null, null, null, "ERROR: TEXT TO REPLACE NOT FOUND IN FILE: " + path + " - TEXT: " + oldText);
            }
            modifiedContent = modifiedContent.replace(oldText, newText);
        }
        List<String> originalLines = Arrays.asList(originalContent.split("\n"));
        List<String> modifiedLines = Arrays.asList(modifiedContent.split("\n"));
        return new EditResult(originalLines, modifiedLines, modifiedContent, null);
    }

    /**
     * Generates a unified diff between the original and modified lines.
     *
     * @param path          The path to the file being diffed (for headers).
     * @param originalLines The original file lines.
     * @param modifiedLines The modified file lines.
     * @return A string containing the unified diff formatted in a code block.
     */
    private String generateDiff(Path path, List<String> originalLines, List<String> modifiedLines) {
        Patch<String> patch = DiffUtils.diff(originalLines, modifiedLines);
        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(path.toString(), path.toString(), originalLines, patch, 3);
        String diffString = String.join("\n", unifiedDiff);
        return "```diff\n" + diffString + "\n```";
    }

    /**
     * Writes the modified content back to the file.
     *
     * @param path    The path to the file.
     * @param content The modified content to write.
     * @return Null if successful, or an error message if an error occurs.
     */
    private String writeFileContent(Path path, String content) {
        try {
            Files.writeString(path, content);
            return null;
        } catch (IOException e) {
            return "ERROR WRITING TO FILE: " + path + " - " + e.getMessage();
        }
    }

    /**
     * Returns a unified diff of changes made to a file since its last commit.
     *
     * @param path The path to the file.
     * @return A string containing the unified diff, or an error message.
     */
    @Override
    public String getChanges(Path path) {
        try {
            ProcessBuilder builder = new ProcessBuilder("git", "diff", getGitBranchName(path), "--", path.getFileName().toString());
            builder.directory(path.getParent().toFile());
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String diff = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return "ERROR GETTING CHANGES FOR FILE: " + path;
            }
            return diff.isBlank() ? "NO CHANGES SINCE LAST COMMIT" : "```diff\n" + diff + "\n```";
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return "ERROR GETTING CHANGES FOR FILE: " + path + " - " + e.getMessage();
        }
    }

    private String getGitBranchName(Path path) {
        try {
            ProcessBuilder builder = new ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD@{upstream}");
            builder.directory(path.getParent().toFile());
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String branchName = new String(process.getInputStream().readAllBytes()).trim();
            log.info("DETECTED PARENT BRANCH NAME: {}", branchName);
            int exitCode = process.waitFor();
            if (exitCode == 0 && !branchName.isBlank()) {
                return branchName;
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("COULD NOT DETERMINE PARENT BRANCH NAME, DEFAULTING TO 'develop' - {}", e.getMessage());
        }
        return "develop";
    }
}
