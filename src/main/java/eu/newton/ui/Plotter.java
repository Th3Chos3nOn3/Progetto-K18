package eu.newton.ui;

import com.jfoenix.controls.JFXButton;
import eu.newton.ui.functioninput.FunctionInputMenu;
import eu.newton.ui.functionmanager.IFunctionManager;
import eu.newton.ui.planes.BetterCartesianPlane;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class Plotter extends StackPane {

    private static final double POPUP_HEIGHT_RATIO = 2.3;

    private final IFunctionManager functionController;
    private final FunctionInputMenu functionInputMenu;
    private final BetterCartesianPlane cartesianPlane;

    private JFXButton show;
    private Popup functionTextPopup;
    private SimpleBooleanProperty playErrorAnimation;

    public Plotter(IFunctionManager functionController, double xLow, double xHi, double yLow, double yHi) {
        this.functionController = functionController;
        functionInputMenu = new FunctionInputMenu(functionController);
        cartesianPlane = new BetterCartesianPlane(functionController, xLow, xHi, yLow, yHi);

        init();

        show.visibleProperty().bindBidirectional(functionInputMenu.hiddenProperty());
        show.setOnAction(e -> {
            functionInputMenu.hiddenProperty().set(false);
            TranslateTransition t = new TranslateTransition(new Duration(250), functionInputMenu);
            t.setToX(functionInputMenu.getMinWidth());
            t.play();
        });

        functionTextPopup.textProperty().bind(functionInputMenu.getSelectedSlotText());

        playErrorAnimation.bind(functionInputMenu.getParseFailed());
        playErrorAnimation.addListener(listener -> {

            if (playErrorAnimation.getValue()) {
                functionTextPopup.playErrorAnimation();
            }

        });

    }

    private void init() {
        StackPane.setAlignment(functionInputMenu, Pos.TOP_LEFT);

        show = new JFXButton(">>");
        show.getStyleClass().add("show");

        StackPane.setAlignment(show, Pos.TOP_LEFT);

        functionTextPopup = new Popup();
        functionTextPopup.getStyleClass().add("functionTextPopup");

        functionTextPopup.translateYProperty().bind(this.heightProperty().divide(POPUP_HEIGHT_RATIO));
        functionTextPopup.setVisible(false);

        playErrorAnimation = new SimpleBooleanProperty();

        getChildren().addAll(cartesianPlane, functionInputMenu, show, functionTextPopup);

        getStylesheets().add(getClass().getResource("/stylesheets/plotterStylesheet.css").toExternalForm());
    }
}

