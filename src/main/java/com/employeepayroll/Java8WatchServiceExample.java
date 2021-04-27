package com.employeepayroll;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

//UC3 Java8 WatchService
public class Java8WatchServiceExample {
    private final WatchService watcher;
    private final Map<WatchKey, Path> dirWatchers;

    //creates a watch service and register the given directory
    Java8WatchServiceExample(Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.dirWatchers = new HashMap<WatchKey, Path>();
        scanAndRegisterDirectories(dir);
    }

    //Register the given directory with the WatchService
    private void registerDirWatchers(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        dirWatchers.put(key, dir);
    }

    //Register the given directory, and all its sub-directories with the watchService
    private void scanAndRegisterDirectories(final Path start) throws IOException {
        //register dir and its sub-dir's
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerDirWatchers(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    //Process all the events for keys queued to the watcher
    @SuppressWarnings({"rawtypes", "unchecked"})
    void processEvents() {
        while (true) {
            WatchKey key; //wait for the key to be signalled
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = dirWatchers.get(key);
            if (dir == null) continue;
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                Path name = ((WatchEvent<Path>) event).context();
                Path child = dir.resolve(name);
                System.out.format("%s : %s\n", event.kind().name(), child); //print out event

                //if dir is created, then register it and its sub-dir's
                if (kind.equals(ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child)) scanAndRegisterDirectories(child);
                    } catch (IOException x) {
                    }
                } else if (kind.equals(ENTRY_DELETE)) {
                    if (Files.isDirectory(child)) dirWatchers.remove(key);
                }
            }

            //reset key and remove from set if dir no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                dirWatchers.remove(key);
                // all directories are inaccessible
                if (dirWatchers.isEmpty()) break;
            }
        }
    }
}
