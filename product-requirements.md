# Product Requirements Document

## Project

Reusable Java/FXGL card-game engine plus the specific turn-based multiplayer card game application **La Kika**.

## Purpose

Build a clean, extensible card-game architecture while developing a playable custom card game. The project should support learning software engineering, game architecture, JavaFX, FXGL, and design patterns through practical development.

The project should also serve as a teaching vehicle for interface design and deep modules: Raul should focus increasingly on domain concepts and public interfaces, while implementation details are delegated behind well-designed modules.

## Product Goals

1. Create a playable turn-based multiplayer implementation of La Kika.
2. Develop reusable card-engine abstractions that are not tightly coupled to FXGL.
3. Support 2-4 players.
4. Support multiple standard decks and jokers as wild cards.
5. Support La Kika meld rules: three-of-a-kind-or-more and straight flushes only.
6. Support castigo behavior.
7. Support six rounds with distinct opening requirements.
8. Support round scoring and cumulative scoring.
9. Support save states through explicit snapshots.
10. Keep architecture clean enough to test domain logic without launching FXGL.
11. Use the project as a guided learning environment for interface design, deep modules, and design patterns.

## Non-Goals for Now

These are not immediate priorities:

- Networked multiplayer.
- Online matchmaking.
- Advanced AI opponents.
- Full modding/scripting support.
- Polished commercial UI.
- Final balancing of all game rules.
- Mobile deployment.
- Persistence beyond local save/load.

## Current Known Game Summary

La Kika is a turn-based multiplayer card game for 2-4 players.

Each round:

- Each player is dealt 13 cards.
- Players take turns drawing, playing/mutating melds, and discarding.
- A player must satisfy the round opening requirement before freely playing or mutating melds.
- Melds remain in a shared play area.
- Any player may mutate any meld if the move is legal.
- The round ends when a player empties their hand.
- Other players score penalty points based on cards remaining in hand.

The full game:

- Has six rounds.
- Uses cumulative scoring.
- The player with the lowest cumulative score after six rounds wins.

## Users

### Primary User

Raul, developer and domain expert, building the game while learning software engineering and game architecture.

### Future Players

Players of La Kika.

## Guiding Architecture Principles

### 1. Separate Domain from Presentation

Domain classes should not depend on FXGL or JavaFX.

Good domain candidates:

```text
Card
Deck
HandModel
Player
Meld
Move
RoundState
GameState
ScoreRule
OpeningRequirement
```

FXGL-dependent candidates:

```text
CardComponent
HandView
GameHUD
CardAnimationComponent
MeldView
PlayAreaView
```

### 2. Prefer Deep Modules

Public interfaces should be small and intention-revealing. Implementation details should remain hidden.

Example target interface:

```java
game.startNewRound();
game.draw(playerId);
game.takeCastigo(playerId);
game.playMove(playerId, move);
game.discard(playerId, cardId);
```

The caller should not manually manipulate every card collection. The rules module should decide whether a move is legal and then mutate the game state consistently.

### 3. Distinguish Validity from Legality

Use two separate concepts:

```text
Valid = structurally correct.
Legal = allowed in the current game state.
```

Example:

- `7♣ 7♦ 7♠` is a valid three-of-a-kind meld.
- It may be illegal to play if the player has not satisfied the round opening requirement.

### 4. Save State Is a First-Class Requirement

Do not rely on Java object identity alone. Domain objects that must survive save/load need stable IDs and snapshots.

### 5. Build Incrementally

Avoid large rewrites unless the current architecture blocks progress. Prefer small steps with tests where possible.

### 6. Prefer Explicit Domain Concepts

Avoid hiding game rules in generic collections or UI code.

Examples of concepts that deserve explicit names:

```text
Meld
Castigo
OpeningRequirement
RoundState
TurnPhase
Move
LegalMoveValidator
ScoreRule
```

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

Known La Kika rules:

- Jokers may represent any rank or suit.
- Jokers may not make up more than half of a meld.
- Jokers may not be consecutive in a straight flush.
- Jokers may be replaced and taken from melds.
- Taken jokers must be played during the same turn.
- Jokers score 50 points when left in hand.

### FR-4: Deck Creation

The system shall support decks with configurable numbers of standard 52-card decks and jokers.

Status: partially implemented.

### FR-5: Hand Management

The system shall represent cards held by a player.

Status: implemented, but currently mixed with FXGL rendering and animation.

Known La Kika rule:

- Each player starts each round with 13 cards.

### FR-6: Card Selection

The system shall allow a player to select cards from their hand.

Status: implemented in current `Hand`/component behavior, but should be separated into domain logic and presentation logic.

Required update:

- Selection must support melds larger than five cards.
- Selection behavior should be driven by move construction, not poker-hand assumptions.

### FR-7: Meld Creation

The system shall allow a player to create legal melds.

Allowed meld types:

1. Three-of-a-kind or more.
2. Straight flush.

Status: not yet implemented as clean domain logic.

### FR-8: Meld Mutation

The system shall allow players to mutate existing melds in the play area.

Allowed mutations:

- Add a same-rank card to a three-of-a-kind-or-more meld.
- Add a continuing card to the beginning or end of a straight flush.
- Replace a joker in a meld.
- Take a joker from a meld if the replacement is legal.
- Use a taken joker during the same turn.

