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
8. Support dealer rotation and the exact deal bonus.
9. Support round scoring and cumulative scoring, including negative scores.
10. Support save states through explicit snapshots.
11. Keep architecture clean enough to test domain logic without launching FXGL.
12. Use the project as a guided learning environment for interface design, deep modules, and design patterns.

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

- Dealer responsibility rotates according to turn order.
- The dealer shuffles and prepares cards to be dealt.
- Each player is dealt 13 cards, one at a time, starting with the player next to the dealer.
- If the dealer prepares exactly the number of cards needed for the deal, the dealer gets a 100-point score reduction.
- Players take turns drawing, playing/mutating melds, and discarding.
- A player may take castigo even if they have not opened.
- A player must satisfy the round opening requirement before freely playing or mutating melds.
- If a closed player steals a joker, they must open that same turn.
- Melds remain in a shared play area.
- Any player may mutate any meld if the move is legal.
- The round ends when a player discards their final card.
- Other players score penalty points based on cards remaining in hand.

The full game:

- Has six rounds.
- Uses cumulative scoring.
- Allows negative scores.
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
DealerRotation
DealService
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
DealerRotation
ExactDealBonus
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
- Jokers may be stolen from melds by replacement.
- More than one joker may be stolen during a single turn.
- A stolen joker must be played during the same turn.
- A stolen joker may be used to create a new meld or mutate any meld in the play area.
- Jokers score 50 points when left in hand.

### FR-4: Deck Creation

The system shall support decks with configurable numbers of standard 52-card decks and jokers.

Known setup rule:

- The game starts with at least two standard decks plus jokers in early rounds.
- Later rounds may use up to four standard decks plus jokers.
- Exact starting deck composition by player count and round is still being researched with other game experts.

Status: partially implemented.

### FR-5: Deck Exhaustion

The system shall handle normal draw and castigo draw when the deck has insufficient cards.

Current rule decision:

- Add a new shuffled standard deck with jokers to the game deck when the current deck cannot satisfy the draw.

Alternative considered:

- Shuffle the discard pile into the deck if the discard pile is large enough.

Status: not yet implemented.

### FR-6: Hand Management

The system shall represent cards held by a player.

Status: implemented, but currently mixed with FXGL rendering and animation.

Known La Kika rule:

- Each player starts each round with 13 cards.

### FR-7: Card Selection

The system shall allow a player to select cards from their hand.

Status: implemented in current `Hand`/component behavior, but should be separated into domain logic and presentation logic.

Required update:

- Selection must support melds larger than five cards.
- Selection behavior should be driven by move construction, not poker-hand assumptions.

### FR-8: Meld Creation

The system shall allow a player to create legal melds.

Allowed meld types:

1. Three-of-a-kind or more.
2. Straight flush.

Status: not yet implemented as clean domain logic.

### FR-9: Meld Mutation

The system shall allow players to mutate existing melds in the play area.

Allowed mutations:

- Add a same-rank card to a three-of-a-kind-or-more meld.
- Add a continuing card to the beginning or end of a straight flush.
- Steal a joker from a meld by replacing it with a legal card.
- Use a stolen joker to create a meld or mutate any meld.

Status: not yet implemented.

### FR-10: Play Area

The system shall represent melds currently in play.

Rules:

- The play area is shared for rule purposes.
- Melds are not permanently owned by individual players.
- Any player may mutate any meld if the move is legal.
- A player's in-front-of-them physical area should not be modeled separately for now.

Status: not yet implemented as a clean domain model.

### FR-11: Turn Structure

The system shall model a turn as three phases:

1. Draw/castigo phase.
2. Meld play/meld mutation phase.
3. Discard phase.

Status: not yet implemented.

### FR-12: Castigo

The system shall support castigo.

Rules:

- A player may take only the most recently discarded card.
- The player taking castigo must also draw three additional cards from the deck.
- Only one castigo may occur per turn.
- If the active player declines the castigo, other players may accept it in player turn order.
- A player may take castigo even if they have not opened.
- If the deck has fewer than three cards for the extra castigo draw, add a new shuffled standard deck with jokers to the game deck.

Status: not yet implemented.

### FR-13: Round Opening Requirements

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
- Opening melds may include jokers if the resulting melds are legal.
- Round 6's eight-card straight flush may include jokers if the meld is legal.
- If a closed player steals a joker, they must open that same turn.

Status: not yet implemented.

### FR-14: Round End

The system shall end a round when a player discards their final card.

Rules:

- The final card should be discarded.
- The final card does not need to be played into a meld.
- If a player can play their final card during the meld phase, they still should discard the final card instead.
- Discard is not skipped merely because the player could otherwise play all cards.

Status: not yet implemented as a clean domain rule.

### FR-15: Scoring

The system shall score players based on cards remaining in hand at the end of a round.

Point values:

```text
2-7     = 5 points
8-King  = 10 points
Ace     = 20 points
Joker   = 50 points
```

Status: not yet implemented.

### FR-16: Dealer Rotation and Exact Deal Bonus

The system shall rotate dealer responsibility according to turn order.

Rules:

- Each player takes turns being the dealer.
- At the beginning of each round, the dealer shuffles the deck.
- The dealer takes cards from the top of the deck all at once to create the deal packet.
- Cards are dealt one at a time to each player in turn order, starting with the player next to the dealer.
- The player next to the dealer also takes the first turn after the deal.
- If the dealer takes exactly `hand size * number of players` cards, subtract 100 points from the dealer's score.
- The exact deal bonus is applied immediately at the beginning of the round.
- Negative scores are possible.
- If the dealer takes too few cards, the remaining cards are dealt directly from the deck and the dealer's score is unaffected.
- If the dealer takes too many cards, the extra cards are placed back on top of the deck and the dealer's score is unaffected.

