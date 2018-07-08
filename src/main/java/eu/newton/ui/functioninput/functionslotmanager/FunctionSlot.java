package eu.newton.ui.functioninput.functionslotmanager;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

class FunctionSlot extends HBox {

    private int index;

    private JFXTextField functionInput;
    private DerivativeButton derivativeButton;
    private JFXButton delete;

    FunctionSlot(
            FunctionSlotManager functionSlotManager,
            SimpleStringProperty selectedSlotTextProperty, SimpleBooleanProperty parseFailed,
            int index) {

        this.index = index;

        init();

        functionInput.setOnMouseClicked(e -> selectedSlotTextProperty.bind(functionInput.textProperty()));
        functionInput.setOnKeyTyped(e -> selectedSlotTextProperty.bind(functionInput.textProperty()));

        functionInput.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.UP)) {
                derivativeButton.incrementDerivativeOrder();
            } else if (e.getCode().equals(KeyCode.DOWN)) {
                derivativeButton.decrementDerivativeOrder();
            }
        });

        functionInput.setOnAction(e -> {

            if (!functionInput.getText().equals("")) {
                boolean parseResult = functionSlotManager.getFunctionManager().add(index, functionInput.getText());

                parseFailed.set(!parseResult);
            }

            parseFailed.set(false);

        });

        delete.setOnAction(e -> {
            if (functionSlotManager.getChildren().size() > 1) {
                functionSlotManager.getChildren().remove(this);

                // Function removal
                functionSlotManager.getFunctionManager().remove(index);
            }
        });

    }

    String getText() {
        return functionInput.getText();
    }

    private void init() {
        Label prompt = new Label("f(x) = ");
        prompt.getStyleClass().add("prompt");

        functionInput = new JFXTextField("");
        functionInput.getStyleClass().add("functionInput");

        derivativeButton = new DerivativeButton();

        delete = new JFXButton("X");
        delete.getStyleClass().add("delete");

        FunctionSlot.this.getStyleClass().add("functionSlot");

        getChildren().addAll(prompt, functionInput, derivativeButton, delete);

        getStylesheets().add(getClass().getResource("/stylesheets/functionSlotStylesheet.css").toExternalForm());
    }

}