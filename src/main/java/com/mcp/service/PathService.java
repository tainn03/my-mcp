package com.mcp.service;

import java.nio.file.Path;
import java.util.List;

public interface PathService {
    boolean isAllowed(Path path);

    Path validatePath(String inputPath);

    List<String> getAllowedDirsAsString();

    Path getCurrentWorkingDir();

    String toContainerPath(String hostPath);
}
