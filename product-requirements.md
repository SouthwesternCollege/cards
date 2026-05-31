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

- The game starts with two standard decks plus jokers.
- Each standard deck contributes two jokers.
- Additional decks are added when necessary.
- Exact scaling by player count and later rounds may be refined after consulting other game experts.

Status: partially implemented.

### FR-5: Deck Exhaustion

The system shall handle normal draw and castigo draw when the deck has insufficient cards.

Current rule decision:

- Add a new shuffled standard deck with jokers to the game deck when the current deck cannot satisfy the draw.
- The added deck includes 52 standard cards and two jokers.

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

Validation rules implemented in Milestone 3:

- Minimum meld size is three cards.
- Three-of-a-kind-or-more melds may contain duplicate physical cards with the same rank and suit because multiple standard decks are used.
- Kind melds cannot contain mixed non-joker ranks.
- Straight flushes cannot contain duplicate sequence ranks, even if the cards are physically distinct duplicates from multiple decks.
- Straight flush selections do not need to be pre-sorted; the validator returns or supports a normalized order for presentation.
- Aces are low.
- Straight flushes are not cyclic.
- Jokers may appear in opening melds as long as the resulting meld is legal.
- Jokers cannot be more than half of any meld.
- All-joker melds are invalid.
- Jokers cannot be consecutive in straight flush interpretations.
- Ambiguous straight flush joker placement chooses the lowest possible valid sequence.

Design nuance:

- Meld validation answers whether selected cards can structurally form a valid meld.
- Presentation/meld placement determines how the valid meld should be displayed.
- Players may select straight-flush cards in any order. Once played, the straight flush should be displayed in normalized sequence order.
- Kind melds preserve selected/insertion order.
- Because ambiguous jokers choose the lowest valid sequence, `Q♠ K♠ Joker` can validate as `J♠ Q♠ K♠`; it does not validate as `Q♠ K♠ A♠` because aces are not high. If future rules require position-sensitive joker placement, the validator interface may need to accept placement/order intent.

Status: implemented as initial domain validation in Milestone 3.

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
- Each player has 10 castigos per game.
- Taking a castigo consumes one castigo.
- Declining a castigo does not consume one.
- Castigos reset between games, but not between rounds.

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

Status: partially improved in Milestone 4B.

Milestone 4B introduced a prototype all-player HUD layout, but `GameHUD` still uses some static/global access for selected-meld feedback. Full observer/property-based HUD updates remain future work.


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

Current status:

- Milestone 4A/4A.2 introduced an initial played-meld layout and centering fix.
- This is still transitional. Full multi-meld wrapping, compression, creator grouping, and opponent carousel behavior remain future work.

### UI-4: Player Hand Region

The application shall reserve the third 30% of the right gameplay area for the current player's hand.

### UI-5: Play Button Region

The application shall reserve the bottom 10% of the right gameplay area for play buttons and action controls.

### UI-6: Debug Layout Overlay

The application may include a debug overlay to verify layout boundaries during development.

This overlay is a development tool and should not be treated as part of the domain model.


### UI-7: Local Hot-Seat Prototype

The application shall use local hot-seat multiplayer for the prototype.

Rules:

- The local/current player is always displayed at the bottom.
- The hand area shows the active player's hand.
- Opponent/non-active hands are hidden during normal play.

### UI-8: Debug Hand Overlay

The application may include a development-only debug overlay that reveals hidden hands.

This overlay is for testing/debugging only and should not be part of normal gameplay.

### UI-9: Future Pass-Device Screen

A future version should include a pass-device screen between turns.

Intended flow:

```text
Player turn ends
Screen hides hand information
Prompt: Pass device to next player
Next player confirms
Next player's hand becomes visible
```

### UI-10: HUD Player Status

The HUD should show all players simultaneously.

Milestone 4B introduced a prototype all-player HUD.

For each player, the HUD should eventually display or visually indicate:

- Name.
- Cumulative score only.
- Cards remaining.
- Castigos remaining.
- Opened/closed status.
- Dealer indicator.
- Active-turn indicator.

