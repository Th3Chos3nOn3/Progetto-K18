package eu.newton.reworkedui;

import com.jfoenix.controls.JFXButton;
import eu.newton.reworkedui.functioninput.FunctionInputMenu;
import eu.newton.reworkedui.functionmanager.IFunctionManager;
import eu.newton.reworkedui.planes.CartesianPlane;
import javafx.animation.*;
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

        graphicInit();

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

    private void graphicInit() {
        show = new JFXButton(">>");
        show.setStyle(
                "-fx-background-color: #bfbfbf;" +
                        "-jfx-button-type: RAISED;"
        );
        StackPane.setAlignment(show, Pos.TOP_LEFT);
        show.setTranslateY(10);
        show.setTranslateX(10);

        functionTextPopup = new Label("f(x) = ");
        functionTextPopup.setStyle(
                "-fx-background-color: rgba(128,128,128,0.65);" +
                        "-fx-background-radius: 10;" +
                        "-fx-pref-height: 50px;" +
                        "-fx-min-width: 80px;" +
                        "-fx-padding: 15px;" +
                        "-fx-alignment: center;" +
                        "-fx-font-style: oblique;"
        );

        StackPane.setAlignment(functionTextPopup, Pos.BOTTOM_CENTER);

        functionTextPopup.setTranslateY(-30);
        functionTextPopup.setVisible(false);

        StackPane.setAlignment(functionInputMenu, Pos.TOP_LEFT);

        Timeline waiting = new Timeline(new KeyFrame(Duration.seconds(3)));
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), functionTextPopup);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);
        fadePopupAnimation = new SequentialTransition(functionTextPopup, waiting, fadeTransition);

        getChildren().addAll(cartesianPlane, functionInputMenu, show, functionTextPopup);
    }
}