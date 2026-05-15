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

Status: Blocked  
Priority: P1  
Area: Game Rules

### Problem

Jokers are known to be wild, but exact behavior is undefined.

### Questions

- Can jokers represent any rank?
- Can jokers represent any suit?
- Can several jokers represent the same face?
- Are jokers always assigned optimally?
- How are jokers scored when left in hand?

### Next Step

Domain expert defines joker behavior.

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

## ISS-008: Clarify poker-hand evaluator role

Status: Blocked  
Priority: P1  
Area: Game Rules

### Problem

Current code evaluates poker-like hands, but the final game rules are not yet defined.

### Questions

- Is poker-hand evaluation part of the final game?
- Is it temporary prototype behavior?
- What combinations are legal plays?
- Does the selected-card limit of 5 come from final rules?

---

## ISS-009: Define round lifecycle

Status: Blocked  
Priority: P1  
Area: Game Rules

### Problem

The game is round-based, but the lifecycle is not yet formally modeled.

### Questions

- How does a round start?
- How are hands dealt?
- What actions can players take?
- What ends a round?
- What happens immediately after a player empties their hand?

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
