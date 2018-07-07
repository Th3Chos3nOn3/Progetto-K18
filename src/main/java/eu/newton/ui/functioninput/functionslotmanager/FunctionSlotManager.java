package eu.newton.ui.functioninput.functionslotmanager;

import eu.newton.ui.functionmanager.IFunctionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class FunctionSlotManager extends VBox {

    private IFunctionManager functionManager;
    private int functionsCounter;

    private SimpleStringProperty selectedSlotTextProperty;

    public FunctionSlotManager(IFunctionManager functionManager) {
        this.functionManager = functionManager;
        functionsCounter = 0;

        selectedSlotTextProperty = new SimpleStringProperty();

        getChildren().add(new FunctionSlot(this, selectedSlotTextProperty, functionsCounter));
    }

    public void reset() {
        functionsCounter = 0;

        getChildren().clear();
        functionManager.clear();

        getChildren().add(new FunctionSlot(this, selectedSlotTextProperty, functionsCounter));
    }

    public String getSelectedSlotTextProperty() {
        return selectedSlotTextProperty.get();
    }

    public SimpleStringProperty selectedSlotTextPropertyProperty() {
        return selectedSlotTextProperty;
    }

    public void newSlot() {
        Node last = getChildren().get(0);

        if (last instanceof FunctionSlot && !((FunctionSlot) last).getText().equals("")) {
            getChildren().add(0, new FunctionSlot(this, selectedSlotTextProperty, ++functionsCounter));
        }

    }

    IFunctionManager getFunctionManager() {
        return functionManager;
    }

}
