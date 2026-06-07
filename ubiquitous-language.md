# Ubiquitous Language

This document defines the shared vocabulary for the card game and reusable card-game engine. The goal is to keep code, documentation, and design discussions using the same words for the same ideas.

## How to Use This File

- Add terms as soon as we discover ambiguity.
- Prefer domain language over implementation language.
- Keep implementation details separate from gameplay concepts when possible.
- Raul is the domain expert for game rules. When a term is uncertain, mark it as **Needs domain definition**.
- Use **valid** for structural correctness and **legal** for game-state permission.

## Domain Terms

### Game Name

The official name of the game is **La Kika**.

### Card

A physical card that can move between collections such as a deck, hand, play area, meld, or discard pile.

A `Card` has stable identity through `CardId` and face information through rank/suit or joker status.

Current code concept:

```java
Card card = Card.standard(new CardId(17), Rank.ACE, Suit.SPADES);
Card joker = Card.joker(new CardId(55));
```

### Card Identity

The answer to: “Which physical card is this?”

Two cards may have the same rank and suit but different identities.

Example:

```text
CardId 17 = Ace of Spades
CardId 69 = Ace of Spades
```

These are different physical cards with the same face.

### Card Face

The visible value of a card: rank/suit for standard cards, or joker status for jokers.

Current representation:

```text
Standard card = Rank + Suit
Joker = joker flag, no rank, no suit
```

Potential future improvement: introduce a `CardFace` type so `Card` is composed of `CardId` + `CardFace`.

### Rank

The value of a standard card, such as `TWO`, `THREE`, ..., `ACE`.

Known La Kika rule:

- Aces are low.
- Aces are not high.
- Straight flushes are not cyclic.

This means:

```text
A-2-3 is potentially valid.
Q-K-A is not valid.
K-A-2 is not valid.
```

### Suit

The category of a standard card: clubs, diamonds, hearts, spades.

In La Kika, suits matter for straight flushes.

### Joker

A special card that acts as a wild card.

Known La Kika rules:

- A joker may represent any rank.
- A joker may represent any suit.
- Jokers may not make up more than half of the cards in a meld.
- Jokers may not appear consecutively in a straight flush.
- A player may steal a joker from a meld if they can replace it according to the rules.
- A joker stolen from a meld must be played during the same turn.
- A player may steal more than one joker in a single turn.
- A stolen joker may be used to create a new meld or mutate any meld in the play area.
- Jokers are worth 50 points when left in a player's hand at the end of a round.

Joker replacement rules:

- In a three-of-a-kind-or-more meld, a player may steal a joker by replacing it with a card of the required rank.
- In a straight flush meld, a player may steal a joker by replacing it with the exact rank and suit represented by the joker.
- If a player has not opened and steals a joker, they must open during that same turn.

### Joker Stealing

The act of taking a joker from an existing meld by replacing it with the correct card.

This is the preferred domain term, rather than “joker theft,” “joker replacement,” or “joker rescue.”

Known rule:

- The stolen joker must be played during the same turn.

### Deck

A collection from which cards are drawn.

In this game, a deck may consist of multiple standard 52-card decks and jokers.

Current code concept:

```java
Deck deck = new Deck(numberOfStandardDecks, jokersPerDeck);
```

### Game Deck

The active deck used during a round.

Known La Kika rules:

- The game currently starts with two standard 52-card decks plus jokers.
- Each standard deck contributes two jokers.
- Additional shuffled standard decks with jokers are added when necessary.
- If the deck is exhausted, or if there are not enough cards to complete a castigo draw, add a new shuffled standard deck with jokers to the game deck.

Design note:

- Exact starting deck composition by player count and later rounds may be refined after Raul consults other game experts.
- For now, the working rule is: start with two standard decks plus jokers, then add decks when needed.

### Hand

A collection of cards held by a player.

Known La Kika rule:

- Each player starts a round with 13 cards.

