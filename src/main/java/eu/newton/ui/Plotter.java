package eu.newton.ui;

import com.jfoenix.controls.JFXButton;
import eu.newton.ui.functioninput.FunctionInputMenu;
import eu.newton.ui.functionmanager.IFunctionManager;
import eu.newton.ui.planes.CartesianPlane;

import javafx.animation.KeyFrame;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class Plotter extends StackPane {

    private final IFunctionManager functionController;
    private final FunctionInputMenu functionInputMenu;
    private final CartesianPlane cartesianPlane;

    private JFXButton show;
    private Label functionTextPopup;
    private SequentialTransition fadePopupAnimation;

    public Plotter(IFunctionManager functionController, double xLow, double xHi, double yLow, double yHi) {
        this.functionController = functionController;
        functionInputMenu = new FunctionInputMenu(functionController);
        cartesianPlane = new CartesianPlane(functionController, xLow, xHi, yLow, yHi);

        init();

        show.visibleProperty().bindBidirectional(functionInputMenu.hiddenProperty());
        show.setOnAction(e -> {
            functionInputMenu.hiddenProperty().set(false);
            TranslateTransition t = new TranslateTransition(new Duration(250), functionInputMenu);
            t.setToX(functionInputMenu.getMinWidth());
            t.play();
        });

        functionTextPopup.textProperty().bind(functionInputMenu.getSelectedSlotText());
        functionTextPopup.textProperty().addListener((a) -> {
            functionTextPopup.setVisible(true);
            fadePopupAnimation.playFromStart();
        });
    }

    private void init() {
        show = new JFXButton(">>");
        show.getStyleClass().add("show");

        StackPane.setAlignment(show, Pos.TOP_LEFT);

        functionTextPopup = new Label("f(x) = ");
        functionTextPopup.getStyleClass().add("functionTextPopup");

        StackPane.setAlignment(functionTextPopup, Pos.BOTTOM_CENTER);

        functionTextPopup.setVisible(false);

        StackPane.setAlignment(functionInputMenu, Pos.TOP_LEFT);

        Timeline waiting = new Timeline(new KeyFrame(Duration.seconds(3)));
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), functionTextPopup);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);
        fadePopupAnimation = new SequentialTransition(functionTextPopup, waiting, fadeTransition);

        getChildren().addAll(cartesianPlane, functionInputMenu, show, functionTextPopup);

        getStylesheets().add(getClass().getResource("/stylesheets/plotterStylesheet.css").toExternalForm());
    }
}