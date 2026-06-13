package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * HUD panel for deck/discard interactions.
 *
 * Milestone 6D removes card overlays and uses dedicated controls below the
 * deck/discard piles. The Take Castigo button doubles as the visible
 * five-second decision timer.
 */
public final class DeckDiscardPanel {

    private static final double CARD_OFFSET = 2.0;
    private static final int MAX_VISIBLE_DECK_BACKS = 5;
    private static final double PILE_GAP = 28.0;
    private static final double BUTTON_WIDTH = 142.0;
    private static final double BUTTON_HEIGHT = 38.0;
    private static final double DECISION_SECONDS = 5.0;

    private final Deck deck;
    private final DeckDiscardActions actions;
    private final Rectangle2D hudArea;
    private final CardBackViewFactory cardBackViewFactory = new CardBackViewFactory();
    private final CardViewFactory cardViewFactory = new CardViewFactory();

    private final Group root = new Group();
    private final Group deckGroup = new Group();
    private final Group discardGroup = new Group();
    private final Group drawButton = new Group();
    private final Group takeCastigoButton = new Group();
    private final Group passButton = new Group();
    private final Rectangle takeCastigoTimerFill = new Rectangle(0, BUTTON_HEIGHT);
    private final Text deckCountText = new Text();
    private final Text discardStatusText = new Text();

    private Timeline takeCastigoTimeline;
    private boolean castigoAvailable = false;
    private boolean activePlayerDecisionMode = true;
    private Card topDiscardCard;

    public DeckDiscardPanel(GameLayout gameLayout, Deck deck, DeckDiscardActions actions) {
        if (gameLayout == null) {
            throw new IllegalArgumentException("GameLayout cannot be null.");
        }

        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null.");
        }

        if (actions == null) {
            throw new IllegalArgumentException("DeckDiscardActions cannot be null.");
        }

        this.hudArea = gameLayout.getHudArea();
        this.deck = deck;
        this.actions = actions;

        build();
        position();
        showActivePlayerControls(false);
        refresh();