Current code warning: `Hand` currently mixes domain responsibilities with FXGL rendering, animation, input, and layout. Long term, this should be split into a pure hand model and one or more visual/controller classes.

### Selected Cards

Cards in a hand that the player has selected for possible play.

Current implementation note:

- The old five-card selection limit came from prototype poker-hand evaluation behavior.
- La Kika melds may contain more than five cards, so selection rules need to be updated.

### Meld

A group of three or more cards in the play area.

In La Kika, a meld must be one of:

1. Three-of-a-kind or more.
2. Straight flush.

A meld is not permanently owned by a player. Any player may mutate any meld in the play area, subject to legal move rules.

### Three-of-a-Kind or More Meld

A meld containing at least three cards of the same rank.

Examples:

```text
7♣ 7♦ 7♠
K♣ K♦ K♥ K♠
```

Jokers may substitute for missing cards, subject to joker restrictions.

### Straight Flush Meld

A meld containing at least three sequential cards of the same suit.

Known La Kika rules:

- Aces are low.
- Straight flushes are not cyclic.
- Jokers may substitute for missing cards.
- Jokers may not appear consecutively.
- A player may extend the beginning or end of a straight flush if the added card continues the sequence.

Examples:

```text
A♠ 2♠ 3♠
4♥ 5♥ 6♥ 7♥
```

Non-examples:

```text
Q♠ K♠ A♠     // invalid because aces are not high
K♠ A♠ 2♠     // invalid because straight flushes are not cyclic
```


### Meld Validation

The process of determining whether a selected group of cards forms a structurally valid La Kika meld.

Milestone 3 introduced a `MeldValidator` interface and La Kika-specific validator implementation.

Validation returns more than true/false. It can include:

- Meld type.
- Normalized card order.
- Joker assignments.
- Error codes.

### Normalized Meld Order

The canonical card order returned by validation or used by presentation when cards are placed into the play area.

For straight flushes, this means sequence order.

Decision:

- Players may select straight-flush cards in any order.
- Meld validation answers whether those cards can structurally form a straight flush.
- Presentation/meld placement arranges the played straight flush into normalized sequence order.
- Kind melds preserve selected/insertion order.

This is a deliberate domain/presentation boundary:

```text
Meld validation = can these cards form a valid meld?
Meld presentation = how should this valid meld be displayed?
```

### Meld Validation Error

A structured reason why selected cards do not form a valid meld.

Examples include:

- Too few cards.
- Too many jokers.
- Mixed ranks.
- Mixed suits.
- Duplicate sequence rank.
- Non-consecutive sequence.
- Consecutive jokers.

### Joker Assignment

A computed interpretation of what a joker represents inside a validated meld.

For a kind meld, the joker assignment usually has an assigned rank but no assigned suit.

For a straight flush, the joker assignment has both assigned rank and assigned suit.

Joker assignments are not stored on the `Card` itself. The same physical joker may mean different things in different melds.

### Valid Meld

A meld whose card structure satisfies La Kika's combination rules.

Examples:

- Three or more cards of the same rank.
- A straight flush of three or more cards.
- A meld with jokers that satisfies joker ratio and placement restrictions.

A valid meld may still be illegal to play if the game state does not allow it.

### Legal Move

A move that is permitted in the current game state.

A move can be structurally valid but not legal.

Examples:

- A valid meld may be illegal if the player has not yet satisfied the round opening requirement.
- A joker stealing action may be illegal if the player cannot immediately play the stolen joker.
- A discard may be illegal if it occurs before the required draw/castigo phase.

### Move

A move is creating or mutating a meld.

Examples:

- Creating a new meld.
- Extending an existing meld.
- Replacing a joker in a meld.
- Stealing a joker from a meld.
- Using a stolen joker during the same turn.

Design note:

- Moves happen sequentially during the play/meld phase of a turn.
- A player may perform multiple meld mutations during one turn.

### Play Area

