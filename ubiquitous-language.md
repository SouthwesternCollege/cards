# Ubiquitous Language

This document defines the shared vocabulary for the card game and reusable card-game engine. The goal is to keep code, documentation, and design discussions using the same words for the same ideas.

## How to Use This File

- Add terms as soon as we discover ambiguity.
- Prefer domain language over implementation language.
- Keep implementation details separate from gameplay concepts when possible.
- Raul is the domain expert for game rules. When a term is uncertain, mark it as **Needs domain definition**.

## Domain Terms

### Card

A physical card that can move between collections such as a deck, hand, played area, or discard pile.

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

Open question: whether ace is always high, can be low, or depends on the rule being evaluated.

### Suit

The category of a standard card: clubs, diamonds, hearts, spades.

Open question: whether suits matter for this game beyond standard poker-like hand evaluation.

### Joker

A special card that acts as a wild card.

**Needs domain definition.**

Questions:

- Can a joker represent any rank?
- Can a joker represent any suit?
- Can multiple jokers represent the same card face?
- Are jokers always interpreted optimally for the player?
- Do jokers have a score value when left in hand?
- Can jokers be played outside normal ranked/suited combinations?

### Deck

A collection from which cards are drawn.

In this game, a deck may consist of multiple standard 52-card decks and jokers.

Current code concept:

```java
Deck deck = new Deck(numberOfStandardDecks, jokersPerDeck);
```

### Hand

A collection of cards held by a player.

Current code warning: `Hand` currently mixes domain responsibilities with FXGL rendering, animation, input, and layout. Long term, this should be split into a pure hand model and one or more visual/controller classes.

### Selected Cards

Cards in a hand that the player has selected for possible play.

Current constraint: selection appears limited to 5 cards.

Open question: is 5 a permanent rule or only inherited from poker-hand evaluation?

### Played Cards / Played Area

Cards that have been played from a hand during a turn or round.

Open question: after cards are played, do they go to a discard pile, remain in a played area, or leave the round entirely?

### Discard

A collection or area for cards that have been removed from active play.

**Needs domain definition.**

### Round

A segment of the game. The first player to empty their hand wins the round. Other players score based on the cards left in their hands.

Questions:

- How is a round initialized?
- How many cards does each player receive?
- What happens to played cards between turns?
- What ends a round besides a player emptying their hand, if anything?

### Game

A sequence of rounds played by multiple players.

**Needs domain definition.**

Questions:

- Does the game end after a fixed number of rounds?
- Does the game end when someone reaches a score threshold?
- Is low score good or high score good?

### Player

A participant in the game.

Current known requirement: multiplayer, turn-based.

Questions:

- Is multiplayer local, networked, AI-assisted, or all of these eventually?
- How many players are supported?
- Does each player have one hand?

### Turn

One player’s opportunity to act.

**Needs domain definition.**

Questions:

- What actions are allowed on a turn?
- Can a player draw, play, discard, pass, or rearrange cards?
- Does play continue clockwise, by priority, or by some other rule?

### Score

A numerical value associated with a player, round, or card collection.

Known rule: after a player empties their hand, other players score the sum of cards left in their hand.

Questions:

- What are the point values of numbered cards, face cards, aces, and jokers?
- Is the winner the lowest score or highest score?

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
hand.select(cardId);
hand.playSelected();
```

The caller should not need to know how selection is stored, how cards are ordered, or how validation happens internally.

## Terms to Avoid or Use Carefully

### `cardIndex`

Old implementation detail. Avoid using this as a domain concept.

### `deckNumber`

Removed from the current design. We use `CardId` to identify physical cards instead.

### Entity-owned Card vs Card-owned Entity

Use: entity owns a component that references a card.

Avoid: card owns an FXGL entity.

## Open Vocabulary Questions

1. What is the official name of the game?
2. What is a legal play called?
3. What is the group of cards played on a turn called?
4. Are there named combinations, like poker hands, runs, sets, books, or melds?
5. What is the exact role of jokers?
6. What is the correct word for the central area where cards are played?
7. Are players trying to minimize score, maximize score, or avoid penalty points?
