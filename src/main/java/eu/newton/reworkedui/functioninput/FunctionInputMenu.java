package eu.newton.reworkedui.functioninput;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXScrollPane;
import eu.newton.reworkedui.functionmanager.IFunctionManager;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class FunctionInputMenu extends VBox {
    private SimpleBooleanProperty hidden;

    private JFXButton addFunction, clear, hide;
    private HBox leftOptionsBar;
    private HBox optionsBar;

    private FunctionSlotManager functionSlotManager;
    private ScrollPane functionsScrollPane;

    public FunctionInputMenu(IFunctionManager functionManager) {
        functionSlotManager = new FunctionSlotManager(functionManager);

        init();

        addFunction.setOnAction(e -> functionSlotManager.newSlot());

        clear.setOnAction(e -> functionSlotManager.reset());

        hide.setOnAction(e -> {
            TranslateTransition t = new TranslateTransition(new Duration(250), this);
            t.setToX(-getMaxWidth());
            t.play();
            t.setOnFinished(e1 -> hidden.set(true));
        });
    }

    public SimpleStringProperty getSelectedSlotText() {
        return functionSlotManager.selectedSlotTextPropertyProperty();
    }

    public boolean isHidden() {
        return hidden.get();
    }

    public SimpleBooleanProperty hiddenProperty() {
        return hidden;
    }

    private void init() {
        addFunction = new JFXButton("+");
        addFunction.setStyle(
                "-fx-background-color: #a4a4a4;"
        );

        clear = new JFXButton("Clear");
        clear.setStyle(
                "-fx-background-color: #bc0000;" +
                        "-fx-font-weight: bold;"
        );

        hidden = new SimpleBooleanProperty(false);
        hide = new JFXButton("<<");
        hide.setStyle(
                "-fx-background-color: #a4a4a4;"
        );

        leftOptionsBar = new HBox(addFunction, clear);
        leftOptionsBar.setStyle(
                "-fx-spacing: 15;" +
                        "-fx-alignment: center-left;" +
                        "-fx-padding: 10;"
        );

        optionsBar = new HBox(leftOptionsBar, hide);
        optionsBar.setStyle(
                "-fx-pref-height: 45px;" +
                        "-fx-background-color: #d3d3d3;" +
                        "-fx-border-width: 0px 0px 2px 0px;" +
                        "-fx-border-style: solid;" +
                        "-fx-border-color: #bebebe;" +
                        "-fx-alignment: center-left;" +
                        "-fx-spacing: 240px;"
        );



        functionsScrollPane = new ScrollPane(functionSlotManager);
        JFXScrollPane.smoothScrolling(functionsScrollPane);
        functionsScrollPane.setStyle(
                "-fx-vbar-policy: never;" +
                        "-fx-fit-to-width: true;"
        );

        setStyle(
                "-fx-max-width: 410px;" +
                        "-fx-border-style: solid;" +
                        "-fx-border-color: #bebebe;" +
                        "-fx-border-width: 0px 2px 0px 0px;" +
                        "-fx-background-color: #ffffff;"
        );

        getChildren().addAll(optionsBar, functionsScrollPane);
    }
}