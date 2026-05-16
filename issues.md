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

`Card.standard(...)` and `Card.joker(...)` now construct cards explicitly.

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

### Problem

Jokers were known to be wild, but exact behavior was initially undefined.

### Decision

Known La Kika joker rules:

- Jokers may represent any rank or suit.
- Jokers may not make up more than half of a meld.
- Jokers may not be consecutive in a straight flush.
- Jokers may be stolen from melds by replacement.
- More than one joker may be stolen during a single turn.
- A joker stolen from a meld must be played during the same turn.
- A stolen joker may be used to create a new meld or mutate any meld in the play area.
- Jokers are worth 50 points in hand scoring.

### Remaining Design Work

Implementation still needs a joker subsystem to model:

- Joker assignments.
- Joker replacement.
- Joker stealing.
- Mandatory same-turn use.
- Closed-player joker stealing requiring opening during the same turn.

---

## ISS-006: Split `Hand` into domain and FXGL responsibilities

Status: Open  
Priority: P1  
Area: Architecture

### Problem

`Hand` currently manages card collection, deck drawing, selection, FXGL spawning, entity mapping, layout, animation, and play behavior.

### Risk

This makes the class difficult to test, reuse, or evolve into engine code.

### Proposed Direction

Split into:

```text
Hand / HandModel          // pure domain collection and selection rules
HandView / HandController // FXGL spawning, layout, animation, input coordination
```

---

## ISS-007: Remove static/global HUD updates

Status: Open  
Priority: P2  
Area: UI Architecture

### Problem

`GameHUD` currently uses static/global update patterns.

### Risk

This will make multiplayer, testing, save/load, and multiple UI states harder.

### Proposed Direction

Use observer/event/property updates from game state to HUD.

---

## ISS-008: Replace poker-hand evaluator with La Kika meld validation

Status: Open  
Priority: P1  
Area: Game Rules

### Problem

Current code evaluates poker-like hands, but La Kika does not use ordinary poker hands.

### Decision

Only two meld families are valid:

- Three-of-a-kind or more.
- Straight flush.

No other poker hands are legal.

### Proposed Direction

Replace `PokerHandEvaluator` with domain-specific validation such as:

```text
MeldValidator
KindMeldValidator
StraightFlushMeldValidator
JokerConstraintValidator
```

---

## ISS-009: Define round lifecycle

Status: Done  
Priority: P1  
Area: Game Rules

### Problem

The game is round-based, but the lifecycle was not formally modeled.

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

### Remaining Design Work

Need to model dealer preparation and exact deal behavior.

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

### Reason

JSON is inspectable, debuggable, and compatible with explicit snapshot records.

---

## ISS-012: Package structure needs eventual engine/application split

Status: Deferred  
Priority: P2  
Area: Architecture

### Problem

All source currently lives under `quetzal.cards`.

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

### Reason for Deferral

Do not reorganize packages until the core module boundaries are clearer.

---

## ISS-013: Implement castigo

Status: Open  
Priority: P1  
Area: Game Rules

### Problem

Castigo is a central La Kika rule and is not currently modeled.

### Known Rules

- A player may take only the most recently discarded card.
- The player taking castigo must also draw three additional cards from the deck.
- Only one castigo may occur per turn.
- If the active player declines, other players may accept in turn order.
- A player who has not opened may take castigo.
- If the deck has fewer than three cards, add a new shuffled standard deck with jokers to the game deck.

### Remaining Design Question

- How many jokers should be included when adding a new deck?

---

## ISS-014: Implement opening requirements

Status: Open  
Priority: P1  
Area: Game Rules

### Problem

Each round has a different opening requirement that gates whether a player may freely play or mutate melds.

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

### Design Direction

Represent this as a rule object rather than scattered conditionals.

Possible interface:

```java
public interface OpeningRequirement {
    boolean isSatisfiedBy(List<Meld> newlyPlayedMelds);
}
```

---

## ISS-015: Model play area and meld mutation

Status: Open  
Priority: P1  
Area: Domain Model

### Problem

Melds remain mutable in the play area, and any player may mutate any meld.

### Known Rules

- Melds are not permanently owned by players.
- A player may add matching ranks to a kind meld.
- A player may extend the beginning or end of a straight flush.
- A player may steal jokers under specific constraints.
- A stolen joker may be used to create a new meld or mutate any meld in the play area.

### Proposed Direction

Create explicit domain types:

```text
PlayArea
Meld
MeldId
Move
MoveResult
LegalMoveValidator
```

---

## ISS-016: Remove five-card selection assumption

Status: Open  
Priority: P1  
Area: UI / Domain Interaction

### Problem

The current prototype appears influenced by poker-hand selection, likely assuming five selected cards.

### Risk

La Kika melds may contain more than five cards, especially for later opening requirements and extended melds.

### Proposed Direction

Selection should support arbitrary legal move construction, not fixed-size poker evaluation.

---

## ISS-017: Define final-card round-ending rule

Status: Done  
Priority: P1  
Area: Game Rules

