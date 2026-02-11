/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a standard playing card with a value and suit.
 *
 * <p>This record encapsulates the immutable properties of a playing card, including its rank
 * (value) and suit. It provides convenience methods for different card representations and
 * supports both low-ace and high-ace scoring systems common in different card games.
 *
 * <p>The card system supports:
 * <ul>
 *   <li>All standard card values from Ace through King</li>
 *   <li>All four suits (Hearts, Diamonds, Clubs, Spades)</li>
 *   <li>Multiple representation formats (long names, short codes, characters)</li>
 *   <li>Dual ace values for different game rules (low=1, high=14)</li>
 *   <li>Image path generation for card visualization</li>
 * </ul>
 *
 * <p>As a record, this class provides automatic implementations of {@code equals()},
 * {@code hashCode()}, and {@code toString()} methods, making it suitable for use in
 * collections and as map keys.
 *
 * <p>Example usage:
 * <pre>{@code
 * Card aceOfSpades = new Card(CardValue.ACE, CardSuite.SPADES);
 * System.out.println(aceOfSpades.getLongDescription()); // "Ace of spades"
 * System.out.println(aceOfSpades.getShortDrescription()); // "AS"
 * }</pre>
 *
 * @param cardValue the rank/value of the card
 * @param cardSuite the suit of the card
 *
 * @see CardValue
 * @see CardSuite
 */
public record Card(CardValue cardValue, CardSuite cardSuite) {

  // card sizes - from original PNG files
  /**
   * The width of a card image in pixels.
   */
  public static final int CARD_WIDTH = 240;
  /**
   * The height of a card image in pixels.
   */
  public static final int CARD_HEIGHT = 336;

  /**
   * Returns a human-readable description of this card. Example: "Ace of hearts"
   *
   * @return A string with the full value name and suit name
   */
  public String getLongDescription() {
    return cardValue.getLongValue() + " of " + cardSuite.name().toLowerCase();
  }

  /**
   * Returns a short description of this card. Example: "AH" for Ace of Hearts
   *
   * @return A string with the short value and suit characters
   */
  public String getShortDrescription() {
    return cardValue.getShortValue() + cardSuite.getValue();
  }

  /**
   * Returns the path to the image file for this card.
   *
   * @return A string with the path to the card's image file
   */
  public String getImageName() {
    return ("/images/cards/" + cardValue.charValue + cardSuite.value + ".png");
  }

  /**
   * Enumeration of possible card values. Includes properties like short/long names and numeric
   * values. Supports both low and high ace values for different game rules.
   */
  @Getter
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public enum CardValue {
    ACE("A", "Ace", 1, 14, 'A'), TWO("2", "2", 2, 2, '2'), THREE("3", "3", 3, 3, '3'), FOUR("4",
        "4", 4, 4, '4'), FIVE("5", "5", 5, 5, '5'), SIX("6", "6", 6, 6, '6'), SEVEN("7", "7", 7, 7,
        '7'), EIGHT("8", "8", 8, 8, '8'), NINE("9", "9", 9, 9, '9'), TEN("10", "10", 10, 10,
        'T'), JACK("J", "Jack", 11, 11, 'J'), QUEEN("Q", "Queen", 12, 12, 'Q'), KING("K", "King",
        13, 13, 'K');

    /**
     * Short text representation of the card value (e.g., "A" for Ace).
     */
    private final String shortValue;

    /**
     * Long text representation of the card value (e.g., "Ace").
     */
    private final String longValue;

    /**
     * Numeric value when ace is counted as low (1).
     */
    private final int valueAceLow;

    /**
     * Numeric value when ace is counted as high (14).
     */
    private final int valueAceHigh;

    /**
     * Character used in image filenames.
     */
    private final char charValue;
  }

  /**
   * Enumeration of the four standard card suits. Each suit has a character representation.
   */
  @Getter
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public enum CardSuite {
    HEARTS('H'), DIAMONDS('D'), CLUBS('C'), SPADES('S');

    /**
     * Character representation of the suit (H, D, C, or S).
     */
    private final char value;
  }
}
