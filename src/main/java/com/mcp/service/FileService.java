package com.mcp.service;

import com.mcp.model.Edit;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

public interface FileService {
    String readFile(Path path);

    String readMultipleFiles(List<Path> paths);

    String writeFile(Path path, String content);

    String moveFile(Path sourcePath, Path targetPath);

    String getFileInfo(Path path);

    String searchFiles(Path startPath, PathMatcher patternMatcher, List<PathMatcher> excludeMatchers);

    String editFile(Path path, List<Edit> edits, Boolean dryRun);

    String getChanges(Path path);
}
