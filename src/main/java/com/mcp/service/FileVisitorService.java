package com.mcp.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileVisitorService extends SimpleFileVisitor<Path> {
    Path startPath;
    PathMatcher patternMatcher;
    List<PathMatcher> excludeMatchers;
    @Getter
    List<String> results = new ArrayList<>();

    public FileVisitorService(Path startPath, PathMatcher patternMatcher, List<PathMatcher> excludeMatchers) {
        this.startPath = startPath;
        this.patternMatcher = patternMatcher;
        this.excludeMatchers = excludeMatchers;
    }

    /**
     * Invoked for a directory before entries in the directory are visited.
     * If the directory is excluded, skip its subtree.
     *
     * @param dir   a reference to the directory
     * @param attrs the directory's basic attributes
     * @return FileVisitResult to continue, skip subtree, or terminate
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (isExcluded(startPath.relativize(dir)))
            return FileVisitResult.SKIP_SUBTREE;
        if (patternMatcher.matches(dir.getFileName()))
            results.add(dir.toString());
        return FileVisitResult.CONTINUE;
    }

    /**
     * Invoked for a file in a directory.
     *
     * @param file  a reference to the file
     * @param attrs the file's basic attributes
     * @return FileVisitResult to continue, skip siblings, or terminate
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (isExcluded(startPath.relativize(file)))
            return FileVisitResult.CONTINUE;
        if (patternMatcher.matches(file.getFileName()))
            results.add(file.toString());
        return FileVisitResult.CONTINUE;
    }

    /**
     * Checks if the given path matches any of the exclude patterns.
     *
     * @param path the path to check
     * @return true if the path matches any exclude pattern, false otherwise
     */
    private boolean isExcluded(Path path) {
        return excludeMatchers.stream().anyMatch(m -> m.matches(path));
    }
}