The shared area containing all melds currently in play.

Players typically play melds in front of themselves physically, but for rules purposes the play area is shared because players may mutate any meld.

Decision:

- Do not model each player's in-front-of-them physical area separately from the rules-level play area.

### Discard Pile

The place where discarded cards go.

Known La Kika rules:

- A player must discard one card at the end of their turn.
- The final card should be discarded to end a round.
- Only the most recently discarded card can be taken through castigo.

### Final Discard

The customary final action that ends a round.

Known La Kika rule:

- A player should discard their final card to end the round.
- The final card does not need to be played into a meld.
- If a player could play their final card during the meld phase, they still should discard the final card instead.

### Castigo

A special draw action. The word means “punishment” in Spanish.

A castigo occurs when a player takes the most recently discarded card and also draws three additional cards from the deck.

Known rules:

- Only the most recently discarded card may be taken.
- Only one castigo may occur per turn.
- The active player has the first chance to take the castigo.
- If the active player declines, the next player in turn order may choose to take it.
- This continues in player turn order until someone takes it or all eligible players decline.
- A player who has not opened may take castigo.
- Each player has 10 castigos per game.
- Taking a castigo consumes one castigo.
- Declining a castigo does not consume one.
- Castigos reset between games, but not between rounds.


### Stealing a Joker

The domain term for taking and replacing a joker from an existing meld is **stealing a joker**.

A player steals a joker by replacing it with a valid card and taking the joker into their temporary turn state.

Rules:

- A player may steal more than one joker in a turn.
- A stolen joker may be used to create a new meld or mutate any meld in the play area.
- A stolen joker must be played during the same turn.
- If the stolen joker is not played during the same turn, it must be returned.
- If a closed player steals a joker, they must open that turn and must still play the stolen joker that turn.

### Joker Obligation

A same-turn obligation created when a player steals a joker.

The obligation is not satisfied until the stolen joker is legally played into a meld during that same turn.

If the obligation is not satisfied, the joker must be returned to its source meld.

### Turn

One player's opportunity to act.

A La Kika turn has three phases:

1. Draw or castigo phase.
2. Meld play / meld mutation phase.
3. Discard phase.

During the meld play/meld mutation phase, the player may create or mutate multiple melds.

### Round

A sequence of turns ending when a player empties their hand by discarding their final card.

Known La Kika rules:

- Each player begins with 13 cards.
- The game has six rounds.
- Each round has an opening requirement.
- A player cannot play freely until they satisfy that round's opening requirement.
- Once a player opens, they may immediately continue playing and mutating melds during the same turn.

### Opening

The act of satisfying the current round's opening requirement.

Decision:

- Use the term **opening**.
- Avoid “going down” and “laying down” unless Raul later decides those are better table-language terms.

### Opening Requirement

The pre-condition a player must satisfy before freely creating or mutating melds in a round.

The six round opening requirements are:

1. One three-of-a-kind.
2. Two three-of-a-kind melds.
3. One four-of-a-kind.
4. Two four-of-a-kind melds.
5. One five-of-a-kind.
6. One straight flush of eight cards.

Jokers may be used in opening melds if the resulting melds are legal.

For Round 6, the eight-card straight flush may include jokers if:

- Jokers are no more than half of the meld.
- Jokers are not consecutive.
- The meld is otherwise legal.

### Opened Player

A player who has satisfied the current round's opening requirement.

Once opened, the player may create and mutate melds during the same turn and future turns in the round.

### Closed Player

A player who has not yet satisfied the current round's opening requirement.

A closed player may not freely create or mutate melds until opening.

Known exception:

- A closed player may take castigo.
- If a closed player steals a joker, they must open during that same turn.

### Game

A sequence of six rounds.

The player with the lowest cumulative score after the sixth round wins.

### Player

A participant in the game.

Known La Kika rules:

- The game supports 2-4 players.
- Each player has one hand.
- Each player accumulates score across rounds.
- Lowest cumulative score wins.
- Players take turns being dealer according to turn order.

