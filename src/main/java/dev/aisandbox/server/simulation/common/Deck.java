/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents a deck of playing cards. The deck can be created with a variable number of suits and
 * contains all possible card values for each suit.
 */
public class Deck {

  /**
   * The collection of cards currently in the deck.
   */
  private final List<Card> cards = new ArrayList<>();

  /**
   * Creates a standard deck with 4 suits.
   */
  public Deck() {
    this(4);
  }

  /**
   * Creates a deck with the specified number of suits. Each suit will contain all possible card
   * values.
   *
   * @param suits The number of suits to include in the deck
   */
  public Deck(int suits) {
    // Iterate through each suit up to the specified limit
    for (int i = 0; i < suits; i++) {
      Card.CardSuite suite = Card.CardSuite.values()[i];
      // For each suit, add all possible card values
      for (Card.CardValue value : Card.CardValue.values()) {
        cards.add(new Card(value, suite));
      }
    }
  }

  /**
   * Shuffles the cards in the deck using the provided random number generator.
   *
   * @param random The random number generator to use for shuffling
   */
  public void shuffle(Random random) {
    Collections.shuffle(cards, random);
  }

  /**
   * Removes and returns the top card from the deck.
   *
   * @return The top card from the deck
   * @throws java.util.NoSuchElementException if the deck is empty
   */
  public Card getCard() {
    return cards.removeFirst();
  }
}
