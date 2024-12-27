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

    public String getImageName() {
        return ("/images/cards/"+(cardValue.imageBase + cardSuite.imageDelta) + ".png");
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum CardValue {
        ACE("A", "Ace",1,14,1),
        TWO("2", "2",2,2,49),
        THREE("3", "3",3,3,45),
        FOUR("4", "4",4,4,41),
        FIVE("5", "5",5,5,37),
        SIX("6", "6",6,6,33),
        SEVEN("7", "7",7,7,29),
        EIGHT("8", "8",8,8,25),
        NINE("9", "9",9,9,21),
        TEN("10", "10",10,10,17),
        JACK("J", "Jack",11,11,13),
        QUEEN("Q", "Queen",12,12,9),
        KING("K", "King",13,13,5);

        private final String shortValue;
        private final String longValue;
        private final int valueAceLow;
        private final int valueAceHigh;
        private final int imageBase;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum CardSuite {
        HEARTS('H',+2), DIAMONDS('D',+3), CLUBS('C',0), SPADES('S',+1);

        private final char value;
        private final int imageDelta;

    }
}