### Dealer

The player responsible for shuffling and preparing cards to be dealt at the beginning of a round.

Known La Kika rules:

- Dealer responsibility rotates according to turn order.
- At the beginning of each round, the dealer shuffles the deck and takes cards from the top of the deck all at once to create the deal packet.
- Cards are dealt one at a time to each player in turn order, starting with the player next to the dealer.
- The player next to the dealer takes the first turn after the deal.
- If the dealer takes exactly the number of cards needed for the deal, the dealer receives a 100-point score reduction.
- If the dealer takes too few cards, the remaining cards are dealt directly from the deck and the dealer's score is unaffected.
- If the dealer takes too many cards, the extra cards are placed back on top of the deck and the dealer's score is unaffected.

### Deal Packet

The group of cards the dealer takes from the top of the deck all at once before dealing.

Known La Kika rules:

- The dealer attempts to take exactly enough cards to deal the round.
- If the deal packet is exact, the dealer receives the exact deal bonus.
- If the deal packet is short, the remaining cards are dealt directly from the deck.
- If the deal packet is long, extra cards are returned to the top of the deck.

Digital design note:

- The current preferred UI analogy is a shot/swing meter.
- The dealer stops the meter, and the meter result determines the deal packet size.
- The domain model should receive only the resulting deal packet size, not depend on the UI meter itself.

### Exact Deal Bonus

A scoring bonus awarded to the dealer when the dealer takes exactly the number of cards needed for the round deal.

Formula:

```text
required cards = hand size * number of players
```

Known La Kika rules:

- If the dealer takes exactly `hand size * number of players` cards, subtract 100 points from the dealer's score.
- The bonus is applied immediately at the beginning of the round.
- Negative scores are possible.

### Score

A numerical penalty value associated with cards remaining in a player's hand at the end of a round.

Known point values:

```text
2-7     = 5 points
8-King  = 10 points
Ace     = 20 points
Joker   = 50 points
```

Lower score is better.

Score modifiers may reduce score, including the exact deal bonus.


### Scene

The full FXGL/JavaFX game scene.

The current scene is divided into a left HUD region and a right gameplay region.

### HUD Area

The left 20% of the scene.

This area is reserved for heads-up display information such as current round, active player, dealer, score, opening requirement, turn phase, castigo availability, and selected-card or selected-meld feedback.

### Gameplay Area

The right 80% of the scene.

This area is divided vertically into four regions:

```text
Top 30%       = opponent played melds
Next 30%      = player played melds
Next 30%      = player hand
Bottom 10%    = play buttons
```

### Opponent Meld Area

The top 30% of the gameplay area.

This area displays melds currently associated visually with opponents.

Rules note:

- At the domain level, melds are part of a shared play area.
- At the presentation level, the UI may visually distinguish opponent melds from player melds.

### Player Meld Area

The second 30% of the gameplay area.

This area displays melds visually associated with the local/current player.

Current issue:

- Played cards currently animate near the upper-left corner instead of landing in this region.

### Player Hand Area

The third 30% of the gameplay area.

This area displays the current player's hand.

### Play Button Area

The bottom 10% of the gameplay area.

This area contains buttons for player actions such as playing selected cards, drawing, discarding, or future turn-phase actions.

### Debug Layout Overlay

A development overlay used to verify scene-region boundaries.

The overlay is useful while the UI layout is still evolving and should remain a development aid rather than a domain concept.



### Local Hot-Seat Play

The prototype multiplayer mode is local hot-seat play.

Rules/UX direction:

- The local/current player is always displayed at the bottom.
- Only the active player's hand should be visible during normal gameplay.
- Opponent/non-active hands should be hidden.
- Hidden hands may be exposed through a development-only debug overlay.
- Long-term, the game should use a pass-device screen between turns.

### Debug Hand Overlay

