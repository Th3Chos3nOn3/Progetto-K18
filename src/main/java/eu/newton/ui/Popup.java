package eu.newton.ui;

import javafx.animation.*;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;


class Popup extends Group {

    private final Label text;
    private final Rectangle background;

    private final SequentialTransition errorAnimation;

    public Popup() {
        text = new Label();
        text.getStyleClass().add("popupText");

        background = new Rectangle();
        background.getStyleClass().add("background");

        // Bindings
        background.xProperty().bind(text.layoutXProperty());
        background.yProperty().bind(text.layoutYProperty());
        background.widthProperty().bind(text.widthProperty());
        background.heightProperty().bind(text.heightProperty());

        Color errorColor = Color.rgb(245, 50, 41);
        FillTransition start = new FillTransition(Duration.seconds(.5), background, Color.rgb(128,128,128,0), errorColor);
        FillTransition end = new FillTransition(Duration.seconds(.5), background, errorColor, Color.rgb(128,128,128,0));
        errorAnimation = new SequentialTransition(start, end);

        Timeline waiting = new Timeline(new KeyFrame(Duration.seconds(3)));
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), this);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);
        SequentialTransition fadePopupAnimation = new SequentialTransition(this, waiting, fadeTransition);

        text.textProperty().addListener((listener) -> {
            this.setVisible(true);
            fadePopupAnimation.playFromStart();
        });

        getStylesheets().add(getClass().getResource("/stylesheets/popupStylesheet.css").toExternalForm());

        getChildren().addAll(background, text);
    }

    StringProperty textProperty() {
        return text.textProperty();
    }

    void playErrorAnimation() {
        errorAnimation.play();
    }

}
