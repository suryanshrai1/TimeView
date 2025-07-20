package timeview.watcher;

import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;

public class DirectoryWatcher {
    public interface ChangeListener {
        void onChange(String message);
    }

    private final Path folder;
    private final ChangeListener listener;
    private WatchService watchService;
    private ExecutorService executor;
    private volatile boolean running = false;

    private static final long BATCH_WINDOW_MS = 2000; // 2 second batch window
    private Timer batchTimer;
    private final Set<String> eventTrigger = Collections.synchronizedSet(new HashSet<>());
    private Map<String, Boolean> previousSnapshot = new HashMap<>(); // name -> isDirectory

    private static class FileEvent {
        final WatchEvent.Kind<?> kind;
        final String name;
        final boolean isDirectory;
        final LocalDateTime time;
        FileEvent(WatchEvent.Kind<?> kind, String name, boolean isDirectory, LocalDateTime time) {
            this.kind = kind;
            this.name = name;
            this.isDirectory = isDirectory;
            this.time = time;
        }
    }

    private static final long RENAME_WINDOW_MS = 3000;

    public DirectoryWatcher(Path folder, ChangeListener listener) {
        this.folder = folder;
        this.listener = listener;
    }

    public void start() {
        if (running) return;
        running = true;
        executor = Executors.newSingleThreadExecutor();
        batchTimer = new Timer(true);
        batchTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                processSnapshotDiff();
            }
        }, BATCH_WINDOW_MS, BATCH_WINDOW_MS);
        executor.submit(this::processEvents);
        // Take initial snapshot
        previousSnapshot = scanDirectory();
    }

    public void stop() {
        running = false;
        if (executor != null) executor.shutdownNow();
        if (batchTimer != null) batchTimer.cancel();
        if (watchService != null) {
            try { watchService.close(); } catch (IOException ignored) {}
        }
    }

    private void processEvents() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            folder.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            while (running) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changed = folder.resolve((Path) event.context());
                    String fileName = changed.getFileName().toString();
                    eventTrigger.add(fileName);
                }
                key.reset();
            }
        } catch (Exception e) {
            if (running) listener.onChange("[Watcher error] " + e.getMessage());
        }
    }

    private Map<String, Boolean> scanDirectory() {
        Map<String, Boolean> snapshot = new HashMap<>();
        File[] files = folder.toFile().listFiles();
        if (files != null) {
            for (File f : files) {
                snapshot.put(f.getName(), f.isDirectory());
            }
        }
        return snapshot;
    }

    private void processSnapshotDiff() {
        if (eventTrigger.isEmpty()) return; // No changes detected
        Map<String, Boolean> currentSnapshot = scanDirectory();
        Set<String> prevNames = new HashSet<>(previousSnapshot.keySet());
        Set<String> currNames = new HashSet<>(currentSnapshot.keySet());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        // Detect deletes and renames
        for (String oldName : prevNames) {
            if (!currNames.contains(oldName)) {
                // Try to find a rename (same type, new name)
                String renamedTo = null;
                for (String newName : currNames) {
                    if (!previousSnapshot.containsKey(newName)
                        && previousSnapshot.get(oldName).equals(currentSnapshot.get(newName))) {
                        renamedTo = newName;
                        break;
                    }
                }
                if (renamedTo != null) {
                    listener.onChange("[" + now.format(formatter) + "] RENAME: " + oldName + " -> " + renamedTo);
                    currNames.remove(renamedTo); // Don't double-log as create
                } else {
                    listener.onChange("[" + now.format(formatter) + "] DELETE: " + oldName);
                }
            }
        }
        // Detect creates
        for (String newName : currNames) {
            if (!previousSnapshot.containsKey(newName)) {
                listener.onChange("[" + now.format(formatter) + "] CREATE: " + newName);
            }
        }
        previousSnapshot = currentSnapshot;
        eventTrigger.clear();
    }

    private void checkForRenameOrCreate(String createName, DateTimeFormatter formatter) {
        // Remove all references to EventRecord, only use FileEvent for batching and processing events.
    }

    private void checkForRenameOrDelete(String deleteName, DateTimeFormatter formatter) {
        // Remove all references to EventRecord, only use FileEvent for batching and processing events.
    }

    private void log(String message, DateTimeFormatter formatter) {
        String timestamp = LocalDateTime.now().format(formatter);
        listener.onChange("[" + timestamp + "] " + message);
    }
}
