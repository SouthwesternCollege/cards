# Core Domain Model
- What is a Meld? A meld is a group of three or more cards that must be a three-of-a-kind (or more) or a straight flush.
- What is a Move? A move is creating or mutating a meld.
- What is a Turn? A turn has three phases:
  1. Draw a card from the deck or take a castigo. If you do not take a castigo, then other players may take a castigo according to player turn order. There can olny be one castigo per turn. In other words, a player can only take the last discarded card. 
  2. Play a meld or mutate a meld.
  3. Discard a card.
- What is a Round? A round is a sequence of turns that terminates once a player has no more cards to play.
- What is a PlayArea? The play area is the cards that are currently in play.
- What actions may mutate melds? A player may add to a three-of-a-kind (or more) meld if the played card is of the same rank. A player may add a card to the beginning and end of a straight flush if it continues the straight flush. You could also take a joker from a meld; we discussed this , but ask me more questions if you need clarification

# Design Questions
- Are melds owned permanently by players? No, players can mutate any meld in the play area.
- Can multiple melds be modified in one turn? Yes.
- Are actions atomic or sequential? I believe actions are atomic. For example, a player can take a joker, mutate a meld, then use the joker, then mutate another meld, etc.
- Does opening consume the entire turn? No, opening satisifies the "pre-condition". A player can begin playing melds or mutating melds immediately after opening.
- Are straight flushes cyclic? No.
- Are aces high, low, or both? aces are low.

# Open Product Questions
1. May a player discard their final card to end the round, or must the final card be played into a meld? The final card should be discarded.
2. What happens if the deck has fewer than three cards when a castigo requires drawing three additional cards? If the deck has fewer than three cards, then we can either add another deck (shuffled of course) or shuffle the discard pile into the deck, if the discard pile is big enough.
3. What happens if the deck is exhausted during normal draw? Same as the previous question. I think that the best solution for now is to add a new standard deck to the game deck.
4. Can a player take more than one joker from melds in a single turn? Yes.
5. Must a taken joker be used in a new meld, or may it be used to mutate an existing meld? A taken joker can be used to create a meld or mutate any meld in the play area.
6. When a player opens with multiple melds, may those melds include jokers? Yes.
7. For Round 6, does the eight-card straight flush opening requirement allow jokers? Yes, as long as the meld is legal. Remember, no consecutive jokers in a straight flush and jokers must be no more than half of the meld
8. If a player has not opened, may they take a castigo? Yes.
9. If a player has not opened, may they replace/take a joker, or is that considered a meld mutation that requires opening first? If a player has not opened and they take a joker, then they must open that turn.

There is one last game mechanic that affects scoring. Each player takes turns being the dealer, according to turn order. At the beginning of each round, the dealer shuffles the deck and takes cards from the top of the deck to be dealt. The cards are dealt one at a time to each player, in turn order, starting with the player next to the dealer. If the cards are dealt exactly (meaning that the dealer took exactly (hand size)*(number of players) cards), then 100 points are subtracted from the dealer's score. This means that a negative score is possible.

# Open Vocabulary Questions
1. Should the act of satisfying the opening requirement be called **opening**, **going down**, **laying down**, or something else? I like the term opening.
2. Should a player's in-front-of-them physical area be modeled separately from the shared rules-level play area? No.
3. What should we call the action of taking and replacing a joker: **joker theft**, **joker replacement**, **joker rescue**, or another domain term? We call it "stealing" a joker.

# ISS-017 Questions
- May the final card be discarded to end the round? Yes, it should be discarded.
- Must the final card be played into a meld? No, it is custom to discard the final card.
- If a player plays all cards during the meld phase, is discard skipped? No, the even if you can play the final card, you should discard the last card.


# Open Product Questions
1. How many standard decks and jokers are used at the start for 2, 3, and 4 players? We always start with at least 2 standard decks and jokers in the early rounds and add up to four decks in the later rounds. I will speak with other game experts to try to formulate this better.
2. When adding a new shuffled standard deck after exhaustion, are jokers added too, or only the 52 standard cards? Jokers are alseo added. 
3. During exact deal preparation, how does the dealer choose how many cards to take: manual choice, random cut, or simulated physical cutting? The dealer takes cards from the top of the deck all at once. This is one of the game mechanics that I have been struggling with, but I think a good digital analogy would be like a shot/swing meter in sports games. The dealer would select when to stop the meter, which determines how many cards are taken from the deck to deal.
4. If the dealer takes too few cards for the deal, what happens? Then the remaining cards to be dealt are dealt directly from the deck and the dealers score is unaffected.
5. If the dealer takes too many cards for the deal, what happens to the extra cards? Then the extra cards are placed back on top of the deck and the dealer's score is unaffected.
6. Is the exact deal bonus applied immediately at the beginning of the round or during end-of-round scoring? The bonus is applied immediately.
7. Does the player after the dealer always take the first turn after the deal? Yes.