Status: not yet implemented.

### FR-9: Play Area

The system shall represent melds currently in play.

Rules:

- The play area is shared for rule purposes.
- Melds are not permanently owned by individual players.
- Any player may mutate any meld if the move is legal.

Status: not yet implemented as a clean domain model.

### FR-10: Turn Structure

The system shall model a turn as three phases:

1. Draw/castigo phase.
2. Meld play/meld mutation phase.
3. Discard phase.

Status: not yet implemented.

### FR-11: Castigo

The system shall support castigo.

Rules:

- A player may take only the most recently discarded card.
- A player taking castigo must also draw three cards from the deck.
- Only one castigo may occur per turn.
- If the active player declines the castigo, other players may accept it in player turn order.

Status: not yet implemented.

### FR-12: Round Opening Requirements

The system shall enforce opening requirements per round.

Opening requirements:

1. Round 1: one three-of-a-kind.
2. Round 2: two three-of-a-kind melds.
3. Round 3: one four-of-a-kind.
4. Round 4: two four-of-a-kind melds.
5. Round 5: one five-of-a-kind.
6. Round 6: one straight flush of eight cards.

Rules:

- A player cannot freely create or mutate melds until satisfying the current round's opening requirement.
- Opening does not consume the full turn.
- After opening, the player may continue creating or mutating melds immediately.

Status: not yet implemented.

### FR-13: Round End

The system shall end a round when a player empties their hand.

Status: not yet implemented as a clean domain rule.

Open detail:

- Need to clarify whether the final card may be discarded to end the round or must be played into a meld.

### FR-14: Scoring

The system shall score players based on cards remaining in hand at the end of a round.

Point values:

```text
2-7     = 5 points
8-King  = 10 points
Ace     = 20 points
Joker   = 50 points
```

Status: not yet implemented.

### FR-15: Game End

The system shall end the game after six rounds.

The player with the lowest cumulative score wins.

Status: not yet implemented.

### FR-16: Save and Load

The system shall support save states through explicit snapshot objects.

Status: started with `CardSnapshot`.

### FR-17: FXGL Card Rendering

The application shall render cards as FXGL entities using card data from the domain model.

Status: partially implemented.

### FR-18: HUD Updates

The application shall display relevant game information such as selected meld information, score, turn state, and round status.

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

### NFR-6: Rule Traceability

Rules should be implemented in modules whose names correspond to domain concepts.

Examples:

```text
MeldValidator
OpeningRequirement
CastigoService
ScoreCalculator
LegalMoveValidator
```

## Proposed Milestones

### Milestone 1: Clean Card and Deck Foundation

- Stable `CardId`.
- `CardSnapshot`.
- Remove FXGL dependency from `Card`.
- Configurable deck creation.

Status: mostly complete.

### Milestone 2: Define La Kika Domain Model

- Define `Meld`.
- Define `Move`.
- Define `PlayArea`.
- Define `TurnPhase`.
- Define `RoundState`.
- Define `OpeningRequirement`.

### Milestone 3: Replace Poker-Hand Prototype Logic

- Remove `PokerHandEvaluator` as a central concept.
- Replace with La Kika meld validation.
- Support three-of-a-kind-or-more.
- Support straight flushes.
- Support ace-low non-cyclic sequence rules.

### Milestone 4: Split Hand Domain from FXGL View

- Create pure `Hand` or `HandModel`.
- Move layout and animation out of domain hand logic.
- Keep FXGL entity mapping in presentation layer.

### Milestone 5: Introduce Game State and Actions

- `GameState` / `RoundState`.
- Player identity.
- Actions such as draw, take castigo, create meld, mutate meld, discard.
- Validation before mutation.

### Milestone 6: Implement Round and Turn Rules

- Deal 13 cards.
- Enforce turn phases.
- Enforce castigo.
- Enforce opening requirements.
- End round when a player empties their hand.

### Milestone 7: Scoring and Game End

- Score remaining hand cards.
- Track cumulative score.
- End after six rounds.
- Determine lowest-score winner.

### Milestone 8: Save/Load

- Snapshot whole game state.
- Restore full game state.
- Add JSON serialization.

### Milestone 9: UI/HUD Cleanup

- Remove static HUD update calls.
- Introduce observer/event/property pattern.
- Display turn, meld, castigo, opening, and scoring information.

### Milestone 10: Tests

- Unit tests for deck creation.
- Unit tests for card snapshots.
- Unit tests for meld validation.
- Unit tests for joker constraints.
- Unit tests for opening requirements.
- Unit tests for scoring.
- Unit tests for round transitions.

## Open Product Questions

1. May a player discard their final card to end the round, or must the final card be played into a meld?
2. What happens if the deck has fewer than three cards when a castigo requires drawing three additional cards?
3. What happens if the deck is exhausted during normal draw?
4. Can a player take more than one joker from melds in a single turn?
5. Must a taken joker be used in a new meld, or may it be used to mutate an existing meld?
6. When a player opens with multiple melds, may those melds include jokers?
7. For Round 6, does the eight-card straight flush opening requirement allow jokers?
8. If a player has not opened, may they take a castigo?
9. If a player has not opened, may they replace/take a joker, or is that considered a meld mutation that requires opening first?
