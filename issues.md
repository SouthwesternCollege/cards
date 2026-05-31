# Issue Tracker

This file tracks architectural, product, and implementation issues discovered during development.

## Status Labels

- `Open`: not started.
- `In Progress`: currently being addressed.
- `Blocked`: waiting on a domain/design decision.
- `Done`: completed.
- `Deferred`: intentionally postponed.

## Priority Labels

- `P0`: blocks core progress or causes broken builds.
- `P1`: important architectural or gameplay issue.
- `P2`: useful improvement, not urgent.
- `P3`: polish or cleanup.

---

## ISS-001: Remove FXGL ownership from domain card model

Status: Done  
Priority: P0  
Area: Architecture

### Problem

The original `Card` class stored an FXGL `Entity`, coupling the domain model to the rendering/game-world layer.

### Decision

`Card` should be a pure domain object. FXGL entities should reference cards through `CardComponent` or another presentation-layer mapping.

### Result

`Card` no longer owns an entity.

---

## ISS-002: Replace `cardIndex` with explicit rank/suit/joker model

Status: Done  
Priority: P0  
Area: Domain Model

### Problem

The original implementation inferred card meaning from an integer index. This made rules depend on hidden ordering assumptions.

### Decision

Use explicit `Rank`, `Suit`, and joker status.

### Result

`Card.standard(...)` and `Card.joker(...)` construct cards explicitly.

---

## ISS-003: Add stable card identity for save states

Status: Done  
Priority: P0  
Area: Save/Load

### Problem

Object references are not stable across save/load. Multiple identical card faces need distinct physical identities.

### Decision

Use `CardId` as a stable identity value.

### Result

`Card` now has a `CardId`.

---

## ISS-004: Remove `deckNumber`

Status: Done  
Priority: P1  
Area: Domain Model

### Problem

`deckNumber` was a weak approximation of card identity and added unnecessary complexity.

### Decision

Use `CardId` instead. Do not model deck provenance unless a domain rule requires it later.

---

## ISS-005: Define joker/wild-card rules

Status: Done  
Priority: P1  
Area: Game Rules

### Decision

Known La Kika joker rules:

- Jokers may represent any rank or suit.
- Jokers may not make up more than half of a meld.
- Jokers may not be consecutive in a straight flush.
- Jokers may be stolen from melds by replacement.
- More than one joker may be stolen during a single turn.
- A stolen joker must be played during the same turn.
- A stolen joker may be used to create a new meld or mutate any meld in the play area.
- If a closed player steals a joker, they must open during that same turn.
- Jokers are worth 50 points in hand scoring.

### Remaining Design Work

Implementation still needs a joker subsystem to model joker assignments, stealing, replacement, mandatory same-turn use, and return behavior if the obligation is not satisfied.

---

## ISS-006: Split `Hand` into domain and FXGL responsibilities

Status: In Progress  
Priority: P1  
Area: Architecture

### Problem

`Hand` has historically mixed card collection, selection, FXGL spawning, entity mapping, layout, animation, and play behavior.

### Progress

Milestone 4A introduced:

```text
HandModel
HandLayout
CardLayoutSlot
CardEntityRegistry
```

The current `Hand` class remains a transitional facade.

### Remaining Work

Split further into:

```text
HandModel          // pure domain/hand state
HandView           // FXGL rendering/layout
HandController     // input and coordination
```

---

## ISS-007: Remove static/global HUD updates

Status: Open  
Priority: P2  
Area: UI Architecture

### Problem

`GameHUD` currently uses static/global update patterns.

### Progress

Milestone 4A.2 introduced `SelectionFeedback` and `HudMeldSelectionFeedback` as a transitional decoupling step.

### Proposed Direction

Use observer/event/property updates from game state to HUD.

### Milestone 4B Note

Milestone 4B introduced a better all-player HUD layout, but it did not fully remove static/global HUD access. The selected-meld feedback path still uses static update methods as a transitional compatibility layer.

---

## ISS-008: Replace poker-hand evaluator with La Kika meld validation

Status: Done  
Priority: P1  
Area: Game Rules

### Decision

Only two meld families are valid:

- Three-of-a-kind or more.
- Straight flush.

No other poker hands are legal.

### Result

La Kika selection feedback now uses domain-specific meld validation instead of `PokerHandEvaluator`.

Implemented concepts include:

```text
MeldValidator
LaKikaMeldValidator
KindMeldValidator
StraightFlushMeldValidator
JokerRules
MeldValidationError
MeldValidationResult
MeldInterpretation
JokerAssignment
```

`PokerHandEvaluator` may remain temporarily as a possible future generic-engine utility, but it should not drive La Kika gameplay.

---

## ISS-009: Define round lifecycle

Status: Done  
Priority: P1  
Area: Game Rules

### Decision

Known round lifecycle:

1. Dealer rotates according to turn order.
2. Dealer shuffles and prepares the deal.
3. Deal 13 cards to each player, one at a time, starting with the player next to the dealer.
4. Apply the exact deal bonus if the dealer prepared exactly the required number of cards.
5. Players take turns.
6. Each player must satisfy the round opening requirement before freely playing or mutating melds.
7. A round ends when a player discards their final card.
8. Remaining players score cards left in hand.
9. Game ends after six rounds.

---

## ISS-010: Add unit tests for core card model

Status: Open  
Priority: P2  
Area: Testing

### Proposed Tests

- Standard card requires rank and suit.
- Joker cannot have rank/suit.
- Card IDs must be positive.
- Card snapshot round-trips correctly.
- Deck creates expected number of cards.
- Multiple identical card faces receive different IDs.

---

## ISS-011: Decide save file format

Status: Open  
Priority: P2  
Area: Save/Load

### Recommendation

Use human-readable JSON while the architecture is evolving.

---

## ISS-012: Package structure needs eventual engine/application split

Status: Deferred  
Priority: P2  
Area: Architecture

### Proposed Future Direction

Possible package split:

```text
quetzal.cards.engine.model
quetzal.cards.engine.rules
quetzal.cards.engine.state
quetzal.cards.engine.snapshot
quetzal.cards.fxgl
quetzal.cards.game
```

---

## ISS-013: Implement castigo

Status: Open  
Priority: P1  
Area: Game Rules

### Known Rules

- A player may take only the most recently discarded card.
- The player taking castigo must also draw three additional cards from the deck.
- Only one castigo may occur per turn.
- If the active player declines, other players may accept in turn order.
- A player who has not opened may take castigo.
- Each player has 10 castigos per game.
- Taking a castigo consumes one castigo.
- Declining a castigo consumes none.
- Castigos reset between games, but not between rounds.
- If the deck has fewer than three cards, add a new shuffled standard deck with jokers to the game deck.
- Each added standard deck includes two jokers.

### Proposed Direction

Track castigos remaining in player/game state, not in meld validation.

---

## ISS-014: Implement opening requirements

Status: Open  
Priority: P1  
Area: Game Rules

### Requirements

1. Round 1: one three-of-a-kind.
2. Round 2: two three-of-a-kind melds.
3. Round 3: one four-of-a-kind.
4. Round 4: two four-of-a-kind melds.
5. Round 5: one five-of-a-kind.
6. Round 6: one straight flush of eight cards.

### Additional Rules

- Opening melds may include jokers if legal.
- Round 6's eight-card straight flush may include jokers if legal.
- Opening does not consume the whole turn.
- A player may continue creating or mutating melds after opening.
- If a closed player steals a joker, they must open during that same turn.

---

## ISS-015: Model play area and meld mutation

Status: Open  
Priority: P1  
Area: Domain Model

### Known Rules

- Melds are not permanently owned by players.
- A meld remains visually associated with its creator.
- A player may add matching ranks to a kind meld.
- A player may extend the beginning or end of a straight flush.
- A player may steal jokers under specific constraints.
- A stolen joker may be used to create a new meld or mutate any meld in the play area.

### Proposed Direction

Create or refine explicit domain/presentation types:

```text
PlayArea
Meld
MeldId
Move
MoveResult
LegalMoveValidator
MeldLayout
PlayAreaView
```

---

## ISS-016: Remove five-card selection assumption

Status: Done  
Priority: P1  
Area: UI / Domain Interaction

### Problem

The prototype had poker-influenced selection behavior.

### Result

Selection now supports arbitrary selected-card counts for La Kika meld construction.

---

## ISS-017: Define final-card round-ending rule

Status: Done  
Priority: P1  
Area: Game Rules

### Decision

- The final card should be discarded.
- The final card does not need to be played into a meld.
- If a player can play the final card during the meld phase, they should still discard the final card.
- Discard is not skipped simply because the player could otherwise play all cards.

---

## ISS-018: Define deck exhaustion behavior