# Milestone 3 Questions
1. Minimum straight flush length. Like all melds, the minimum straight flush length is 3.
2. Can a three-of-a-kind meld contain duplicate physical cards with same suit/rank? Yes.
3. Can a straight flush contain duplicate rank/suit cards from multiple decks? No.
4. Are jokers allowed in opening melds for every round? Yes, as long as they do not comprise more than half of the meld nor be placed consecutively in a straight flush.
5. Joker ratio: “not more than half” means floor behavior? Yes.
6. Are all-joker melds always invalid? Yes.
7. Three-of-a-kind joker interpretation. Yes, your assumption is correct.
8. Can a kind meld have mixed non-joker ranks? No, `7♣ 7♦ 8♠ Joker` is not a valid meld.
9. Straight flush joker interpretation should be forced by gaps. Yes, your assumption is correct.
10. What about edge-extension with jokers? Your assumption is correct.
11. Are the cards inside a straight flush meld required to already be sorted? No, but they should be sorted once they are placed in the play area. This should be an animation concern.
12. For a straight flush with a joker, should the validator return the joker assignment? Yes.
13. Can straight flush validation infer from unordered cards with jokers? Yes.
14. Ambiguous straight flush with jokers. In terms of the game, this is not a big issue since a player can move the joker to the beginning or the end of the meld before the end of the turn. I like your recommendation. Validator returns valid if at least one interpretation exists, and chooses the lowest possible sequence.
15. Ambiguous kind melds are less problematic. Your accumption is correct, This is not an issue.
16. Validation result: should it include error codes? Yes.
17. Should Milestone 3 remove PokerHandEvaluator? I think it might be good to keep for the engine since power hands are very common in many games, but it is not necessary for this game. So, its use should be removed from the game, but it may be part of an engine module later.
18. Should Milestone 3 update live selection feedback? Yes. This might be helpful for testing, too.

# Open Product Questions
1. What is the exact starting deck composition by player count and round? Start with two standard decks and jokers. Add decks when necessary.
2. When adding a new shuffled deck after exhaustion, how many jokers are included with the added 52 standard cards? Two jokers per standard deck.
3. How should the dealer's digital shot/swing meter be tuned so that the exact deal bonus is skill-based but not frustrating? I was thinking to just have a linear interpolation between the top and the bottom of the deck, indicating the number of cards to take, but do not include the number of cards in the meter, perhaps an arrow would suffice for now. I was imagining that later we could animate the deck so that it is split between the top and bottom and cards will move from the top to the bottom and you click on the deck or a button to stop the animation and thus select the cards to be dealt.

# Pre-Milestone 4 Clarifications
Before we move on to Milestone 4, I would like to have a conversation about the GUI and visual aspects of the game. Let me list as much of the visual details that I have considered so far:
- The HUD should contain information about each player including their score and number of castigos. There should also be some sort of visual indicator for the dealer.
- I don't think that I ever mentioned this, but there are only 10 castigos for the entire game. A player can use as many castigos as they want in any given round, as long as they have not exceeded their allotment of 10 castigos. We will need to alter the rules.
- In the opponent' play area, I have the idea of viewing only one player's played melds at a time and the player can scroll left or right to view other opponent's melds in a carousel style.
- There should be a maximum and minimum distance from one card to the next in played melds. There should be significant overlap for cards in a meld to visually represent a meld and space between melds to separate one meld from another. As the number of played melds grow, the melds should compress, but there should be minimum distances so that cards rank and suit are not obscured (15 pixel or 20% of the cards width should work as a minimum) and melds do not overlap. If compressing is not enough, then the size of cards should shrink. Melds should be centered within their play area.
- The play area gets crowded, especially in later rounds. There should be a mechanism for viewing the entire play area, without the HUD, hand, nor game controls. I am thinking of dividing the screen into equal parts with one row for each player.
- The hand should have stretch and compression as well. If there are many cards in a hand then compress the hand without obscuring rank or suit. If there are few cards in the hand then the distance between cards should be no more than 10 pixels or about 15% of the cards width.
- I have not decided where to place the deck and discard pile. I think that a good space would be at the bottom of the HUD or to the right of a player's hand. The deck itself and the discard pile could serve as buttons to draw a card take a castigo or discard a card.
- DePixelHalbfett should be used as a "global" font including a dropshadow. Notice how buttons also have that drop shadow. Also, I need to add the same style to the pause menu music volume slider. The buttons do not have a border, but they do have a drop shadow.
- I would like to change the loading screen and start developing a splash, title, and main menu screen. For now, I would like to take the animated La Kika title and turn it into its own screen which transitions into the main menu. The main menu should have play, settings, and rules buttons. I will work on settings and rules screens later, unless it is critical to complete before moving on.
- I would like to work on animating cards being dealt and moving throughout different areas later, but I would like there to be continuity. For example, currently there is a "wiggle" animation for all the cards and when the mouse hovers over a card then the wiggling intensifies. However, when the intensified wiggle begins it "jumps" to its starting angle instead of continuing from its current angle.
  This should bring up many more questions, so please ask me anything. I would like to have a couple of rounds of back and fourth before updating the project documents and moving on to milestone 4. We will need to revisit milestone 3 because there might be rules that need to be added or revised.

# Pre-Milestone 4 Questions
- Does each player have 10 castigos total, or are there 10 castigos shared by all players for the entire game? Each player has 10 castigos per game.
- Does taking a castigo consume one castigo, or does merely having the opportunity and declining affect anything? Only taking one consumes it.
- Should HUD show all players at once, or only current player plus compact opponent summaries? Yes, I would prefer that ALL players are shown at once.
- For each player, should we display: Name, Score, Cards remaining, Castigos used / remaining, Opened status, Dealer icon, Turn indicator? Yes, but there could be visual indicators for some of these. For example, the opened status could be indicated by the color of the player's name in the HUD. I was thinking a chip for the dealer, and an arrow for the turn indicator.
- Should scores show cumulative score only, or also current-round pending hand score? Cumulative score only.
- Since melds are rules-level shared and not owned, what does “opponent’s played melds” mean visually? A meld appears under the player who originally created it. Even if others mutate it, it stays visually in the creator’s area.
- Is this a temporary overlay while holding a key/button, or a toggle screen? I like the idea of a button toggle screen.
- 