Digital interaction design:

- The current preferred digital analogy is a shot/swing meter, similar to sports games.
- The dealer selects when to stop the meter.
- The stopped meter position determines how many cards are taken from the deck for the deal packet.

Status: not yet implemented.

Design note:

- This is not merely UI flavor. It affects score and should be modeled in the domain layer.
- The domain layer should not know about the visual meter. It should only receive the resulting number of cards taken.

### FR-17: Game End

The system shall end the game after six rounds.

The player with the lowest cumulative score wins.

Status: not yet implemented.

### FR-18: Save and Load

The system shall support save states through explicit snapshot objects.

Status: started with `CardSnapshot`.

### FR-19: FXGL Card Rendering

The application shall render cards as FXGL entities using card data from the domain model.

Status: partially implemented.

### FR-20: HUD Updates

The application shall display relevant game information such as selected meld information, score, turn state, dealer, round status, castigo availability, and opening status.

Status: prototype exists, but `GameHUD` currently uses static/global access.


## UI Layout Requirements

The intended scene layout is:

```text
Full Scene
├── Left 20%: HUD
└── Right 80%: Gameplay Area
    ├── Top 30%: Opponent played melds
    ├── Next 30%: Player played melds
    ├── Next 30%: Player hand
    └── Bottom 10%: Play buttons
```

### UI-1: HUD Region

The application shall reserve the left 20% of the scene for HUD information.

This region should eventually display:

- Round number.
- Active player.
- Dealer.
- Scores.
- Current opening requirement.
- Whether the player has opened.
- Turn phase.
- Castigo availability.
- Selected-card or selected-meld feedback.

### UI-2: Opponent Meld Region

The application shall reserve the top 30% of the right gameplay area for opponent played melds.

This is a presentation-layer distinction. At the domain level, melds remain part of the shared play area.

### UI-3: Player Meld Region

The application shall reserve the second 30% of the right gameplay area for the local/current player's played melds.

Known current bug:

- Cards played from the hand do not currently animate to this region.
- They currently animate near the upper-left corner.
- This should be addressed when the hand/domain model is separated from the FXGL presentation layer.

### UI-4: Player Hand Region

The application shall reserve the third 30% of the right gameplay area for the current player's hand.

### UI-5: Play Button Region

The application shall reserve the bottom 10% of the right gameplay area for play buttons and action controls.

### UI-6: Debug Layout Overlay

The application may include a debug overlay to verify layout boundaries during development.

This overlay is a development tool and should not be treated as part of the domain model.

### UI Architecture Note

The `GameLayout` class currently centralizes layout calculations. This is acceptable for the prototype, but Milestone 4 should revisit the boundary between:

```text
GameLayout          // layout geometry
HandView            // visual hand behavior
MeldView            // visual meld behavior
PlayAreaView        // visual play area behavior
Domain model        // no rendering knowledge
```


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
DealerRotation
ExactDealBonusRule
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
- Define `DealerRotation`.
- Define exact deal bonus domain concept.

### Milestone 3: Replace Poker-Hand Prototype Logic

- Remove `PokerHandEvaluator` as a central concept.
- Replace with La Kika meld validation.
- Support three-of-a-kind-or-more.
- Support straight flushes.
- Support ace-low non-cyclic sequence rules.
- Support joker constraints.

### Milestone 4: Split Hand Domain from FXGL View

- Create pure `Hand` or `HandModel`.
- Move layout and animation out of domain hand logic.
- Keep FXGL entity mapping in presentation layer.
- Ensure played-card animation targets the player meld region.
- Preserve or improve layout-debug overlay support.

### Milestone 5: Introduce Game State and Actions

- `GameState` / `RoundState`.
- Player identity.
- Dealer identity.
- Actions such as draw, take castigo, create meld, mutate meld, steal joker, discard.
- Validation before mutation.

### Milestone 6: Implement Round and Turn Rules

- Deal 13 cards.
- Rotate dealer.
- Enforce turn phases.
- Enforce castigo.
- Handle deck exhaustion by adding a new shuffled standard deck with jokers.
- Enforce opening requirements.
- End round on final discard.

### Milestone 7: Scoring and Game End

- Score remaining hand cards.
- Apply exact deal bonus.
- Track cumulative score.
- Support negative scores.
- End after six rounds.
- Determine lowest-score winner.

### Milestone 8: Save/Load

- Snapshot whole game state.
- Restore full game state.
- Add JSON serialization.

### Milestone 9: UI/HUD Cleanup

- Remove static HUD update calls.
- Introduce observer/event/property pattern.
- Display turn, meld, castigo, opening, dealer, and scoring information.

### Milestone 10: Tests

- Unit tests for deck creation.
- Unit tests for card snapshots.
- Unit tests for meld validation.
- Unit tests for joker constraints.
- Unit tests for opening requirements.
- Unit tests for castigo.
- Unit tests for dealer rotation and exact deal bonus.
- Unit tests for scoring.
- Unit tests for round transitions.

## Open Product Questions

No blocking product questions remain for Milestone 1.

Remaining clarifications to eventually answer:

1. What is the exact starting deck composition by player count and round?
   - Current rule: always start with at least two standard decks plus jokers in early rounds.
   - Later rounds may use up to four standard decks plus jokers.
   - Raul will consult other game experts to formalize this.
2. When adding a new shuffled deck after exhaustion, how many jokers are included with the added 52 standard cards?
3. How should the dealer's digital shot/swing meter be tuned so that the exact deal bonus is skill-based but not frustrating?