Current visual direction:

```text
Closed = muted/gray name
Opened = bright/normal player color
Active turn = highlighted + arrow
Dealer = chip/icon
Castigos = simple text for now
```

Current Milestone 4B implementation:

- `PlayerHudState` represents presentation-facing HUD data.
- `PlayerColorPalette` centralizes player colors.
- `GameHUD` renders prototype rows for all players.
- The prototype HUD currently uses placeholder player state until full game state exists.
- Selected-meld feedback remains displayed at the bottom of the HUD.

### UI-11: Player Colors

Players should use fixed colors for identity cues:

```text
Player 1 = blue
Player 2 = red
Player 3 = gold
Player 4 = green
```

### UI-12: Deck and Discard Placement

Preferred placement is to the right of the player hand.

Fallback placement is the bottom of the HUD if the hand area becomes too crowded.

Interaction direction:

- Click/button-like behavior for draw and castigo first.
- Drag/drop discard may be added later.

### UI-13: Card Wiggle Behavior

Desired behavior:

- Hand cards have subtle idle wiggle.
- Hovered hand card has stronger wiggle.
- Played meld cards have no wiggle or very subtle idle.
- Deck/discard should not wiggle unless interactive feedback is needed.

Known animation concern:

- Hover-intensified wiggle should not jump to a starting angle; it should continue smoothly from current phase.

### UI-14: Future Full Play-Area View

A future read-only full play-area view should hide HUD, hand, and controls.

Direction:

- Toggle button should live in the play button area.
- Show all players simultaneously, one row per player.
- Avoid shrinking cards in full view when possible.
- Use horizontal scrolling if a player's play area has too many cards.

Status: deferred.

### UI Architecture Note

The `GameLayout` class currently centralizes layout calculations. This is acceptable for the prototype, but Milestone 4 should revisit the boundary between:

```text
GameLayout          // layout geometry
HandView            // visual hand behavior
MeldView            // visual meld behavior
PlayAreaView        // visual play area behavior
Domain model        // no rendering knowledge
```



### FR-21: Milestone 2 Domain Model Interfaces

The system shall define initial domain-model types and interfaces for La Kika before implementing full validation logic.

Included concepts:

```text
PlayerId
MeldId
Meld
MeldType
MeldPlacement
PlayArea
Move
CreateMeldMove
AddToMeldMove
StealJokerMove
TurnPhase
RoundState
OpeningRequirement
MeldValidator
MeldValidationResult
JokerAssignment
StolenJokerObligation
LegalMoveValidator
MoveResult
```

Design constraints:

- These types belong to the domain layer.
- They should not depend on FXGL or JavaFX.
- Full meld validation is deferred to Milestone 3.
- Full turn execution is deferred to later game-state milestones.

Status: started in Milestone 2.


### FR-22: Milestone 4 Presentation Architecture Baseline

The system shall continue separating domain hand state from FXGL presentation behavior.

Milestone 4A introduced:

```text
HandModel
HandLayout
CardLayoutSlot
CardEntityRegistry
```

Milestone 4A.2 introduced or refined:

```text
SelectionFeedback
HudMeldSelectionFeedback
PlayedMeldLayout
```

Current design direction:

- `HandModel` owns pure hand state.
- `HandLayout` computes hand visual positions.
- `CardEntityRegistry` maps cards to FXGL entities in the presentation layer.
- `PlayedMeldLayout` handles transitional played-meld centering and grouping.
- `CardAnimationComponent` should focus on card input/animation behavior rather than HUD updates or validation internals.

Status: in progress.


### UI-14: Played Meld Layout

The application shall visually group played cards into melds.

Rules:

