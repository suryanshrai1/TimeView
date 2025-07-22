package timeview.snapshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SnapshotManager {
    private final Path snapshotsDir;

    public SnapshotManager(Path watchedFolder) throws IOException {
        this.snapshotsDir = watchedFolder.resolve(".time_machine");
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
        Files.walk(source).forEach(path -> {
            try {
                Path relative = source.relativize(path);
                Path dest = target.resolve(relative);
                if (Files.isDirectory(path)) {
                    if (!Files.exists(dest)) {
                        Files.createDirectory(dest);
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