The overlay now reads from real `GameState` player hands. It no longer uses mock hand data.

A development-only overlay that can reveal hidden player hands for debugging.

This should not be part of normal gameplay and should not affect domain rules.

### Pass-Device Screen

A future hot-seat transition screen that hides sensitive hand information between turns.

Intended flow:

```text
Player turn ends
Screen hides hands
Prompt: Pass device to next player
Next player confirms
Next player's hand becomes visible
```

### Player Colors

The current fixed player color palette is:

```text
Player 1 = blue
Player 2 = red
Player 3 = gold
Player 4 = green
```

These colors may be used for HUD names, turn indicators, dealer indicators, meld creator grouping, and full-table rows.

### Castigo Remaining Display

The HUD should display castigos as remaining uses:

```text
Castigos: X remaining
```

Text is sufficient for now. A chip/icon style may be added later.


### Player HUD Row

A visual row in the HUD that summarizes one player's public game state.

Milestone 4B introduced a prototype row for each player.

Current row information:

- Player name.
- Cumulative score.
- Cards remaining.
- Castigos remaining.
- Opened/closed visual status.
- Dealer chip.
- Active-turn arrow.

### Dealer Chip

A small visual indicator in the HUD showing which player is currently the dealer.

Current prototype:

```text
D chip beside the dealer's HUD row
```

### Active-Turn Arrow

A visual indicator in the HUD showing whose turn is currently active.

Current prototype:

```text
Arrow shown on the active player's HUD row
```

### Castigos Remaining

The number of castigos a player may still take during the current game.

Known rule:

- Each player starts a new game with 10 castigos.
- Taking a castigo consumes one.
- Declining a castigo does not consume one.
- Castigos reset between games, not between rounds.

Current HUD text:

```text
Castigos: X remaining
```

### Player HUD State

A presentation-facing data object used by the HUD to render player information.

It is not the final domain `PlayerState`. It is a view model for the HUD.

Current code concept:

```java
PlayerHudState
```



### Player HUD Model

A presentation-facing model that stores the data currently rendered by the HUD.

This is not the final domain `PlayerState`; it is a view model used by the UI.

Current flow:

```text
PlayerHudModel -> GameHudController -> GameHUD
```

Later, real game state should feed this model through an adapter.

### Game HUD Controller

A presentation controller that updates `PlayerHudModel` and tells `GameHUD` to render the latest state.

This exists to keep static HUD calls and direct UI manipulation out of card interaction components.


### Visual Meld

A presentation-layer grouping of cards that should be displayed as one meld.

A visual meld records the player who originally created it so melds can remain visually grouped under their creator, even though the rules-level play area is shared.

Current implementation note:

- `VisualMeld` is a presentation helper, not the final domain aggregate.
- Later, it should be fed by `PlayArea` / `GameState`.

### Meld Layout

The presentation algorithm that decides where played melds and cards appear inside a play-area region.

Current layout priorities:

1. Preserve meld creation order left-to-right.
2. Compress card spacing and meld gaps before wrapping.
3. Wrap melds to additional rows before shrinking cards.
4. Use slight vertical row overlap when needed.
5. Avoid card scaling unless layout pressure requires it later.

### Staggered Card Animation

A card movement animation style where cards move to their final positions one at a time instead of all at once.

Current starting value:

```text
0.1 seconds between cards
```



### Meld Layout Debug Harness

A development-only toolset for stress-testing played-meld layout before the full turn, draw, discard, and game-state systems exist.

Current controls:

- `+Kind`: adds a test kind meld.
- `+Run`: adds a test straight flush meld.
- `Stress`: adds multiple test melds to pressure-test wrapping, overlap, centering, and staggered animation.
- `Clear`: removes the visual test melds from the played area.

This is not a domain concept and should not influence La Kika rules.


### Animation Settings

Player-facing or development-facing settings that control animation timing and speed.

Current default:

- Played-card animation uses a 0.1-second stagger between cards.

