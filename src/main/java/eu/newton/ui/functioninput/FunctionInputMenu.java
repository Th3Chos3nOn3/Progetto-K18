package eu.newton.ui.functioninput;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXScrollPane;
import eu.newton.ui.functioninput.functionslotmanager.FunctionSlotManager;
import eu.newton.ui.functionmanager.IFunctionManager;
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
        addFunction.getStyleClass().add("addFunction");

        clear = new JFXButton("Clear");
        clear.getStyleClass().add("clear");

        hidden = new SimpleBooleanProperty(false);
        hide = new JFXButton("<<");
        hide.getStyleClass().add("hide");

        leftOptionsBar = new HBox(addFunction, clear);
        leftOptionsBar.getStyleClass().add("leftOptionsBar");

        optionsBar = new HBox(leftOptionsBar, hide);
        optionsBar.getStyleClass().add("optionsBar");

        functionsScrollPane = new ScrollPane(functionSlotManager);
        functionsScrollPane.getStyleClass().add("functionsScrollPane");
        JFXScrollPane.smoothScrolling(functionsScrollPane);

        getStyleClass().add("functionInputMenu");

        getChildren().addAll(optionsBar, functionsScrollPane);

        getStylesheets().add(getClass().getResource("/stylesheets/functionInputMenuStylesheet.css").toExternalForm());
    }
}