package dev.aisandbox.server.simulation.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

public record Card(CardValue cardValue, CardSuite cardSuite) {

    // card sizes - from original PNG files
    public static final int CARD_WIDTH = 240;
    public static final int CARD_HEIGHT = 336;

    public String getLongDescription() {
        return cardValue.getLongValue() + " of " + cardSuite.name().toLowerCase();
    }

    public String getShortDrescription() {
        return cardValue.getShortValue() + cardSuite.getValue();
    }

    public String getImageName() {
        return ("/images/cards/"+cardValue.charValue + cardSuite.value + ".png");
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum CardValue {
        ACE("A", "Ace",1,14,'A'),
        TWO("2", "2",2,2,'2'),
        THREE("3", "3",3,3,'3'),
        FOUR("4", "4",4,4,'4'),
        FIVE("5", "5",5,5,'5'),
        SIX("6", "6",6,6,'6'),
        SEVEN("7", "7",7,7,'7'),
        EIGHT("8", "8",8,8,'8'),
        NINE("9", "9",9,9,'9'),
        TEN("10", "10",10,10,'T'),
        JACK("J", "Jack",11,11,'J'),
        QUEEN("Q", "Queen",12,12,'Q'),
        KING("K", "King",13,13,'K');

        private final String shortValue;
        private final String longValue;
        private final int valueAceLow;
        private final int valueAceHigh;
        private final char charValue;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum CardSuite {
        HEARTS('H'), DIAMONDS('D'), CLUBS('C'), SPADES('S');

        private final char value;

    }
}
