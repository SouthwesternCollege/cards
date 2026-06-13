package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Player-facing hot-seat privacy overlay.
 *
 * The overlay hides all hands between turns and reveals the next active
 * player's hand only after confirmation.
 */
public final class PassDeviceOverlay {

    private final double sceneWidth;
    private final double sceneHeight;

    private final CardViewFactory cardViewFactory = new CardViewFactory();

    private Group root;
    private boolean visible = false;

    public PassDeviceOverlay(double sceneWidth, double sceneHeight) {
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;
    }

    public void show(PlayerId nextPlayerId, Runnable onReady) {
        if (nextPlayerId == null) {
            throw new IllegalArgumentException("Next player id cannot be null.");
        }

        hideImmediately();

        visible = true;
        root = buildRoot(nextPlayerId, onReady == null ? () -> { } : onReady);
        root.setOpacity(0.0);

        FXGL.getGameScene().addUINode(root);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.18), root);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }


    public void showCastigoDecision(PlayerId playerId, Card discardCard, Runnable onTakeCastigo, Runnable onPass) {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (discardCard == null) {
            throw new IllegalArgumentException("Discard card cannot be null.");
        }

        hideImmediately();

        visible = true;
        root = buildCastigoDecisionRoot(
                playerId,
                discardCard,
                onTakeCastigo == null ? () -> { } : onTakeCastigo,
                onPass == null ? () -> { } : onPass
        );
        root.setOpacity(0.0);

        FXGL.getGameScene().addUINode(root);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.18), root);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    public void hideImmediately() {
        if (!visible && root == null) {
            return;
        }

        visible = false;

        if (root != null) {
            FXGL.getGameScene().removeUINode(root);
            root = null;
        }
    }

    private Group buildRoot(PlayerId nextPlayerId, Runnable onReady) {
        Group group = new Group();

        Rectangle background = new Rectangle(sceneWidth, sceneHeight);
        background.setFill(Color.color(0.01, 0.012, 0.018, 0.96));
        background.setStroke(Color.color(1.0, 0.82, 0.18, 0.45));
        background.setStrokeWidth(3);

        Text title = new Text("PASS DEVICE");
        title.setFont(loadFont(82));
        title.setFill(Color.GOLD);
        title.setEffect(dropShadow(Color.BLACK, 6));
        title.setTranslateX(centerTextX(title, 82));
        title.setTranslateY(sceneHeight * 0.34);

        Text instruction = new Text("Player " + nextPlayerId.value() + ", press Ready when the device is yours.");
        instruction.setFont(loadFont(30));
        instruction.setFill(Color.WHITE);
        instruction.setEffect(dropShadow(Color.BLACK, 3));
        instruction.setTranslateX(centerTextX(instruction, 30));
        instruction.setTranslateY(sceneHeight * 0.45);

        Text privacy = new Text("Hands are hidden between hot-seat turns.");
        privacy.setFont(loadFont(20));
        privacy.setFill(Color.color(0.78, 0.78, 0.78));
        privacy.setEffect(dropShadow(Color.BLACK, 2));
        privacy.setTranslateX(centerTextX(privacy, 20));
        privacy.setTranslateY(sceneHeight * 0.50);

        Button readyButton = gameButton("READY", Color.color(0.12, 0.42, 0.18));
        readyButton.setTranslateX(sceneWidth / 2.0 - 95);
        readyButton.setTranslateY(sceneHeight * 0.58);
        readyButton.setOnAction(event -> {
            hideImmediately();
            onReady.run();
        });

        group.getChildren().addAll(background, title, instruction, privacy, readyButton);
        return group;
    }


    private Group buildCastigoDecisionRoot(PlayerId playerId, Card discardCard, Runnable onTakeCastigo, Runnable onPass) {
        Group group = new Group();

        Rectangle background = new Rectangle(sceneWidth, sceneHeight);
        background.setFill(Color.color(0.01, 0.012, 0.018, 0.96));
        background.setStroke(Color.color(1.0, 0.82, 0.18, 0.45));
        background.setStrokeWidth(3);

        Text title = new Text("CASTIGO DECISION");
        title.setFont(loadFont(68));
        title.setFill(Color.GOLD);
        title.setEffect(dropShadow(Color.BLACK, 6));
        title.setTranslateX(centerTextX(title, 68));
        title.setTranslateY(sceneHeight * 0.22);

        Text instruction = new Text("Player " + playerId.value() + ", take the castigo?");
        instruction.setFont(loadFont(30));
        instruction.setFill(Color.WHITE);
        instruction.setEffect(dropShadow(Color.BLACK, 3));
        instruction.setTranslateX(centerTextX(instruction, 30));
        instruction.setTranslateY(sceneHeight * 0.31);

        Text privacy = new Text("Only the discard card is shown. Hands stay hidden.");
        privacy.setFont(loadFont(20));
        privacy.setFill(Color.color(0.78, 0.78, 0.78));
        privacy.setEffect(dropShadow(Color.BLACK, 2));
        privacy.setTranslateX(centerTextX(privacy, 20));
        privacy.setTranslateY(sceneHeight * 0.36);

        Node discardView = cardViewFactory.createView(discardCard);
        discardView.setTranslateX(sceneWidth / 2.0 - CardViewMetrics.renderedWidth() / 2.0);
        discardView.setTranslateY(sceneHeight * 0.41);

        Group takeButton = timedDecisionButton("TAKE CASTIGO", Color.color(0.48, 0.18, 0.10), onTakeCastigo, onPass);
        takeButton.setTranslateX(sceneWidth / 2.0 - 220);
        takeButton.setTranslateY(sceneHeight * 0.70);

        Button passButton = gameButton("PASS", Color.color(0.18, 0.18, 0.18));
        passButton.setTranslateX(sceneWidth / 2.0 + 30);
        passButton.setTranslateY(sceneHeight * 0.70);
        passButton.setOnAction(event -> {
            hideImmediately();
            onPass.run();
        });

        group.getChildren().addAll(background, title, instruction, privacy, discardView, takeButton, passButton);
        return group;
    }

    private Group timedDecisionButton(String textValue, Color color, Runnable onTakeCastigo, Runnable onTimeout) {
        Group group = new Group();

        double width = 230;
        double height = 78;

        Rectangle background = new Rectangle(width, height);
        background.setArcWidth(12);
        background.setArcHeight(12);
        background.setFill(color);
        background.setEffect(dropShadow(Color.color(0.1, 0.1, 0.1), 6));

        Rectangle timerFill = new Rectangle(0, height);
        timerFill.setArcWidth(12);
        timerFill.setArcHeight(12);
        timerFill.setFill(Color.color(0.42, 0.42, 0.42, 0.68));
        timerFill.setMouseTransparent(true);

        Text text = new Text(textValue);
        text.setFont(loadFont(23));
        text.setFill(Color.WHITE);
        text.setEffect(dropShadow(color.darker().darker(), 2));
        text.setMouseTransparent(true);
        text.setTranslateX(16);
        text.setTranslateY(48);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(timerFill.widthProperty(), 0)),
                new KeyFrame(Duration.seconds(5.0), new KeyValue(timerFill.widthProperty(), width))
        );
        timeline.setOnFinished(event -> {
            hideImmediately();
            onTimeout.run();
        });
        timeline.play();

        group.setOnMouseClicked(event -> {
            timeline.stop();
            hideImmediately();
            onTakeCastigo.run();
        });

        group.getChildren().addAll(background, timerFill, text);
        return group;
    }

    private Button gameButton(String textValue, Color color) {
        Button button = new Button();

        Text text = new Text(textValue);
        text.setFont(loadFont(28));
        text.setStyle("-fx-fill: #ffffff;");
        text.setEffect(dropShadow(color.darker().darker(), 2));

        button.setStyle(String.format("-fx-background-color: #%s ;", color.toString().substring(2, 8)));
        button.setPadding(new Insets(24));
        button.setPrefWidth(190);
        button.setPrefHeight(78);
        button.setEffect(dropShadow(Color.color(0.1, 0.1, 0.1), 6));
        button.setGraphic(text);

        return button;
    }

    private double centerTextX(Text text, double size) {
        // Font metrics may not be ready until layout; this approximation keeps
        // the prototype centered well enough while avoiding a layout pass.
        return sceneWidth / 2.0 - text.getText().length() * size * 0.28;
    }

    private Font loadFont(double size) {
        return Font.loadFont(getClass().getResourceAsStream("/DePixelHalbfett.ttf"), size);
    }

    private DropShadow dropShadow(Color color, double offsetY) {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(1);
        shadow.setOffsetY(offsetY);
        shadow.setColor(color);
        return shadow;
    }
}