- Melds should remain visually associated with the player who originally created them.
- Melds should be displayed left-to-right in creation order.
- Cards within a meld should overlap enough to visually communicate grouping.
- Minimum compressed card spacing should be 20% of card width so rank/suit remain readable.
- Melds should have enough space between them to visually separate groups.
- Melds should be centered horizontally and vertically within their assigned play area.
- Card spacing and meld spacing should compress before wrapping.
- Melds should wrap to additional rows before cards are shrunk.
- Card scaling should be avoided where possible; normal-view scale should not go below 70% once scaling is introduced.
- Played straight flush melds should display in normalized sequence order.
- Kind melds should preserve selected/insertion order.

### UI-15: Played Card Animation

When cards are played into a meld, they should animate to the final reflowed layout positions.

Current animation decision:

- Cards animate one at a time.
- Use a 0.1-second stagger between cards.
- Existing meld cards may reflow when a new meld is added.
- The newly played meld should animate to its final position after layout reflow is computed.

Future settings direction:

- Animation speed should eventually be configurable by the player.
- For now, animation defaults may live in a centralized presentation settings object rather than being scattered as hardcoded values.



### UI-16: Meld Layout Debug Harness

The application shall provide development-only controls for stress-testing the played-meld layout before full draw/discard/turn systems exist.

Initial debug controls:

- `+Kind`: add a test three-of-a-kind-or-more visual meld.
- `+Run`: add a test straight flush visual meld.
- `Stress`: add several test melds to pressure-test wrapping, centering, overlap, and staggered animation.
- `Clear`: remove the visual test melds from the played area.

Rules:

- These controls are development tools, not gameplay features.
- They should not affect the final domain model.
- They should be easy to remove, hide, or replace once real game-state-driven play-area testing is available.

Status: implemented as Milestone 4C.2.


## Settings and House Rules Requirements

### SET-1: Animation Settings

The application should eventually allow players to configure animation speed.

Initial design direction:

- Keep animation constants centralized so they can later be replaced by real settings.
- Played-card animation currently uses a 0.1-second stagger between cards.
- The final settings screen may expose global animation speed or separate animation categories.

Status: planned.

### SET-2: House Rule Configuration

La Kika has minor house-rule variations. The rules engine should eventually support configurable house rules rather than hardcoding every variant.

Known example:

- Standard castigo behavior currently draws the most recent discard plus three additional cards from the deck.
- A possible house rule draws four cards if the castigo is taken when it is not the player's turn, and three cards total if it is the active player's turn.

Design direction:

- House rules should be modeled as explicit configuration.
- Gameplay services should depend on a rule/settings object rather than scattered constants.
- The initial prototype may use default La Kika rules until the settings screen exists.

Status: planned.


### UI-14: Card View Metrics

The application shall derive source card dimensions from the standard deck sprite sheet instead of hardcoding `71 × 95`.

Sprite sheet contract:

```text
13 columns × 4 rows
```

Rendered card size:

```text
source cell size × render scale
```

Current prototype render scale:

```text
2.0
```

The render scale is a presentation setting and may later become configurable.

### UI-15: Vertical Card Content Band

Cards in hand and meld regions should be vertically centered inside the middle 80% of their layout area.

This rule applies to:

- Player hand area.
- Player meld area.
- Opponent meld area.

Selected hand cards may lift upward into the region padding, but should not leave the hand region.

### UI-16: Shared Card Metrics Across Presentation Classes

`CardComponent`, `HandLayout`, and `MeldLayout` should all use the same rendered card metrics.

This prevents bugs where the card texture is scaled to one size but layout code centers using another size.

### UI-17: Fitted Card Views

Card visuals should be rendered at their final size using fitted image nodes rather than JavaFX scale transforms.

Design invariant:

```text
Card entity position = top-left corner of the rendered card.
```

This prevents layout bugs where visual card bounds are shifted relative to entity coordinates.


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

Status: mostly complete.

Scope:

- Stable `CardId`.
- `CardSnapshot`.
- Remove FXGL dependency from `Card`.
- Configurable deck creation.
- Support multiple standard decks and jokers.
- Add shuffled standard deck with jokers when the deck is exhausted.

### Milestone 2: Define La Kika Domain Model

Status: started.

Scope:

