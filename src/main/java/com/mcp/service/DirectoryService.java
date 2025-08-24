package com.mcp.service;

import java.nio.file.Path;

public interface DirectoryService {
    String createDirectory(Path validPath);

    String listDirectory(Path validPath);

    String buildDirectoryTree(Path currentPath);
}
