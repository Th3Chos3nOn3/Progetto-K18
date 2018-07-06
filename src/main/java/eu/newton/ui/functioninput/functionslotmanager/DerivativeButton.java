package eu.newton.ui.functioninput.functionslotmanager;

import com.jfoenix.controls.JFXButton;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

class DerivativeButton extends HBox {
    private int derivativeOrder;
    private JFXButton increment, decrement;
    private JFXButton computeDerivative;
    private Label indexLabel;

    DerivativeButton() {
        init();

        increment.setOnAction(e -> {
            if (derivativeOrder < 99) {
                indexLabel.setText(String.valueOf(++derivativeOrder));
            }
        });

        decrement.setOnAction(e -> {
            if (derivativeOrder > 1) {
                indexLabel.setText(String.valueOf(--derivativeOrder));
            }
        });

        // TODO: to be implemented
        computeDerivative.setOnAction(e -> {});

    }

    void incrementDerivateiveOrder() {
        increment.fire();
    }

    void decrementDerivativeOrder() {
        decrement.fire();
    }

    private void init() {
        derivativeOrder = 1;

        increment = new JFXButton("▴");

        decrement = new JFXButton("▾");

        VBox modifier = new VBox(increment, decrement);
        modifier.getStyleClass().add("modifier");

        computeDerivative = new JFXButton("Dⁿf ");
        computeDerivative.getStyleClass().add("computeDerivative");

        indexLabel = new Label(String.valueOf(derivativeOrder));
        indexLabel.getStyleClass().add("indexLabel");

        DerivativeButton.this.getStyleClass().add("derivativeButton");

        getChildren().addAll(computeDerivative, indexLabel, modifier);
    }

}