Future direction:

- Animation speed should be configurable from the settings screen.
- Code should centralize animation defaults instead of scattering hardcoded values.

### House Rules

Configurable La Kika rule variations.

House rules should be represented explicitly in the rules/game-state layer rather than hidden in UI code or scattered constants.

Known example:

- Castigo draw count may vary depending on whether the player taking castigo is the active player.

## Technical / Architecture Terms

### Engine

Reusable, game-agnostic code for representing cards, collections, rules, turns, snapshots, and potentially actions.

Engine code should not depend on JavaFX or FXGL.

### Application

The specific playable game built using the engine. This may depend on FXGL, JavaFX, textures, animation, input, and game-specific rules.

### Entity

An FXGL object in the game world. A card entity is the visual/interactive representation of a domain `Card`.

Important distinction:

```text
Card = model
Entity = FXGL object
CardComponent = bridge between Entity and Card
```

### Component

An FXGL behavior/data object attached to an entity.

Examples:

- `CardComponent` attaches a domain `Card` to an entity.
- `CardAnimationComponent` currently handles interaction and animation, but may later be split.

### Snapshot

A serializable representation of game state used for save/load.

Example:

```java
CardSnapshot snapshot = card.toSnapshot();
Card restored = Card.fromSnapshot(snapshot);
```

Snapshots should be simple data, suitable for JSON.


### HandModel

A pure hand/domain state object introduced during Milestone 4A.

It owns hand cards, selected cards, and selectability state, but should not know about FXGL entities, textures, animation, or scene coordinates.

### HandLayout

A presentation-layer layout calculator introduced during Milestone 4A.

It computes visual card positions for the hand while hiding centering, spacing, compression, and selected-card lift math behind a small interface.

### CardEntityRegistry

A presentation-layer mapping from domain card identity to FXGL entity.

It keeps the rule intact that `Card` does not own or know about an FXGL `Entity`.

### PlayedMeldLayout

A presentation-layer layout calculator for played meld positions.

Current behavior:

- Centers played meld groups in the player meld area.
- Keeps separate played melds visually distinct.
- Lays out straight flushes using normalized order.
- Preserves selected/insertion order for kind melds.

Long-term, this should evolve into a fuller `MeldLayout` / `PlayAreaView` system.

### SelectionFeedback

A presentation feedback interface introduced during Milestone 4A.2.

It decouples card animation/input behavior from the HUD and meld validation details.

Current implementation:

```text
CardAnimationComponent -> Hand -> SelectionFeedback -> HUD
```

This is transitional. A future event/property-based UI update system should replace static HUD calls.

### Deep Module

A module with a small, simple public interface and substantial hidden implementation behind it.

Example goal:

```java
game.startRound();
game.draw(playerId);
game.playMove(playerId, move);
game.discard(playerId, cardId);
```

The caller should not need to know how validation, state transitions, joker substitution, scoring, or entity updates happen internally.

## Terms to Avoid or Use Carefully

### `cardIndex`

Old implementation detail. Avoid using this as a domain concept.

### `deckNumber`

Removed from the current design. We use `CardId` to identify physical cards instead.

### Poker Hand

Avoid this term for La Kika rules.

The prototype used poker-hand evaluation, but La Kika only allows:

- Three-of-a-kind or more.
- Straight flushes.

### Played Cards

Use carefully.

Raul previously used “played cards” to describe cards on the table, but the preferred domain term is now **meld** when referring to a specific valid group of cards, and **play area** when referring to the table area containing all melds.

### Entity-owned Card vs Card-owned Entity

Use: entity owns a component that references a card.

Avoid: card owns an FXGL entity.

## Open Vocabulary Questions

No urgent vocabulary questions are currently open.

Possible future vocabulary refinements:

1. Should the exact deal bonus have a traditional table name?
2. Should the process of adding a new shuffled standard deck after exhaustion have a domain name?
3. Should the digital meter action be called **deal preparation**, **cut**, **deal packet selection**, or something else?