Status: Done  
Priority: P1  
Area: Game Rules

### Decision

If the deck cannot satisfy a normal draw or castigo draw, add a new shuffled standard deck with jokers to the game deck.

Each added standard deck includes two jokers.

---

## ISS-019: Implement dealer rotation

Status: Open  
Priority: P1  
Area: Game Rules

### Known Rules

- Players take turns being dealer according to turn order.
- Dealer shuffles the deck at the beginning of a round.
- Dealer takes a deal packet from the top of the deck all at once.
- Cards are dealt one at a time starting with the player next to the dealer.
- The player next to the dealer takes the first turn after the deal.
- If the deal packet is short, remaining cards are dealt directly from the deck.
- If the deal packet is long, extra cards are returned to the top of the deck.

---

## ISS-020: Implement exact deal bonus

Status: Open  
Priority: P1  
Area: Scoring

### Known Rule

If the dealer takes exactly:

```text
hand size * number of players
```

cards for the deal, subtract 100 points from the dealer's score immediately at the beginning of the round.

### Digital Design Direction

- Use a shot/swing-meter style interaction.
- Use linear interpolation between the top and bottom of the deck for now.
- Do not display the exact number of selected cards on the meter.
- A later version may animate the deck splitting.
- The domain logic should receive the resulting count, not depend on the UI meter.

---

## ISS-021: Determine initial deck composition by player count

Status: Open  
Priority: P1  
Area: Game Setup

### Current Domain Knowledge

- The game starts with two standard decks plus jokers.
- Each standard deck contributes two jokers.
- Additional decks are added when necessary.
- Raul will consult other game experts to determine whether later rounds/player counts should start with more than two decks.

---

## ISS-022: Model closed-player joker stealing rule

Status: Open  
Priority: P1  
Area: Game Rules

### Problem

A player who has not opened may steal a joker, but must open that same turn.

### Proposed Direction

Model turn obligations explicitly:

```text
TurnObligation
MustOpenThisTurn
MustUseStolenJoker
```

---

## ISS-023: Implement stolen joker obligations

Status: Open  
Priority: P1  
Area: Game Rules

### Known Rules

- A player may steal more than one joker in a turn.
- A stolen joker may be used to create a new meld or mutate any meld in the play area.
- A stolen joker must be played during the same turn.
- If the stolen joker is not played during the same turn, it must be returned.
- If a closed player steals a joker, they must open that turn.

### Proposed Direction

Represent this with a domain object such as:

```java
public record StolenJokerObligation(
    PlayerId playerId,
    CardId jokerId,
    MeldId sourceMeldId
) {}
```

---

## ISS-024: Complete Milestone 2 domain model types

Status: Done  
Priority: P1  
Area: Domain Model

### Result

Initial domain types were added for players, melds, play area, moves, turn phases, opening requirements, validation results, joker assignments, and stolen joker obligations.

---

## ISS-025: Decide whether straight-flush joker placement should become position-sensitive

Status: Deferred  
Priority: P2  
Area: Game Rules / UI Interaction

### Decision

Milestone 3 validation accepts unordered straight-flush selections and chooses the lowest possible valid sequence when joker placement is ambiguous.

### Current Boundary

- Validation determines whether the selected cards can structurally form a straight flush.
- Presentation/meld placement arranges a played straight flush in normalized sequence order.
- If position-sensitive joker placement becomes important later, update the validation interface to accept explicit placement/order intent from the UI.

---

## ISS-026: Preserve layout debug overlay during UI refactor

Status: Open  
Priority: P3  
Area: UI / Developer Tooling

### Problem

The debug layout overlay is useful for verifying dedicated scene regions.

### Proposed Direction

Keep the debug overlay available while layout and animation behavior are being refactored.

---

## ISS-027: Played meld layout and centering

Status: In Progress  
Priority: P2  
Area: UI / Presentation

### Problem

Played cards initially animated to the wrong region and then later centered as one meld, causing subsequent melds to merge visually.

### Progress

Milestone 4A/4A.2 introduced `PlayedMeldLayout`, centered played melds, and separate visual groups for played melds.

### Remaining Work

This is still transitional. Milestone 4C should implement robust multi-meld layout:

- Creator grouping.
- Wrapping.
- Compression.
- Minimum spacing.
- Opponent carousel.
- Full play-area view support.

---

## ISS-028: Implement hot-seat hand privacy

Status: Open  
Priority: P2  
Area: UI / Game Flow