- Define `Meld`.
- Define `Move`.
- Define `PlayArea`.
- Define `TurnPhase`.
- Define `RoundState`.
- Define `OpeningRequirement`.
- Define `DealerRotation`.
- Define exact deal bonus domain concept.
- Define initial validation/result abstractions.

### Milestone 3: Replace Poker-Hand Prototype Logic

Status: implemented as initial domain validation.

Scope:

- Remove `PokerHandEvaluator` from active La Kika selection feedback.
- Add La Kika meld validation.
- Support three-of-a-kind-or-more.
- Support straight flushes.
- Support ace-low non-cyclic sequence rules.
- Support joker ratio and consecutive-joker constraints.
- Add validation error codes.
- Add normalized meld results and joker assignments.

Note: `PokerHandEvaluator` may remain in the codebase temporarily as a possible future generic-engine utility, but it should not drive La Kika gameplay.

### Milestone 4: Presentation Architecture and UI Layout

Milestone 4 is intentionally split into smaller sub-milestones because the GUI/presentation work is large.

#### Milestone 4A: Presentation Architecture Baseline

Status: implemented.

Scope:

- Introduce `HandModel`.
- Introduce `HandLayout`.
- Introduce `CardEntityRegistry`.
- Keep existing `Hand` as a transitional facade.
- Start moving hand state away from FXGL entity concerns.

#### Milestone 4A.2: Interaction Cleanup and Played-Meld Centering

Status: implemented.

Scope:

- Move selected-meld feedback behind `SelectionFeedback`.
- Reduce `CardAnimationComponent` coupling to HUD and validation.
- Add centered played-meld placement.
- Fix double-animation and merged-meld issues.
- Preserve the working FXGL rotation fix using `entity.setRotation(0.0)`.

#### Milestone 4B: HUD Redesign

Status: implemented as a prototype/debug HUD.

Scope:

- Show all players at once.
- Display cumulative score.
- Display cards remaining.
- Display `Castigos: X remaining`.
- Show dealer chip.
- Show active-turn arrow.
- Show opened/closed visual state.
- Use player colors: blue, red, gold, green.

#### Milestone 4B.2: HUD Cleanup and Live Prototype Counts

Status: implemented.

Scope:

- Add `PlayerHudModel`.
- Add `GameHudController`.
- Move selected-meld feedback away from direct static HUD updates.
- Wire the active prototype hand to update cards remaining live.
- Keep HUD useful as a development/debug tool until real `GameState` exists.

#### Milestone 4C: Meld / Play-Area Layout

Status: implemented as a visual layout prototype.

Scope:

- Add `VisualMeld`.
- Add `VisualMeldStore`.
- Add `MeldLayout`.
- Add `MeldLayoutSlot`.
- Keep melds visually grouped by creator.
- Lay out melds in creation order.
- Reflow existing melds when a new meld is added.
- Use staggered played-card animation.

#### Milestone 4C.2: Meld Layout Debug Harness and Layout Corrections

Status: implemented.

Scope:

- Add development-only test controls for layout stress testing.
- Add test kind melds.
- Add test straight-flush melds.
- Add stress batch.
- Clear visual melds.
- Adjust played-card stagger to `0.1` seconds.
- Centralize animation defaults in `AnimationSettings`.
- Improve compressed card spacing.
- Center hand and meld cards using shared metrics.
- Derive card dimensions from the sprite sheet contract.
- Replace transform-scaled card textures with fitted card views.

#### Milestone 4D: Deck and Discard Placement / Click Interactions

Status: not started.

Scope:

- Place deck and discard pile near the right side of the player hand area if feasible.
- Keep bottom-of-HUD placement as a fallback if the hand area becomes too crowded.
- Click deck to draw.
- Click discard pile to begin castigo behavior later.
- Provide a simple discard interaction, likely button-like first.
- Defer drag/drop discard until after basic turn flow exists.

#### Milestone 4E: Screen Flow / Splash / Title / Main Menu

Status: not started.

