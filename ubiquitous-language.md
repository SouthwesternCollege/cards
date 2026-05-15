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
- A player may take a joker from a meld if they can replace it according to the rules.
- A joker taken from a meld must be played during the same turn.
- Jokers are worth 50 points when left in a player's hand at the end of a round.

Joker replacement rules:

- In a three-of-a-kind-or-more meld, a player may take a joker by replacing it with a card of the required rank.
- In a straight flush meld, a player may take a joker by replacing it with the exact rank and suit represented by the joker.

### Deck

A collection from which cards are drawn.

In this game, a deck may consist of multiple standard 52-card decks and jokers.

Current code concept:

```java
Deck deck = new Deck(numberOfStandardDecks, jokersPerDeck);
```

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
- A joker replacement may be invalid if the player cannot immediately play the taken joker.
- A discard may be illegal if it occurs before the required draw/castigo phase.

### Move

A move is creating or mutating a meld.

Examples:

- Creating a new meld.
- Extending an existing meld.
- Replacing a joker in a meld.
- Taking a joker from a meld.
- Using a joker taken earlier in the same turn.

Design note:

- Moves happen sequentially during the play/meld phase of a turn.
- A player may perform multiple meld mutations during one turn.

### Play Area

The area containing all melds currently in play.

Players typically play melds in front of themselves physically, but for rules purposes the play area is shared because players may mutate any meld.

### Discard Pile

The place where discarded cards go.

Known La Kika rules:

- A player must discard one card at the end of their turn.
- Only the most recently discarded card can be taken through castigo.

### Castigo

A special draw action. The word means “punishment” in Spanish.

A castigo occurs when a player takes the most recently discarded card and also draws three additional cards from the deck.

Known rules:

- Only the most recently discarded card may be taken.
- Only one castigo may occur per turn.
- The active player has the first chance to take the castigo.
- If the active player declines, the next player in turn order may choose to take it.
- This continues in player turn order until someone takes it or all eligible players decline.

### Turn

One player's opportunity to act.

A La Kika turn has three phases:

1. Draw or castigo phase.
2. Meld play / meld mutation phase.
3. Discard phase.

During the meld play/meld mutation phase, the player may create or mutate multiple melds.

### Round

A sequence of turns ending when a player empties their hand.

Known La Kika rules:

- Each player begins with 13 cards.
- The game has six rounds.
- Each round has an opening requirement.
- A player cannot play freely until they satisfy that round's opening requirement.
- Once a player opens, they may immediately continue playing and mutating melds during the same turn.

### Opening Requirement

The pre-condition a player must satisfy before freely creating or mutating melds in a round.

The six round opening requirements are:

1. One three-of-a-kind.
2. Two three-of-a-kind melds.
3. One four-of-a-kind.
4. Two four-of-a-kind melds.
5. One five-of-a-kind.
6. One straight flush of eight cards.

### Opened Player

A player who has satisfied the current round's opening requirement.

Once opened, the player may create and mutate melds during the same turn and future turns in the round.

### Closed Player

A player who has not yet satisfied the current round's opening requirement.

A closed player may not freely create or mutate melds until opening.

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

These are intentionally narrow unresolved vocabulary questions.

1. Should the act of satisfying the opening requirement be called **opening**, **going down**, **laying down**, or something else?
2. Should a player's in-front-of-them physical area be modeled separately from the shared rules-level play area?
3. What should we call the action of taking and replacing a joker: **joker theft**, **joker replacement**, **joker rescue**, or another domain term?
