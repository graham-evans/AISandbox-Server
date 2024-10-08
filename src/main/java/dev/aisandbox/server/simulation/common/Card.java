package dev.aisandbox.server.simulation.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

public record Card(CardValue cardValue, CardSuite cardSuite) {

    public String getLongDescription() {
        return cardValue.getLongValue() + " of " + cardSuite.name().toLowerCase();
    }

    public String getShortDrescription() {
        return cardValue.getShortValue() + cardSuite.getValue();
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum CardValue {
        ACE("A", "Ace",1,14),
        TWO("2", "2",2,2),
        THREE("3", "3",3,3),
        FOUR("4", "4",4,4),
        FIVE("5", "5",5,5),
        SIX("6", "6",6,6),
        SEVEN("7", "7",7,7),
        EIGHT("8", "8",8,8),
        NINE("9", "9",9,9),
        TEN("10", "10",10,10),
        JACK("J", "Jack",11,11),
        QUEEN("Q", "Queen",12,12),
        KING("K", "King",13,13);

        private final String shortValue;
        private final String longValue;
        private final int valueAceLow;
        private final int valueAceHigh;
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
