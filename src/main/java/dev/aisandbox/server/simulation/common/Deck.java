package dev.aisandbox.server.simulation.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Deck {

  private final List<Card> cards = new ArrayList<>();

  public Deck(int suits) {
    for (int i = 0; i < suits; i++) {
      Card.CardSuite suite = Card.CardSuite.values()[i];
      for (Card.CardValue value : Card.CardValue.values()) {
        cards.add(new Card(value, suite));
      }
    }
  }

  public Deck() {
    this(4);
  }

  public void shuffle(Random random) {
    Collections.shuffle(cards, random);
  }

  public Card getCard() {
    return cards.removeFirst();
  }
}
