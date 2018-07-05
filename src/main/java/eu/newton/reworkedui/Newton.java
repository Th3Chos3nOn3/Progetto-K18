package eu.newton.reworkedui;

import eu.newton.reworkedui.functionmanager.FunctionManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Newton extends Application {

    public void start(Stage primaryStage) {

        Plotter root = new Plotter(new FunctionManager(), -5, 5, -5, 5);

        Scene scene = new Scene(root, 1280, 720);

        primaryStage.setTitle("Plotter");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
