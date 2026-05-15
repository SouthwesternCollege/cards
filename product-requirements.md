# Product Requirements Document

## Project

Reusable Java/FXGL card-game engine plus a specific turn-based multiplayer card game application.

## Purpose

Build a clean, extensible card-game architecture while developing a playable custom card game. The project should support learning software engineering, game architecture, JavaFX, FXGL, and design patterns through practical development.

## Product Goals

1. Create a playable turn-based multiplayer card game.
2. Develop reusable card-engine abstractions that are not tightly coupled to FXGL.
3. Support multiple standard decks and jokers as wild cards.
4. Support save states through explicit snapshots.
5. Keep architecture clean enough to test domain logic without launching FXGL.
6. Use the project as a guided learning environment for interface design, deep modules, and design patterns.

## Non-Goals for Now

These are not immediate priorities:

- Networked multiplayer.
- Online matchmaking.
- Advanced AI opponents.
- Full modding/scripting support.
- Polished commercial UI.
- Final balancing of all game rules.

## Current Known Game Summary

- Turn-based multiplayer.
- Fixed decks.
- Multiple standard decks may be used.
- Jokers are included and act as wilds.
- The game has several rounds.
- The first player to empty their hand wins the round.
- Other players score points based on the sum of cards remaining in hand.

## Users

### Primary User

Raul, developer and domain expert, building the game while learning software engineering and game architecture.

### Future Players

Players of the custom card game.

## Guiding Architecture Principles

### 1. Separate Domain from Presentation

Domain classes should not depend on FXGL or JavaFX.

Good:

```text
Card, Deck, HandModel, RoundState, ScoreRule
```

FXGL-dependent:

```text
CardComponent, HandView, GameHUD, CardAnimationComponent
```

### 2. Prefer Deep Modules

Public interfaces should be small and intention-revealing. Implementation details should remain hidden.

Example target interface:

```java
round.start();
round.play(playerId, selectedCards);
round.score();
```

The caller should not manage all low-level mutation itself.

### 3. Save State Is a First-Class Requirement

Do not rely on Java object identity alone. Domain objects that must survive save/load need stable IDs and snapshots.

### 4. Build Incrementally

Avoid large rewrites unless the current architecture blocks progress. Prefer small steps with tests where possible.

## Functional Requirements

### FR-1: Card Model

The system shall represent standard cards with rank and suit.

Status: partially implemented.

### FR-2: Card Identity

The system shall assign stable identities to physical cards so identical card faces can still be distinguished.

Status: implemented with `CardId`.

### FR-3: Jokers

The system shall represent jokers as distinct cards.

Status: partially implemented.

Needs rules for wild-card behavior.

### FR-4: Deck Creation

The system shall support decks with configurable numbers of standard 52-card decks and jokers.

Status: partially implemented.

### FR-5: Hand Management

The system shall represent cards held by a player.

Status: implemented, but currently mixed with FXGL rendering and animation.

### FR-6: Card Selection

The system shall allow a player to select cards from their hand.

Status: implemented in current `Hand`/component behavior, but should be separated into domain logic and presentation logic.

### FR-7: Playing Cards

The system shall allow a player to play selected cards according to game rules.

Status: prototype behavior exists, but domain rules are not finalized.

### FR-8: Round End

The system shall end a round when a player empties their hand.

Status: not yet implemented as a clean domain rule.

### FR-9: Scoring

The system shall score non-winning players based on cards remaining in hand when the round ends.

Status: not yet implemented.

### FR-10: Save and Load

The system shall support save states through explicit snapshot objects.

Status: started with `CardSnapshot`.

### FR-11: FXGL Card Rendering

The application shall render cards as FXGL entities using card data from the domain model.

Status: partially implemented.

### FR-12: HUD Updates

The application shall display relevant game information such as selected hand rank, score, turn state, and round status.

Status: prototype exists, but `GameHUD` currently uses static/global access.

## Non-Functional Requirements

### NFR-1: Testability

Core game logic should be testable without launching FXGL.

### NFR-2: Extensibility

The engine should support new card games or game modes with minimal changes to core abstractions.

### NFR-3: Readability

Code should favor clear names and explicit domain concepts over clever implementation.

### NFR-4: Stability of Save Files

Save format should not break unnecessarily when implementation details change.

Recommendation: JSON snapshots, not Java object serialization.

### NFR-5: Learning Value

When possible, changes should be explained in terms of interface design, responsibility boundaries, and relevant design patterns.

## Proposed Milestones

### Milestone 1: Clean Card and Deck Foundation

- Stable `CardId`.
- `CardSnapshot`.
- Remove FXGL dependency from `Card`.
- Configurable deck creation.

Status: mostly complete.

### Milestone 2: Split Hand Domain from FXGL View

- Create pure `Hand` or `HandModel`.
- Move layout and animation out of domain hand logic.
- Keep FXGL entity mapping in presentation layer.

### Milestone 3: Define Core Game Rules

- Turn structure.
- Legal plays.
- Joker behavior.
- Round ending.
- Scoring.

### Milestone 4: Introduce Game State and Actions

- `GameState` / `RoundState`.
- Player identity.
- Actions such as select, play, draw, discard, pass.
- Validation before mutation.

### Milestone 5: Save/Load

- Snapshot whole game state.
- Restore full game state.
- Add JSON serialization.

### Milestone 6: UI/HUD Cleanup

- Remove static HUD update calls.
- Introduce observer/event/property pattern.
- Display turn and scoring information.

### Milestone 7: Tests

- Unit tests for deck creation.
- Unit tests for card snapshots.
- Unit tests for hand selection.
- Unit tests for scoring.
- Unit tests for round transitions.

## Open Product Questions

1. What is the exact player count range?
2. How many cards are dealt to each player?
3. Do players draw cards during the round?
4. What are all legal player actions on a turn?
5. What combinations may be played?
6. Are poker hands actually part of the game rules, or only a temporary prototype feature?
7. What are joker rules?
8. What are card point values?
9. Does the lowest cumulative score win?
10. How many rounds are played, or what ends the full game?
