package eu.newton.reworkedui.functioninput;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import eu.newton.reworkedui.functionmanager.IFunctionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

class FunctionSlotManager extends VBox {

    private IFunctionManager functionManager;
    private int functionsCounter;

    private SimpleStringProperty selectedSlotTextProperty;

    FunctionSlotManager(IFunctionManager functionManager) {
        this.functionManager = functionManager;
        functionsCounter = 0;

        selectedSlotTextProperty = new SimpleStringProperty();

        getChildren().add(new FunctionSlot(functionsCounter));
    }

    void reset() {
        functionsCounter = 0;

        getChildren().clear();
        functionManager.clear();

        getChildren().add(new FunctionSlot(functionsCounter));
    }

    String getSelectedSlotTextProperty() {
        return selectedSlotTextProperty.get();
    }

    SimpleStringProperty selectedSlotTextPropertyProperty() {
        return selectedSlotTextProperty;
    }

    void newSlot() {
        Node last = getChildren().get(0);

        if (last instanceof FunctionSlot && !((FunctionSlot) last).functionInput.getText().equals("")) {
            getChildren().add(0, new FunctionSlot(++functionsCounter));
        }
    }

    private class FunctionSlot extends HBox {
        private int index;

        private Label prompt;
        private JFXTextField functionInput;
        private DerivativeButton derivativeButton;
        private JFXButton delete;

        FunctionSlot(int index) {
            this.index = index;

            graphicInit();

            functionInput.setOnMouseClicked(e -> selectedSlotTextProperty.bind(functionInput.textProperty()));
            functionInput.setOnKeyTyped(e -> selectedSlotTextProperty.bind(functionInput.textProperty()));

            functionInput.setOnKeyPressed(e -> {
                if (e.getCode().equals(KeyCode.UP)) {
                    derivativeButton.increment.fire();
                } else if (e.getCode().equals(KeyCode.DOWN)) {
                    derivativeButton.decrement.fire();
                }
            });

            functionInput.setOnAction(e -> functionManager.add(index, functionInput.getText()));

            delete.setOnAction(e -> {
                if (FunctionSlotManager.this.getChildren().size() > 1) {
                    FunctionSlotManager.this.getChildren().remove(this);

                    // Function removal
                    functionManager.remove(index);
                }
            });

        }

        private void graphicInit() {
            prompt = new Label("f(x) = ");
            prompt.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-font-style: oblique;"
            );

            functionInput = new JFXTextField("");
            functionInput.setStyle(
                    "-fx-pref-width: 165px;" +
                            "-fx-font-style: oblique;" +
                            "-jfx-focus-color: green;"
            );

            derivativeButton = new DerivativeButton();

            delete = new JFXButton("X");
            delete.setStyle(
                    "-fx-background-color: #bc0000;" +
                            "-fx-font-weight: bold;"
            );

            setStyle(
                    "-fx-pref-height: 50px;" +
                            "-fx-alignment: center;" +
                            "-fx-padding: 0px 2px 0px 8px;" +
                            "-fx-spacing: 8px;" +
                            "-fx-border-style: solid;" +
                            "-fx-border-color: gray;" +
                            "-fx-border-width: 0px 0px 1px 0px;" +
                            "-fx-background-color: white;"
            );

            getChildren().addAll(prompt, functionInput, derivativeButton, delete);
        }
    }

    private class DerivativeButton extends HBox{
        private int index;
        private JFXButton increment, decrement;
        private VBox modifier;
        private JFXButton computeDerivative;
        private Label indexLabel;

        DerivativeButton() {
            graphicInit();

            index = 1;

            increment.setOnAction(e -> {
                if (index >= 99) {
                    // Do nothing
                } else {
                    indexLabel.setText(String.valueOf(++index));
                }
            });

            decrement.setOnAction(e -> {
                if (index <= 1) {
                    // Do Nothing
                } else {
                    indexLabel.setText(String.valueOf(--index));
                }
            });

            computeDerivative.setOnAction(e -> {
                // TODO: to be implemented when symbolic derivative is added;
            });

        }

        private void graphicInit() {
            increment = new JFXButton("▴");

            decrement = new JFXButton("▾");

            modifier = new VBox(increment, decrement);
            modifier.setStyle(
                    "-fx-alignment: center;" +
                            "-fx-border-color: darkgreen;" +
                            "-fx-border-style: solid;" +
                            "-fx-border-width: 0px, 0px, 0px, 1px;"
            );

            computeDerivative = new JFXButton("Dⁿf ");
            computeDerivative.setStyle(
                    "-fx-pref-height: 50px;"
            );

            indexLabel = new Label(String.valueOf(index));
            indexLabel.setStyle(
                    "-fx-font-weight: bold;" +
                            "-fx-pref-width: 20px;"
            );

            setStyle(
                    "-fx-alignment: center;" +
                            "-fx-background-color: green;" +
                            "-fx-border-radius: 9;" +
                            "-fx-border-width: 10px;" +
                            "-fx-border-color: white;" +
                            "-fx-background-radius: 20;"
            );

            getChildren().addAll(computeDerivative, indexLabel, modifier);
        }
    }
}