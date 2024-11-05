package dev.aisandbox.server.simulation.highlowcards;

import dev.aisandbox.server.engine.chart.RollingScoreChart;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.simulation.common.Card;
import dev.aisandbox.server.simulation.common.Deck;
import dev.aisandbox.server.simulation.highlowcards.proto.ClientAction;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowChoice;
import dev.aisandbox.server.simulation.highlowcards.proto.ServerState;
import dev.aisandbox.server.simulation.highlowcards.proto.Signal;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.*;

@Slf4j
public class HighLowCards implements Simulation {

    private final Player player;
    private final int cardCount;

    private final Random rand = new Random();

    private final List<Card> faceUpCards = new ArrayList<>();
    private final List<Card> faceDownCards = new ArrayList<>();
    private final Map<String, BufferedImage> cardImages = new HashMap<>();
    private final RollingScoreChart rollingScoreChart = new RollingScoreChart(200,300,250,true);


    public HighLowCards(Player player, int cardCount) {
        this.player = player;
        this.cardCount = cardCount;
        reset();
    }

    private void reset() {
        // create a deck of cards
        Deck deck = new Deck();
        // shuffle
        deck.shuffle(rand);
        // clear any old cards
        faceDownCards.clear();
        faceUpCards.clear();
        // deal cards
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
                rollingScoreChart.addScore(faceUpCards.size());
                player.send(getPlayState(Signal.RESET, faceUpCards.size()));
                reset();
            }
        } else {
            // incorrect guess - game over
            rollingScoreChart.addScore(faceUpCards.size()-1);
            player.send(getPlayState(Signal.RESET, faceUpCards.size()-1));
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
        for (int dx = 0; dx < faceUpCards.size(); dx++) {
            Card card = faceUpCards.get(dx);
            BufferedImage cardImage = getCardImage(card.getImageName());
            graphics2D.drawImage(cardImage, dx * 50 + 100, 100, null);
        }
        for (int dx = 0;dx<faceDownCards.size(); dx++) {
            BufferedImage cardImage = getCardImage("/images/cards/back.png");
            graphics2D.drawImage(cardImage, (dx+faceUpCards.size()+2) * 50 + 100, 100, null);
        }
        graphics2D.drawImage(rollingScoreChart.getImage(), 100, 500, null);
    }

    private BufferedImage getCardImage(String path) {
        return cardImages.computeIfAbsent(path, s -> {
            try {
                log.info("Loading image {}", path);
                return ImageIO.read(HighLowCards.class.getResourceAsStream(path));
            } catch (IOException e) {
                log.error("Error loading card image {}", path, e);
                return null;
            }
        });
    }


}
