package dev.aisandbox.server.simulation.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Card {

    private final CardValue cardValue;
    private final CardSuite cardSuite;

    public String getLongDescription() {
        return cardValue.getLongValue() + " of " + cardSuite.name().toLowerCase();
    }

    public String getShortDrescription() {
        return cardValue.getShortValue() + cardSuite.getValue();
    }

    @Getter
    public enum CardValue {
        ACE("A", "Ace"),
        TWO("2", "2"),
        THREE("3", "3"),
        FOUR("4", "4"),
        FIVE("5", "5"),
        SIX("6", "6"),
        SEVEN("7", "7"),
        EIGHT("8", "8"),
        NINE("9", "9"),
        TEN("10", "10"),
        JACK("J", "Jack"),
        QUEEN("Q", "Queen"),
        KING("K", "King");

        private final String shortValue;
        private final String longValue;

        private CardValue(String shortValue, String longValue) {
            this.shortValue = shortValue;
            this.longValue = longValue;
        }

        public int getValue() {
            return ordinal() + 1;
        }

    }

    @Getter
    public enum CardSuite {
        HEARTS('H'), DIAMONDS('D'), CLUBS('C'), SPADES('S');

        private final char value;

        private CardSuite(char value) {
            this.value = value;
        }
    }
}
