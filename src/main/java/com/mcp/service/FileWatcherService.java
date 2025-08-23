package com.mcp.service;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public interface FileWatcherService {
    void handleFileEvent(WatchEvent.Kind<?> kind, Path fullPath);
}