### Decision

Short term:

- Use a debug overlay to reveal hidden hands during development.

Long term:

- Add a pass-device screen between turns.

Normal gameplay should show only the active player's hand.

---

## ISS-029: Normalize straight flush display order

Status: In Progress  
Priority: P1  
Area: Presentation / Meld Layout

### Decision

Players may select straight-flush cards in any order, but once played, the meld should display in sequence order.

Kind melds preserve selected/insertion order.

### Current Status

Milestone 4A/4A.2 began using validation-normalized order for played straight flushes.

---

## ISS-030: Add pass-device screen later

Status: Deferred  
Priority: P2  
Area: UI / Game Flow

### Proposed Direction

After the core presentation architecture is stable, add a screen:

```text
Pass device to Player N
Continue
```

This should hide hand information until the next player confirms.

---

## ISS-031: Fix card interaction disable rotation API

Status: Done  
Priority: P2  
Area: FXGL / UI

### Problem

A patch used the wrong FXGL rotation method when disabling interaction on played cards.

### Correct Project Code

Use:

```java
if (!interactionEnabled) {
    isDragging = false;
    entity.setRotation(0.0);
}
```

Avoid `setRotate(...)`.

---

## ISS-032: Improve wiggle animation continuity

Status: Open  
Priority: P2  
Area: UI / Animation

### Problem

When hover-intensified wiggle begins, the card jumps to the hover animation's starting angle instead of continuing smoothly from the current angle.

### Proposed Direction

Use a stateful animation component where hover changes amplitude/frequency without resetting phase.

---

## ISS-033: Feed HUD from real game state

Status: In Progress  
Priority: P1  
Area: UI / Game State Integration

### Problem

Milestone 4B introduced a prototype all-player HUD. Milestone 4B.2 added a `PlayerHudModel` and `GameHudController`, but the HUD still cannot be fully real until the full game-state layer exists.

### Current Prototype

The HUD displays:

- Player name.
- Cumulative score.
- Cards remaining.
- Castigos remaining.
- Opened/closed visual state.
- Dealer chip.
- Active-turn arrow.

### Proposed Direction

After `GameState`, `PlayerState`, dealer rotation, castigo tracking, and turn state exist, the HUD should be updated from real state rather than hardcoded prototype data.

Possible future flow:

```text
GameState changes
-> UI/game-state adapter publishes PlayerHudState list
-> GameHUD renders rows
```

This keeps the HUD presentation-facing and avoids making UI nodes depend directly on mutable domain internals.

---

## ISS-034: Create UI theme module

Status: Open  
Priority: P2  
Area: UI / Presentation

### Problem

Font loading, drop shadows, button styling, and HUD styling are still scattered across UI classes.

### Desired Direction

Create a small presentation utility/module for shared UI styling.

Possible concept:

```java
public final class UITheme {
    public static Font font(double size);
    public static DropShadow dropShadow(Color color, double offsetY);
    public static void applyButtonStyle(Button button, Color color);
}
```

### Reason

The project should consistently use DePixelHalbfett, drop shadows, borderless buttons, and matching control styles without duplicating style code in every UI class.

---

## ISS-035: Replace prototype HUD player data with GameState adapter

Status: Open  
Priority: P1  
Area: UI / Game State Integration

### Problem

`PlayerHudModel` is currently presentation-facing prototype state. It is useful for Milestone 4, but it is not the final source of truth.

### Decision

Keep `PlayerHudModel` as a HUD view model, but later populate it from real game state.

### Proposed Direction

Introduce a mapper/adapter after the game-state model exists:

```text
GameState / PlayerState
    -> PlayerHudState
    -> PlayerHudModel
    -> GameHudController
    -> GameHUD
```

This keeps `GameHUD` from depending directly on mutable domain internals.

---

## ISS-036: Introduce visual meld grouping and reflow layout

Status: In Progress  
Priority: P1  
Area: UI / Presentation

### Problem

The previous played-meld layout centered newly played cards, but it did not model played melds as first-class visual groups. This made it difficult to support creation order, wrapping, compression, reflow, opponent carousel behavior, and future full-table view.

### Decision

Introduce presentation-layer visual meld concepts:

```text
VisualMeld
VisualMeldStore
MeldLayout
MeldLayoutSlot
```

### Current Rules

