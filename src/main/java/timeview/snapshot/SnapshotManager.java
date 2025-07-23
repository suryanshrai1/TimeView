package timeview.snapshot;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class SnapshotManager {
    private final Path snapshotsDir;
    private static final String SNAPSHOT_DIR_NAME = ".time_machine";

    public SnapshotManager(Path watchedFolder) throws IOException {
        this.snapshotsDir = watchedFolder.resolve(SNAPSHOT_DIR_NAME);
        if (!Files.exists(snapshotsDir)) {
            Files.createDirectory(snapshotsDir);
        }
    }

    public Path takeSnapshot(Path sourceFolder) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path snapshotPath = snapshotsDir.resolve(timestamp);
        copyFolder(sourceFolder, snapshotPath);
        return snapshotPath;
    }

    private void copyFolder(Path source, Path target) throws IOException {
        Files.walk(source)
            .filter(path -> {
                // Exclude .time_machine folder from being copied
                Path relativePath = source.relativize(path);
                return !relativePath.startsWith(SNAPSHOT_DIR_NAME);
            })
            .forEach(path -> {
                try {
                    Path relative = source.relativize(path);
                    Path dest = target.resolve(relative);
                    if (Files.isDirectory(path)) {
                        if (!Files.exists(dest)) {
                            Files.createDirectories(dest);
                        }
                    } else {
                        Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }
}
