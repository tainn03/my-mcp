package com.mcp.service;

import com.mcp.util.PathValidator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileWatcherService {
    private final PathValidator pathValidator;
    private final ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
    private final Map<WatchKey, Path> watchKeyMap = new ConcurrentHashMap<>();
    private final Map<String, Path> resourceMap = new ConcurrentHashMap<>();

    private WatchService watcher;
    private Consumer<String> resourceChangeCallback;

    @PostConstruct
    public void init() {
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
            startFileWatching();
            for (String path : pathValidator.getAllowedDirsAsString()) {
                Path dir = Paths.get(path);
                registerDirectoryForWatching(dir);
            }
        } catch (Exception e) {
            log.error("FAILED TO INITIALIZE FILE WATCHER", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (watcher != null) {
                watcher.close();
            }
        } catch (Exception e) {
            log.error("FAILED TO CLOSE FILE WATCHER", e);
        }
    }

    /**
     * Start the file watching thread
     */
    private void startFileWatching() {
        threadExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("FILE WATCHER THREAD INTERRUPTED");
                    break;
                }

                Path dir = watchKeyMap.get(key);
                if (dir == null) {
                    log.warn("WATCH KEY NOT RECOGNIZED");
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        log.warn("OVERFLOW EVENT");
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path filename = pathEvent.context();
                    Path fullPath = dir.resolve(filename);

                    handleFileEvent(kind, fullPath);

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(fullPath)) {
                        try {
                            registerDirectoryForWatching(fullPath);
                        } catch (IOException e) {
                            log.error("FAILED TO REGISTER NEW DIRECTORY {}", fullPath, e);
                            throw new RuntimeException(e);
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    watchKeyMap.remove(key);
                }
            }
        });
    }

    /**
     * Handles a file system event by updating the resource map and notifying the callback
     *
     * @param kind     The kind of event
     * @param fullPath The full path of the affected file
     */
    private void handleFileEvent(WatchEvent.Kind<?> kind, Path fullPath) {
        String uri = "file://" + fullPath.toAbsolutePath().toString();
        log.info("EVENT {} ON FILE {}", kind.name(), uri);

        if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            resourceMap.put(uri, fullPath);
            log.info("EVENT {} ON FILE {}", kind.name(), uri);
        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            resourceMap.remove(uri);
            log.info("FILE DELETED: {}", uri);
        }

        if (resourceChangeCallback != null) {
            resourceChangeCallback.accept(uri);
        }
    }

    /**
     * Register a directory and its subdirectories for watching
     *
     * @param directory The directory to register
     * @throws IOException If an I/O error occurs
     */
    private void registerDirectoryForWatching(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            log.error("{} IS NOT A DIRECTORY", directory);
            return;
        }

        Files.walk(directory)
                .filter(Files::isDirectory)
                .forEach(dir -> {
                    try {
                        dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_DELETE,
                                StandardWatchEventKinds.ENTRY_MODIFY);
                        log.info("REGISTERED DIRECTORY {}", dir);
                    } catch (IOException e) {
                        log.error("FAILED TO REGISTER DIRECTORY {}", dir, e);
                    }
                });
    }
}
