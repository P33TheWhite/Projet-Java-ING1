package ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        DenoisingController controller = new DenoisingController(primaryStage);
        controller.initialize();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

