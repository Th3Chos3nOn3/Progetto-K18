package eu.newton;

import eu.newton.reworkedui.Plotter;
import eu.newton.reworkedui.functionmanager.FunctionManager;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class Main extends Application {

    public void start(Stage primaryStage) {

        Plotter root = new Plotter(new FunctionManager(), -5, 5, -5, 5);

        // Jump not detected
        //root.plot(x -> 4 * Math.abs(x) / x);

        // Discontinuity detected
        //root.plot(x -> 1 / (x));

        // Discontinuity detected
        //root.plot(x -> Math.pow(Math.sin(2 * x), x));

        Scene scene = new Scene(root, 1280, 720);
        primaryStage.setTitle("Plotter");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static BigDecimal k(double d) {
        return BigDecimal.valueOf(d);
    }

}


