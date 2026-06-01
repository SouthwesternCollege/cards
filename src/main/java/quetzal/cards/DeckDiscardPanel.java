package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
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

/**
 * Prototype HUD panel for deck/discard interactions.
 *
 * Milestone 4D scope:
 * - Show deck to the left of discard.
 * - Show a Balatro-style deck stack using small offsets.
 * - Provide clickable text overlays for prototype draw/castigo actions.
 * - Keep real turn legality out of this class.
 */
public final class DeckDiscardPanel {

    private static final double CARD_OFFSET = 2.0;
    private static final int MAX_VISIBLE_DECK_BACKS = 5;
    private static final double PILE_GAP = 28.0;

    private final Deck deck;
    private final DeckDiscardActions actions;
    private final Rectangle2D hudArea;
    private final CardBackViewFactory cardBackViewFactory = new CardBackViewFactory();

    private final Group root = new Group();
    private final Group deckGroup = new Group();
    private final Group discardGroup = new Group();
    private final Text deckCountText = new Text();
    private final Text discardStatusText = new Text();

    private boolean castigoAvailable = false;

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
            actions.drawFromDeck(deckTopLeft());
            refresh();
        });

        discardGroup.setOnMouseClicked(event -> {
            if (castigoAvailable) {
                actions.takeCastigo(discardTopLeft());
            }
            refresh();
        });

        root.getChildren().addAll(title, deckGroup, discardGroup, deckCountText, discardStatusText);
    }

    private void position() {
        double totalWidth = CardViewMetrics.renderedWidth() * 2 + PILE_GAP;
        double x = hudArea.getMinX() + (hudArea.getWidth() - totalWidth) / 2.0;
        double y = hudArea.getMinY() + 655;

        root.setTranslateX(x);
        root.setTranslateY(y);

        // Deck is left; discard is right.
        deckGroup.setTranslateX(0);
        deckGroup.setTranslateY(30);

        discardGroup.setTranslateX(CardViewMetrics.renderedWidth() + PILE_GAP);
        discardGroup.setTranslateY(30);

        deckCountText.setTranslateX(deckGroup.getTranslateX() + 18);
        deckCountText.setTranslateY(deckGroup.getTranslateY() + CardViewMetrics.renderedHeight() - 18);

        discardStatusText.setTranslateX(discardGroup.getTranslateX() + 10);
        discardStatusText.setTranslateY(discardGroup.getTranslateY() + CardViewMetrics.renderedHeight() + 24);
    }

    public void refresh() {
        rebuildDeckStack();
        rebuildDiscardPile();
        deckCountText.setText("Deck: " + deck.getCards().size());
        discardStatusText.setText(castigoAvailable ? "CASTIGO" : "No castigo");
    }

    public void setCastigoAvailable(boolean castigoAvailable) {
        this.castigoAvailable = castigoAvailable;
        refresh();
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

        // Bottom card is centered over the empty slot. Subsequent cards grow
        // upward and to the right.
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

        deckGroup.getChildren().add(drawButtonOverlay());
    }

    private void rebuildDiscardPile() {
        discardGroup.getChildren().clear();

        if (!castigoAvailable) {
            Group empty = emptyPileSlot("DISCARD");
            empty.setOpacity(0.42);
            discardGroup.getChildren().add(empty);
            return;
        }

        Node back = cardBackViewFactory.createCardBackView();
        discardGroup.getChildren().add(back);

        Text overlay = overlayText("CASTIGO", 15);
        overlay.setTranslateX(12);
        overlay.setTranslateY(CardViewMetrics.renderedHeight() / 2.0 + 8);
        discardGroup.getChildren().add(overlay);
    }

    private ColorAdjust deckDepthEffect(int stackIndex, int visibleBacks) {
        int distanceFromTop = visibleBacks - 1 - stackIndex;
        double brightness = -Math.min(0.18 + distanceFromTop * 0.10, 0.58);

        ColorAdjust adjust = new ColorAdjust();
        adjust.setBrightness(brightness);
        adjust.setSaturation(-0.08);
        return adjust;
    }

    private Group drawButtonOverlay() {
        Group button = new Group();

        double buttonWidth = 86;
        double buttonHeight = 34;

        Rectangle background = new Rectangle(buttonWidth, buttonHeight);
        background.setArcWidth(10);
        background.setArcHeight(10);
        background.setFill(Color.color(0.0, 0.0, 0.0, 0.5));
        background.setStroke(Color.color(1, 1, 1, 0.22));
        Text text = overlayText("DRAW", 16);
        text.setTranslateX(15);
        text.setTranslateY(24);

        button.getChildren().addAll(background, text);
        double topCardOffset = (visibleDeckBackCount() - 1) * CARD_OFFSET;
        button.setTranslateX(topCardOffset + (CardViewMetrics.renderedWidth() - buttonWidth) / 2.0);
        button.setTranslateY(-topCardOffset + (CardViewMetrics.renderedHeight() - buttonHeight) / 2.0);

        return button;
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

    private Point2D discardTopLeft() {
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