- Melds are visually grouped by original creator.
- Melds are laid out left-to-right in creation order.
- Card spacing and meld gaps compress before wrapping.
- Minimum compressed card spacing is 20% of card width.
- Melds wrap before card shrinking.
- Existing melds may reflow when a new meld is added.
- Played cards animate to the final reflowed layout positions.
- Card movement uses a 0.1-second stagger between cards.
- Hand and meld layouts should be vertically centered inside their assigned regions.

### Future Work

This is still a presentation-layer bridge. Later, visual melds should be driven by domain `PlayArea` / `GameState` rather than the transitional `Hand` facade.


---

## ISS-037: Add meld layout debug harness

Status: Done  
Priority: P2  
Area: UI / Developer Tooling

### Problem

The played-meld layout cannot be stress-tested organically yet because draw, discard, turn flow, and full game-state systems are not implemented.

### Decision

Add development-only controls that generate visual melds directly in the player meld area.

### Result

Milestone 4C.2 adds controls for:

- Adding a test kind meld.
- Adding a test straight flush meld.
- Adding a batch of test melds for stress-testing wrapping and centering.
- Clearing generated visual melds.

### Constraint

These controls are not gameplay features and should remain presentation/debug tooling only.

### Future Direction

Once real game-state-driven play-area testing exists, these controls should either be hidden behind a debug mode or removed.

---

## ISS-038: Tune card spacing and vertical centering

Status: Done  
Priority: P2  
Area: UI / Presentation

### Problem

Compressed card spacing in played melds was too small, and hand/meld layouts were horizontally centered but not consistently vertically centered in their assigned areas.

### Decision

- Minimum compressed card spacing should be 20% of card width.
- Hand cards should be vertically centered within the player hand area.
- Played meld rows should remain vertically centered within the player meld area.

### Result

Milestone 4C.2 cleanup updates the layout constants and hand layout positioning.

---

## ISS-039: Add configurable animation settings

Status: Open  
Priority: P2  
Area: Settings / Presentation

### Problem

Animation timing is currently a code-level default. The final game should allow players to configure animation speed.

### Current Decision

Played-card animation currently uses a 0.1-second stagger between cards.

### Proposed Direction

Centralize animation defaults now, then later expose them through a settings screen.

Possible future settings:

```text
Animation speed: Slow / Normal / Fast / Instant
Card movement speed
Card stagger timing
Wiggle intensity
```

---

## ISS-040: Support house-rule configuration

Status: Open  
Priority: P2  
Area: Game Rules / Settings

### Problem

La Kika has minor house-rule variations. Hardcoding every rule would make the game less flexible and harder to adapt.

### Known Example

Default castigo rule:

- Take the most recent discard.
- Draw three additional cards from the deck.

Possible house rule:

- If taking castigo on your own turn, draw three cards total including the discard.
- If taking castigo when it is not your turn, draw four cards total.

### Proposed Direction

Introduce a future rules configuration object, such as:

```java
public record LaKikaRulesConfig(
    int castigoDrawCountOnTurn,
    int castigoDrawCountOffTurn
) {}
```

Gameplay services should depend on explicit configuration rather than scattered constants.

---

## ISS-035: Replace card texture scale transforms with fitted card views

Status: Done  
Priority: P1  
Area: UI / Presentation Architecture

### Problem

Scaling card texture nodes with `setScaleX` / `setScaleY` can make visual bounds differ from the entity position used by layout code.

This contributed to persistent vertical-centering confusion.

### Decision

Use a `CardViewFactory` that creates card visual nodes at their final rendered size using fitted `ImageView` nodes.

### Result

`CardComponent` no longer scales child textures directly.

The intended invariant is now:

```text
Entity position = top-left corner of the rendered card.
```

---

## ISS-036: Keep PRD milestone structure synchronized

Status: Done  
Priority: P2  
Area: Documentation

### Problem

The PRD milestone list had fallen out of sync with actual development. Several Milestone 4 sub-milestones were missing, and `UI-17` had been appended outside the intended UI requirements section.

### Decision

Expand Milestone 4 into explicit sub-milestones and move `UI-17` into the UI requirements section.

### Result

The PRD now includes:

- Milestone 4A
- Milestone 4A.2
- Milestone 4B
- Milestone 4B.2
- Milestone 4C
- Milestone 4C.2
- Milestone 4D
- Milestone 4E
- Milestone 4F
- Milestone 4G
- Milestone 4H

The PRD also includes updated future milestones for game state, round rules, scoring, save/load, settings/house rules, and tests.