### Card View Factory

A presentation-layer factory that creates the JavaFX visual node for a card.

Design invariant:

```text
Card entity position = top-left corner of the rendered card.
```

The card view should be created at its final rendered size, not visually scaled with JavaFX transform scaling. This keeps entity coordinates, layout coordinates, hitboxes, and visible card bounds aligned.


### Deck / Discard Panel

A HUD presentation area showing the discard pile and the deck.

Current design decisions:

- The deck appears to the left of the discard pile.
- The panel lives in the HUD beneath player information and above selected-meld feedback.
- The deck uses a Balatro-style stacked-card visual.
- The card-back sprite comes from the upper-left sprite in `card-backs-enhancers-seals.png`.
- Deck stack offset is currently 2 px.
- Lower deck cards are darkened to improve the illusion of depth.
- The DRAW overlay appears as a centered dark button on the deck.
- The discard pile grays out when castigo is unavailable.

### Castigo Decision Timer

A visual countdown for the limited time a player has to choose whether to take a castigo.

Known rule:

- A player has no more than five seconds to decide.
- Timer expiration means automatic pass.
- Active player chooses between draw and castigo.
- Out-of-turn players choose between castigo and pass.

The current UI direction is a right-to-left gray countdown effect consistent with the game's button style.

### Title Splash

A rough introductory screen shown before the main menu.

Current visual direction:

- Large `LA KIKA` text.
- Font size approximately 25% of screen height.
- Title drops in from the top.
- Title fades out.
- Main menu appears automatically.

### Main Menu

The screen shown after the title splash.

Current buttons:

- Play
- Settings
- Rules

Current behavior:

- Play starts the prototype game.
- Settings and Rules are placeholders.


### Card Wiggle Component

A presentation component responsible for stateful card wiggle animation.

Design principle:

```text
Hover changes amplitude and speed; it does not restart the animation.
```

This preserves phase continuity and prevents the card from jumping to a new angle when the mouse enters or exits.


### Full Play-Area View

A read-only overlay that shows all players' played melds simultaneously.

Current prototype notes:

- It is toggled with the `Table` button.
- It shows one row per player.
- It uses mock meld data until the real `PlayArea` exists.
- It is intended for inspection and layout testing, not interaction.


### GameState

The rules-level source of truth for the current game.

It should know what is true in the game, but it should not know how anything is drawn.

### PlayerState

Rules-level state for one player.

Current fields include:

- player id
- display name
- hand
- cumulative score
- castigos remaining
- opened status

### RoundState

Rules-level state for the current round.

Current fields include:

- round number
- dealer
- active player
- turn phase

### TurnPhase

The current phase of the turn.

Milestone 5A introduces this vocabulary. Later milestones will enforce legal actions by phase.

### GameController

The controlled mutation boundary for `GameState`.

Current responsibility:

- initialize a prototype game
- deal initial hands
- expose HUD state

Future responsibility:

- validate and apply `GameAction` objects
- produce events for the presentation layer


### GameAction

A rules-level request to change the game.

Example:

```java
new DrawFromDeckAction(playerId)
```

### ActionResult

The result of asking `GameController` to apply an action.

An action result can be successful or failed. Successful results may contain domain events.

### GameEvent

A rules-level fact produced by applying an action.

The presentation layer uses game events to decide what to animate.

### DrawFromDeckAction

A request for the active player to draw one card from the deck.

Current rules:

- Only the active player may draw.
- Drawing is allowed only during `DRAW_OR_CASTIGO`.
- A successful draw advances the phase to `MELD`.

### CardDrawnEvent

An event emitted after a card has been drawn into a player's rules-level hand.


### DiscardPile

The rules-level pile of discarded cards.

Current important behavior:

- The top card is the only card currently relevant for castigo.
- The discard pile belongs to `GameState`.

### DiscardAction

