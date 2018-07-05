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

            init();

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

        private void init() {
            prompt = new Label("f(x) = ");
            prompt.getStyleClass().add("prompt");

            functionInput = new JFXTextField("");
            functionInput.getStyleClass().add("functionInput");

            derivativeButton = new DerivativeButton();

            delete = new JFXButton("X");
            delete.getStyleClass().add("delete");

            FunctionSlot.this.getStyleClass().add("functionSlot");

            getChildren().addAll(prompt, functionInput, derivativeButton, delete);

            FunctionSlot.this.getStylesheets().add(getClass().getResource("/functionSlotManagerStylesheet.css").toExternalForm());

        }

        private class DerivativeButton extends HBox{
            private int index;
            private JFXButton increment, decrement;
            private VBox modifier;
            private JFXButton computeDerivative;
            private Label indexLabel;

            DerivativeButton() {
                init();

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
                    FunctionSlotManager.this.newSlot();
                });

            }

            private void init() {
                increment = new JFXButton("▴");

                decrement = new JFXButton("▾");

                modifier = new VBox(increment, decrement);
                modifier.getStyleClass().add("modifier");

                computeDerivative = new JFXButton("Dⁿf ");
                computeDerivative.getStyleClass().add("computeDerivative");

                indexLabel = new Label(String.valueOf(index));
                indexLabel.getStyleClass().add("indexLabel");

                DerivativeButton.this.getStyleClass().add("derivativeButton");

                getChildren().addAll(computeDerivative, indexLabel, modifier);
            }

        }

    }

}