        FXGL.getGameScene().addUINode(root);
    }

    private void build() {
        Text title = new Text("Deck / Discard");
        title.setFont(loadFont(18));
        title.setFill(Color.color(0.78, 0.78, 0.78));
        title.setEffect(dropShadow(Color.BLACK, 2));
        title.setTranslateX(0);
        title.setTranslateY(0);

        deckCountText.setFont(loadFont(14));
        deckCountText.setFill(Color.WHITE);
        deckCountText.setEffect(dropShadow(Color.BLACK, 2));

        discardStatusText.setFont(loadFont(13));
        discardStatusText.setFill(Color.color(0.72, 0.72, 0.72));
        discardStatusText.setEffect(dropShadow(Color.BLACK, 2));

        deckGroup.setOnMouseClicked(event -> {
            if (drawButton.isVisible()) {
                stopTakeCastigoTimer();
                actions.drawFromDeck(deckTopLeft());
                refresh();
            }
        });

        discardGroup.setOnMouseClicked(event -> {
            if (takeCastigoButton.isVisible() && castigoAvailable) {
                stopTakeCastigoTimer();
                actions.takeCastigo(discardTopLeft());
                refresh();
            }
        });

        buildDrawButton();
        buildTakeCastigoButton();
        buildPassButton();

        root.getChildren().addAll(
                title,
                deckGroup,
                discardGroup,
                deckCountText,
                discardStatusText,
                drawButton,
                takeCastigoButton,
                passButton
        );
    }

    private void buildDrawButton() {
        drawButton.getChildren().addAll(buttonBackground(Color.color(0.10, 0.25, 0.46)), buttonText("DRAW", 16));
        drawButton.setOnMouseClicked(event -> {
            stopTakeCastigoTimer();
            actions.drawFromDeck(deckTopLeft());
        });
    }

    private void buildTakeCastigoButton() {
        Rectangle background = buttonBackground(Color.color(0.48, 0.18, 0.10));
        takeCastigoTimerFill.setFill(Color.color(0.42, 0.42, 0.42, 0.68));
        takeCastigoTimerFill.setMouseTransparent(true);

        Text text = buttonText("CASTIGO", 15);
        takeCastigoButton.getChildren().addAll(background, takeCastigoTimerFill, text);
        takeCastigoButton.setOnMouseClicked(event -> {
            if (castigoAvailable) {
                stopTakeCastigoTimer();
                actions.takeCastigo(discardTopLeft());
            }
        });
    }

    private void buildPassButton() {
        passButton.getChildren().addAll(buttonBackground(Color.color(0.18, 0.18, 0.18)), buttonText("PASS", 16));
        passButton.setOnMouseClicked(event -> {
            stopTakeCastigoTimer();
            actions.passCastigo();
        });
    }

    private Rectangle buttonBackground(Color color) {
        Rectangle background = new Rectangle(BUTTON_WIDTH, BUTTON_HEIGHT);
        background.setArcWidth(10);
        background.setArcHeight(10);
        background.setFill(color);
        background.setStroke(Color.color(1, 1, 1, 0.22));
        background.setEffect(dropShadow(Color.BLACK, 3));
        return background;
    }

    private Text buttonText(String value, double fontSize) {
        Text text = overlayText(value, fontSize);
        text.setTranslateX((BUTTON_WIDTH - value.length() * fontSize * 0.55) / 2.0);
        text.setTranslateY(25);
        text.setMouseTransparent(true);
        return text;
    }

    private void position() {
        double totalWidth = CardViewMetrics.renderedWidth() * 2 + PILE_GAP;
        double x = hudArea.getMinX() + (hudArea.getWidth() - totalWidth) / 2.0;
        double y = hudArea.getMinY() + 655;

        root.setTranslateX(x);
        root.setTranslateY(y);

        deckGroup.setTranslateX(0);
        deckGroup.setTranslateY(30);

        discardGroup.setTranslateX(CardViewMetrics.renderedWidth() + PILE_GAP);
        discardGroup.setTranslateY(30);

        deckCountText.setTranslateX(deckGroup.getTranslateX() + 18);
        deckCountText.setTranslateY(deckGroup.getTranslateY() + CardViewMetrics.renderedHeight() - 18);

        discardStatusText.setTranslateX(discardGroup.getTranslateX() + 8);
        discardStatusText.setTranslateY(discardGroup.getTranslateY() + CardViewMetrics.renderedHeight() + 24);

        drawButton.setTranslateX(deckGroup.getTranslateX() - 10);
        drawButton.setTranslateY(deckGroup.getTranslateY() + CardViewMetrics.renderedHeight() + 42);

        takeCastigoButton.setTranslateX(discardGroup.getTranslateX() - 10);
        takeCastigoButton.setTranslateY(discardGroup.getTranslateY() + CardViewMetrics.renderedHeight() + 42);

        passButton.setTranslateX(discardGroup.getTranslateX() - 10);
        passButton.setTranslateY(discardGroup.getTranslateY() + CardViewMetrics.renderedHeight() + 88);
    }

    public void refresh() {
        rebuildDeckStack();
        rebuildDiscardPile();
        deckCountText.setText("Deck: " + deck.getCards().size());
        discardStatusText.setText(castigoAvailable ? "Castigo available" : "No castigo");
    }

    public void setCastigoAvailable(boolean castigoAvailable) {
        this.castigoAvailable = castigoAvailable;
        updateTakeCastigoOpacity();

        if (castigoAvailable && takeCastigoButton.isVisible()) {
            startTakeCastigoTimer();
        } else {
            stopTakeCastigoTimer();
        }

        refresh();
    }

    public void setTopDiscardCard(Card topDiscardCard) {
        this.topDiscardCard = topDiscardCard;
        refresh();
    }

    public void showActivePlayerControls(boolean castigoAvailable) {
        activePlayerDecisionMode = true;
        drawButton.setVisible(true);
        takeCastigoButton.setVisible(castigoAvailable);
        passButton.setVisible(false);
        setCastigoAvailable(castigoAvailable);
    }

    public void showOutOfTurnCastigoControls(boolean castigoAvailable) {
        activePlayerDecisionMode = false;
        drawButton.setVisible(false);
        takeCastigoButton.setVisible(castigoAvailable);
        passButton.setVisible(true);
        setCastigoAvailable(castigoAvailable);
    }

    public void hideDecisionControls() {
        drawButton.setVisible(false);
        takeCastigoButton.setVisible(false);
        passButton.setVisible(false);
        stopTakeCastigoTimer();
    }

    private void updateTakeCastigoOpacity() {
        takeCastigoButton.setOpacity(castigoAvailable ? 1.0 : 0.42);
    }

    private void startTakeCastigoTimer() {
        stopTakeCastigoTimer();

        takeCastigoTimerFill.setWidth(0);

        takeCastigoTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(takeCastigoTimerFill.widthProperty(), 0)),
                new KeyFrame(Duration.seconds(DECISION_SECONDS), new KeyValue(takeCastigoTimerFill.widthProperty(), BUTTON_WIDTH))
        );

        takeCastigoTimeline.setOnFinished(event -> {
            if (activePlayerDecisionMode) {
                actions.drawFromDeck(deckTopLeft());
            } else {
                actions.passCastigo();
            }
        });
        takeCastigoTimeline.play();
    }

    private void stopTakeCastigoTimer() {
        if (takeCastigoTimeline != null) {
            takeCastigoTimeline.stop();
            takeCastigoTimeline = null;
        }

        takeCastigoTimerFill.setWidth(0);
    }

    private void rebuildDeckStack() {
        deckGroup.getChildren().clear();

        int visibleBacks = visibleDeckBackCount();

        Group baseSlot = emptyPileSlot(visibleBacks == 0 ? "EMPTY" : "");
        baseSlot.setOpacity(0.55);
        deckGroup.getChildren().add(baseSlot);

        if (visibleBacks == 0) {
            return;
        }

        for (int i = 0; i < visibleBacks; i++) {
            Node back = cardBackViewFactory.createCardBackView();
            back.setTranslateX(i * CARD_OFFSET);
            back.setTranslateY(-i * CARD_OFFSET);
            back.setOpacity(1.0);

            if (i < visibleBacks - 1) {
                back.setEffect(deckDepthEffect(i, visibleBacks));
            }

            deckGroup.getChildren().add(back);
        }
    }

    private void rebuildDiscardPile() {
        discardGroup.getChildren().clear();

        if (topDiscardCard == null) {
            Group empty = emptyPileSlot("DISCARD");
            empty.setOpacity(0.42);
            discardGroup.getChildren().add(empty);
            return;
        }

        Node discardCard = cardViewFactory.createView(topDiscardCard);
        discardCard.setOpacity(castigoAvailable ? 1.0 : 0.45);
        discardGroup.getChildren().add(discardCard);
    }

    private ColorAdjust deckDepthEffect(int stackIndex, int visibleBacks) {
        int distanceFromTop = visibleBacks - 1 - stackIndex;
        double brightness = -Math.min(0.18 + distanceFromTop * 0.10, 0.58);

        ColorAdjust adjust = new ColorAdjust();
        adjust.setBrightness(brightness);
        adjust.setSaturation(-0.08);
        return adjust;
    }

    private int visibleDeckBackCount() {
        int count = deck.getCards().size();

        if (count <= 0) {
            return 0;
        }

        if (count <= 10) {
            return 1;
        }

        if (count <= 30) {
            return 2;
        }

        if (count <= 60) {
            return 3;
        }

        if (count <= 100) {
            return 4;
        }

        return MAX_VISIBLE_DECK_BACKS;
    }

    private Group emptyPileSlot(String label) {
        Group group = new Group();

        Rectangle slot = new Rectangle(CardViewMetrics.renderedWidth(), CardViewMetrics.renderedHeight());
        slot.setArcWidth(20);
        slot.setArcHeight(20);
        slot.setFill(Color.color(0.04, 0.04, 0.04, 0.32));
        slot.setStroke(Color.color(1, 1, 1, 0.35));
        slot.setStrokeWidth(2);
        slot.setEffect(dropShadow(Color.BLACK, 4));

        group.getChildren().add(slot);

        if (label != null && !label.isBlank()) {
            Text text = overlayText(label, 15);
            double approximateTextWidth = label.length() * 9.0;
            text.setTranslateX((CardViewMetrics.renderedWidth() - approximateTextWidth) / 2.0);
            text.setTranslateY(CardViewMetrics.renderedHeight() / 2.0 + 8);
            group.getChildren().add(text);
        }

        return group;
    }

    private Text overlayText(String value, double fontSize) {
        Text text = new Text(value);
        text.setFont(loadFont(fontSize));
        text.setFill(Color.WHITE);
        text.setEffect(dropShadow(Color.BLACK, 3));
        return text;
    }

    private Point2D deckTopLeft() {
        return new Point2D(
                root.getTranslateX() + deckGroup.getTranslateX(),
                root.getTranslateY() + deckGroup.getTranslateY()
        );
    }

    public Point2D discardTopLeft() {
        return new Point2D(
                root.getTranslateX() + discardGroup.getTranslateX(),
                root.getTranslateY() + discardGroup.getTranslateY()
        );
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