### Problem

A round ends when a player empties their hand, but the exact method needed clarification.

### Decision

- The final card should be discarded.
- The final card does not need to be played into a meld.
- If a player can play the final card during the meld phase, they should still discard the final card.
- Discard is not skipped simply because the player could otherwise play all cards.

### Implementation Impact

The turn model must always include a discard phase unless a future explicit rule says otherwise.

---

## ISS-018: Define deck exhaustion behavior

Status: Done  
Priority: P1  
Area: Game Rules

### Problem

Draw and castigo require cards from the deck.

### Decision

If the deck cannot satisfy a normal draw or castigo draw, add a new shuffled standard deck with jokers to the game deck.

### Alternative Considered

Shuffle the discard pile into the deck if the discard pile is large enough.

### Remaining Design Question

- How many jokers should be included when adding a new deck?

---

## ISS-019: Implement dealer rotation

Status: Open  
Priority: P1  
Area: Game Rules

### Problem

Dealer responsibility affects dealing and scoring, so it must be modeled in the domain layer.

### Known Rules

- Players take turns being dealer according to turn order.
- Dealer shuffles the deck at the beginning of a round.
- Dealer takes a deal packet from the top of the deck all at once.
- Cards are dealt one at a time starting with the player next to the dealer.
- The player next to the dealer takes the first turn after the deal.
- If the deal packet is short, remaining cards are dealt directly from the deck.
- If the deal packet is long, extra cards are returned to the top of the deck.

### Proposed Direction

Introduce domain concepts such as:

```text
DealerRotation
DealOrder
DealService
```

---

## ISS-020: Implement exact deal bonus

Status: Open  
Priority: P1  
Area: Scoring

### Problem

The exact deal mechanic affects scoring and allows negative scores.

### Known Rule

If the dealer takes exactly:

```text
hand size * number of players
```

cards for the deal, subtract 100 points from the dealer's score immediately at the beginning of the round.

### Additional Rules

- Negative scores are possible.
- If the dealer takes too few cards, the remaining cards are dealt directly from the deck and the dealer's score is unaffected.
- If the dealer takes too many cards, the extra cards are returned to the top of the deck and the dealer's score is unaffected.

### Digital Design Direction

- Use a shot/swing-meter style interaction.
- The dealer stops the meter.
- The stopped meter position determines the number of cards taken for the deal packet.
- The domain logic should receive the resulting count, not depend on the UI meter.

---

## ISS-021: Determine initial deck composition by player count

Status: Open  
Priority: P1  
Area: Game Setup

### Problem

The game supports multiple standard decks and jokers, but the exact starting deck composition is not yet fully defined by player count and round.

### Current Domain Knowledge

- The game always starts with at least two standard decks plus jokers in early rounds.
- Later rounds may add up to four decks.
- Raul will consult other game experts to formulate this rule more precisely.

### Questions

- How many standard decks are used by round and player count?
- How many jokers are included per deck or per game?
- Does the joker count scale with the number of standard decks?

---

## ISS-022: Model closed-player joker stealing rule

Status: Open  
Priority: P1  
Area: Game Rules

### Problem

A player who has not opened may steal a joker, but must open that same turn.

### Risk

This creates a temporary obligation within a turn.

### Proposed Direction

Model turn obligations explicitly.

Possible concept:

```text
TurnObligation
MustOpenThisTurn
MustUseStolenJoker
```

This avoids burying the rule in UI event handling or ad hoc conditionals.

---

## ISS-019: Played meld animation targets wrong scene region

Status: Open  
Priority: P2  
Area: UI / Presentation

### Problem

When a player plays selected cards into the play area, the cards do not animate to the intended player meld region.

Current observed behavior:

- Played cards animate near the upper-left corner.

Expected behavior:

- Played cards should animate to the player meld area, which is the second 30% vertical band of the right gameplay area.

### Relevant Layout

```text
Full Scene
├── Left 20%: HUD
└── Right 80%: Gameplay Area
    ├── Top 30%: Opponent played melds
    ├── Next 30%: Player played melds
    ├── Next 30%: Player hand
    └── Bottom 10%: Play buttons
```

### Likely Cause

The animation target is probably using incorrect coordinate-space assumptions or outdated layout constants.

Potential sources:

- `GameLayout`
- Hand play-card animation logic
- Entity coordinate conversion
- Hardcoded coordinates
- Confusion between full-scene coordinates and gameplay-area-relative coordinates

### Proposed Direction

Do not solve this immediately unless it blocks development.

Address during Milestone 4 when splitting the domain hand model from presentation-layer behavior.

The eventual design should route visual movement through a presentation service or view/controller layer, not through domain objects.

---

## ISS-020: Preserve layout debug overlay during UI refactor

Status: Open  
Priority: P3  
Area: UI / Developer Tooling

### Problem

The debug layout overlay is useful for verifying the dedicated scene regions.

### Proposed Direction

Keep the debug overlay available while layout and animation behavior are being refactored.

It should remain optional/development-only and should not affect domain model behavior.