Scope:

- Move animated La Kika title into its own screen.
- Auto-transition from title screen to main menu.
- Add main menu buttons:
  - Play
  - Settings
  - Rules
- Use FXGL scene/menu support as much as practical.
- For now, `Play` starts a prototype local hot-seat game with default players.
- Defer settings and rules screens unless they block development.

#### Milestone 4F: Animation Continuity

Status: not started.

Scope:

- Replace jumpy timeline-based wiggle transitions with a stateful card animation component.
- Preserve animation phase when hover intensity changes.
- Hand cards wiggle.
- Hovered hand cards wiggle more intensely.
- Played meld cards are calm or use very subtle idle motion.
- Deck/discard should not wiggle unless interactive.

#### Milestone 4G: Full Play-Area View

Status: deferred.

Scope:

- Add a read-only full play-area view.
- Hide HUD, hand, and controls while viewing.
- Show all players simultaneously, one row per player.
- Avoid shrinking cards in full-table view when possible.
- Use horizontal scrolling for crowded player rows if needed.

#### Milestone 4H: Hot-Seat Privacy and Debug Hand Overlay

Status: not started.

Scope:

- Normal gameplay shows only the active player's hand.
- Add development-only debug hand overlay.
- Later add pass-device screen between turns.

### Milestone 5: Introduce Game State and Actions

Status: not started.

Scope:

- `GameState` / `RoundState`.
- Player identity.
- Dealer identity.
- Player hands.
- Castigos remaining.
- Opened/closed status.
- Actions such as draw, take castigo, create meld, mutate meld, steal joker, discard.
- Validation before mutation.

### Milestone 6: Implement Round and Turn Rules

Status: not started.

Scope:

- Deal 13 cards.
- Rotate dealer.
- Enforce turn phases.
- Enforce castigo.
- Enforce per-player castigo limit.
- Handle deck exhaustion by adding a new shuffled standard deck with jokers.
- Enforce opening requirements.
- Enforce stolen-joker obligations.
- End round on final discard.

### Milestone 7: Scoring and Game End

Status: not started.

Scope:

- Score remaining hand cards.
- Apply exact deal bonus.
- Track cumulative score.
- Support negative scores.
- End after six rounds.
- Determine lowest-score winner.

### Milestone 8: Save/Load

Status: not started.

Scope:

- Snapshot whole game state.
- Restore full game state.
- Add JSON serialization.

### Milestone 9: Settings and House Rules

Status: not started.

Scope:

- Animation speed settings.
- Music/sound settings.
- House-rule configuration.
- Castigo draw-count variants.
- Future player/game setup options.

### Milestone 10: Tests

Status: not started.

Scope:

- Unit tests for deck creation.
- Unit tests for card snapshots.
- Unit tests for meld validation.
- Unit tests for joker constraints.
- Unit tests for opening requirements.
- Unit tests for castigo.
- Unit tests for dealer rotation and exact deal bonus.
- Unit tests for scoring.
- Unit tests for round transitions.
- Layout calculation tests where practical.



## Open Product Questions

No blocking product questions remain for Milestones 1-4A.2.

Remaining clarifications to eventually answer:

1. What is the exact starting deck composition by player count and round?
   - Current rule: start with two standard decks plus jokers.
   - Each standard deck contributes two jokers.
   - Additional decks are added when necessary.
   - Raul will consult other game experts to formalize whether later rounds/player counts should start with more than two decks.
2. How should the dealer's digital shot/swing meter be tuned so that the exact deal bonus is skill-based but not frustrating?
   - Current direction: use a linear interpolation between the top and bottom of the deck.
   - Do not display the exact number of cards selected on the meter.
   - An arrow or similar indicator is acceptable for now.
   - Later version may animate the deck splitting, with cards moving from top to bottom until the player clicks to stop.
3. How should full play-area view be implemented visually?
   - Current direction: deferred read-only toggle screen.
   - Show all players simultaneously.
   - Avoid shrinking cards where possible; prefer scrolling.

