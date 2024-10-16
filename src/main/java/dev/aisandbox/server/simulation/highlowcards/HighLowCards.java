package dev.aisandbox.server.simulation.highlowcards;

import dev.aisandbox.server.engine.OutputRenderer;
import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.simulation.common.Card;
import dev.aisandbox.server.simulation.common.Deck;
import dev.aisandbox.server.simulation.highlowcards.proto.ClientAction;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowChoice;
import dev.aisandbox.server.simulation.highlowcards.proto.ServerState;
import dev.aisandbox.server.simulation.highlowcards.proto.Signal;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class HighLowCards implements Simulation {

    private final Player player;
    private final int cardCount;

    private final Random rand = new Random();

    private final List<Card> faceUpCards = new ArrayList<>();
    private final List<Card> faceDownCards = new ArrayList<>();


    public HighLowCards(Player player, int cardCount) {
        this.player = player;
        this.cardCount = cardCount;
        reset();
    }

    private void reset() {
        Deck deck = new Deck();
        faceDownCards.clear();
        faceUpCards.clear();
        for (int i = 0; i < cardCount; i++) {
            faceDownCards.add(deck.getCard());
        }
        // turn over the first card
        faceUpCards.add(faceDownCards.removeFirst());
    }

    private ServerState getPlayState(Signal signal, int score) {
        return ServerState.newBuilder()
                .setCardCount(cardCount)
                .addAllDeltCard(faceUpCards.stream().map(Card::getShortDrescription).toList())
                .setScore(score)
                .setSignal(signal)
                .build();
    }

    @Override
    public void step(OutputRenderer output) {
        // send the current state and request an action
        ClientAction action = (ClientAction) player.receive(getPlayState(Signal.PLAY, faceDownCards.size()));
        log.info("Client action: {}", action.getAction().name());
        // get the previous and next cards
        Card previousCard = faceUpCards.getLast();
        Card nextCard = faceDownCards.getFirst();
        // turn over the next card
        faceUpCards.add(faceDownCards.removeFirst());
        // did the player guess correctly
        if (
                (action.getAction() == HighLowChoice.LOW && nextCard.cardValue().getValueAceHigh() < previousCard.cardValue().getValueAceHigh())
                        ||
                        (action.getAction() == HighLowChoice.HIGH && nextCard.cardValue().getValueAceHigh() > previousCard.cardValue().getValueAceHigh())) {
            // correct guess
            output.display();
            // reset if we're finished.
            if (faceDownCards.isEmpty()) {
                player.send(getPlayState(Signal.RESET, faceUpCards.size()));
                reset();
            }
        } else {
            // incorrect guess - game over
            player.send(getPlayState(Signal.RESET, 0));
            output.display();
            // reset
            reset();
        }

    }

    @Override
    public void close() {

    }

    @Override
    public void visualise(Graphics2D graphics2D) {

    }
}
