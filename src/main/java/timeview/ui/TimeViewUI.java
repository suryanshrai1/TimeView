package timeview.ui;

import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.io.File;

public class TimeViewUI {
    private Label folderLabel;

    public void start(Stage primaryStage) {
        primaryStage.setTitle("TimeView");

        folderLabel = new Label("No folder selected.");
        Button selectFolderBtn = new Button("Select Folder");
        selectFolderBtn.setOnAction(e -> openFolderChooser(primaryStage));

        VBox root = new VBox(10, selectFolderBtn, folderLabel);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 400, 150);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openFolderChooser(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Monitor");
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            folderLabel.setText("Selected: " + selectedDirectory.getAbsolutePath());
        }
    }
} 