A request for the active player to discard one card by `CardId`.

Current prototype rules:

- The active player must be the one discarding.
- Discard currently succeeds only after the player has drawn.
- UI requires exactly one selected card before submitting the action.

### CardDiscardedEvent

An event emitted after a card moves from a player's rules-level hand to the rules-level discard pile.


### ActivePlayerChangedEvent

A game event emitted when the active player changes.

The UI uses this event to switch the visible hand to the new active player.

### Turn Transition

The rules-level movement from one turn state to the next.

Current prototype transition after discard:

```text
current player's MELD phase
→ discard
→ next player's DRAW_OR_CASTIGO phase
```


### PlayArea

The rules-level collection of melds currently in play.

Current scope:

- stores created melds
- does not yet handle mutation
- does not yet handle joker stealing

### MeldState

A rules-level meld.

Current fields:

- creator player
- meld type
- cards in the meld

### CreateMeldAction

A request for the active player to create a new meld from selected cards.

### MeldCreatedEvent

An event emitted after a valid meld is added to the rules-level `PlayArea`.


### GameActionPresentationAdapter

A presentation-layer adapter that translates `ActionResult` and `GameEvent` objects into UI updates and animations.

Current responsibilities:

- animate drawn cards into the visible hand
- animate discarded cards into the discard pile
- animate created melds into the played area
- switch visible hand after active player changes
- refresh HUD and deck/discard panel after successful actions

This class does not validate rules. Rule validation belongs to `GameController`.


### Hand Order

The current ordering of cards in a player's hand.

Hand order is now represented by `PlayerState.hand`, so the visible hand and debug hand overlay can agree.

### Custom Hand Order

A saved preferred ordering for a player's hand.

Current behavior:

- initial custom order is the dealt order
- click `Order` to restore saved custom order
- click-and-hold `Order` to save current hand order as custom order
- newly drawn cards that are not in saved custom order are appended when restoring

### Debug Drawer

Milestone 5H implementation: a right-side development-only drawer launched from the `Debug` button.

A planned collapsible side panel for development-only tools.

It differs from a full overlay:

- drawer: compact tool access
- overlay: large inspection screen

The intended design is hybrid: use the drawer to launch debug overlays.

### Custom Order Saved Feedback

A short visual confirmation that the player's current hand order has been saved as their custom order.

Current prototype behavior:

- visible hand cards briefly pop/wiggle in sequence
- the animation is presentation-only
- the saved order itself lives in `PlayerState`

### Order Button Confirmation

A short pop/wiggle animation on the `Order` button confirming that the custom order command was accepted.

Current behavior:

- click `Order` = restore saved custom order and animate the button
- click-and-hold `Order` = save current custom order and animate the button

### Pass-Device Overlay

A player-facing hot-seat privacy overlay shown between turns.

Current behavior:

- appears after discard-driven active-player change
- hides the visible hand
- prompts the next player to press `READY`
- renders the next player's hand only after confirmation

This differs from the debug hand overlay, which is development-only and intentionally exposes all hands.

### Played-Meld Perspective

The active-player-relative way the table is displayed.

Current behavior:

- the current active player's melds appear in the current-player meld area
- one opponent's melds appear in the opponent meld area
- the default visible opponent is the next player after the active player

This is a presentation concept, not a turn-rule concept.

### Action Failure Code

A machine-readable reason an action failed.

Current examples:

- `NOT_ACTIVE_PLAYER`
- `WRONG_TURN_PHASE`
- `CARD_NOT_IN_HAND`
- `INVALID_MELD`
- `INVALID_HAND_ORDER`

The user-facing explanation remains in `ActionResult.message()`.

### Turn Rules

Shared rules for whether an action may occur in the current turn context.

Milestone 6A introduces `TurnRules` for active-player and phase checks.

### Turn Rules Foundation

The first Milestone 6 slice. It centralizes simple turn legality before adding more complex rules such as opening requirements and castigo timing.
