package timeview;

import javafx.application.Application;
import javafx.stage.Stage;
import timeview.ui.TimeViewUI;

public class Main extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        TimeViewUI ui = new TimeViewUI();
        ui.start(primaryStage);
    }
} 