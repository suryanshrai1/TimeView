package timeview.ui;

import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.io.File;
import java.nio.file.Path;
import timeview.watcher.DirectoryWatcher;
import timeview.snapshot.SnapshotManager;
import java.io.IOException;

public class TimeViewUI {
    private Label folderLabel;
    private TextArea logArea;
    private DirectoryWatcher watcher;
    private SnapshotManager snapshotManager;
    private Path watchedFolder;

    public void start(Stage primaryStage) {
        primaryStage.setTitle("TimeView");

        folderLabel = new Label("No folder selected.");
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);

        Button selectFolderBtn = new Button("Select Folder");
        selectFolderBtn.setOnAction(e -> openFolderChooser(primaryStage));

        VBox root = new VBox(10, selectFolderBtn, folderLabel, logArea);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 500, 350);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openFolderChooser(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Monitor");
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            folderLabel.setText("Selected: " + selectedDirectory.getAbsolutePath());
            logArea.clear();
            if (watcher != null) watcher.stop();
            watchedFolder = selectedDirectory.toPath();
            try {
                snapshotManager = new SnapshotManager(watchedFolder);
            } catch (IOException e) {
                logArea.appendText("Failed to initialize SnapshotManager: " + e.getMessage() + "\n");
                return;
            }
            watcher = new DirectoryWatcher(watchedFolder, msg -> {
                javafx.application.Platform.runLater(() -> {
                    logArea.appendText(msg + "\n");
                    // Take snapshot on any change, but skip if .time_machine is involved
                    if (!msg.contains(".time_machine")) {
                        try {
                            snapshotManager.takeSnapshot(watchedFolder);
                            logArea.appendText("[Snapshot taken]\n");
                        } catch (IOException ex) {
                            logArea.appendText("[Snapshot error] " + ex.getMessage() + "\n");
                        }
                    }
                });
            });
            watcher.start();
        }
    }
} 