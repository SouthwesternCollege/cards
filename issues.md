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
- Jokers may be replaced and taken from melds.
- A joker taken from a meld must be played during the same turn.
- Jokers are worth 50 points in hand scoring.

### Remaining Design Work

Implementation still needs a joker subsystem to model:

- Joker assignments.
- Joker replacement.
- Joker theft/taking.
- Mandatory same-turn use.

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

1. Deal 13 cards to each player.
2. Players take turns.
3. Each player must satisfy the round opening requirement before freely playing or mutating melds.
4. A round ends when a player empties their hand.
5. Remaining players score cards left in hand.
6. Game ends after six rounds.

### Remaining Design Work

Need to clarify exact final-card behavior.

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

### Open Questions

- What happens if the deck has fewer than three cards?
- May a player who has not opened take castigo?

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
- A player may replace/take jokers under specific constraints.

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

Status: Blocked  
Priority: P1  
Area: Game Rules

### Problem

A round ends when a player empties their hand, but the exact method needs clarification.

### Questions

- May the final card be discarded to end the round?
- Must the final card be played into a meld?
- If a player plays all cards during the meld phase, is discard skipped?

---

## ISS-018: Define deck exhaustion behavior

Status: Blocked  
Priority: P1  
Area: Game Rules

### Problem

Draw and castigo require cards from the deck.

### Questions

- What happens if the deck is empty?
- What happens if castigo requires three additional cards and fewer than three remain?
- Is the discard pile reshuffled into the deck?
