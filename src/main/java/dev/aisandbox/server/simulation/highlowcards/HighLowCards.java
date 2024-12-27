package dev.aisandbox.server.simulation.highlowcards;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.chart.RollingHistogramChart;
import dev.aisandbox.server.engine.chart.RollingScoreChart;
import dev.aisandbox.server.engine.chart.TextChart;
import dev.aisandbox.server.engine.output.OutputConstants;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.simulation.common.Card;
import dev.aisandbox.server.simulation.common.Deck;
import dev.aisandbox.server.simulation.highlowcards.proto.ClientAction;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowChoice;
import dev.aisandbox.server.simulation.highlowcards.proto.PlayState;
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

    private static final int MARGIN = 80;
    private static final int GRAPH_WIDTH = (OutputConstants.HD_WIDTH - 3 * MARGIN) / 2;
    private static final int GRAPH_HEIGHT = 300;
    private static final int TEXT_HEIGHT = 200;
    private final Player player;
    private final int cardCount;
    private final Random rand = new Random();
    private final List<Card> faceUpCards = new ArrayList<>();
    private final List<Card> faceDownCards = new ArrayList<>();
    private final Map<String, BufferedImage> cardImages = new HashMap<>();
    private final RollingScoreChart rollingScoreChart;
    private final RollingHistogramChart rollingHistogramChart;
    private final TextChart textChart;

    public HighLowCards(Player player, int cardCount) {
        this.player = player;
        this.cardCount = cardCount;
        rollingScoreChart = RollingScoreChart.builder()
                .dataWindow(200)
                .width(GRAPH_WIDTH)
                .height(GRAPH_HEIGHT)
                .cache(true)
                .build();
        rollingHistogramChart = RollingHistogramChart.builder()
                .dataWindow(200)
                .width(GRAPH_WIDTH)
                .height(GRAPH_HEIGHT)
                .cache(true)
                .build();
        textChart = TextChart.builder().width(OutputConstants.HD_WIDTH - 2 * MARGIN).height(TEXT_HEIGHT).build();
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

    private PlayState getPlayState(Signal signal, int score) {
        return PlayState.newBuilder()
                .setCardCount(cardCount)
                .addAllDeltCard(faceUpCards.stream().map(Card::getShortDrescription).toList())
                .setScore(score)
                .setSignal(signal)
                .build();
    }

    @Override
    public void step(OutputRenderer output) {
        // get the previous and next cards
        Card previousCard = faceUpCards.getLast();
        Card nextCard = faceDownCards.getFirst();
        // render the current frame
        textChart.addText("Showing " + faceUpCards.getLast().getShortDrescription());
        output.display();
        // send the current state and request an action
        ClientAction action = player.recieve(getPlayState(Signal.PLAY, faceDownCards.size()), ClientAction.class);
        log.debug("Client action: {}", action.getAction().name());
        // turn over the next card
        faceUpCards.add(faceDownCards.removeFirst());
        // did the player guess correctly
        if (
                (action.getAction() == HighLowChoice.LOW && nextCard.cardValue().getValueAceHigh() < previousCard.cardValue().getValueAceHigh())
                        ||
                        (action.getAction() == HighLowChoice.HIGH && nextCard.cardValue().getValueAceHigh() > previousCard.cardValue().getValueAceHigh())) {
            // correct guess
            textChart.addText("[" + player.getPlayerName() + "] " + action.getAction().name() + " - correct");
            output.display();
            // reset if we're finished.
            if (faceDownCards.isEmpty()) {
                rollingScoreChart.addScore(faceUpCards.size());
                rollingHistogramChart.addScore(faceUpCards.size());
                player.send(getPlayState(Signal.RESET, faceUpCards.size()));
                reset();
            }
        } else {
            // incorrect guess - game over
            textChart.addText("[" + player.getPlayerName() + "] " + action.getAction().name() + " - wrong");
            rollingScoreChart.addScore(faceUpCards.size() - 1);
            rollingHistogramChart.addScore(faceUpCards.size() - 1);
            player.send(getPlayState(Signal.RESET, faceUpCards.size() - 1));
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
        graphics2D.setColor(Color.WHITE);
        graphics2D.fillRect(0, 0, OutputConstants.HD_WIDTH, OutputConstants.HD_HEIGHT);
        for (int dx = 0; dx < faceUpCards.size(); dx++) {
            Card card = faceUpCards.get(dx);
            BufferedImage cardImage = getCardImage(card.getImageName());
            graphics2D.drawImage(cardImage, dx * 50 + MARGIN, MARGIN, null);
        }
        for (int dx = 0; dx < faceDownCards.size(); dx++) {
            BufferedImage cardImage = getCardImage("/images/cards/back.png");
            graphics2D.drawImage(cardImage, (dx + faceUpCards.size() + 2) * 50 + MARGIN, MARGIN, null);
        }
        graphics2D.drawImage(rollingScoreChart.getImage(), MARGIN, OutputConstants.HD_HEIGHT - MARGIN * 2 - TEXT_HEIGHT - GRAPH_HEIGHT, null);
        graphics2D.drawImage(rollingHistogramChart.getImage(), 2 * MARGIN + GRAPH_WIDTH, OutputConstants.HD_HEIGHT - MARGIN * 2 - TEXT_HEIGHT - GRAPH_HEIGHT, null);
        graphics2D.drawImage(textChart.getImage(), MARGIN, OutputConstants.HD_HEIGHT - MARGIN - TEXT_HEIGHT, null);
    }

    private BufferedImage getCardImage(String path) {
        return cardImages.computeIfAbsent(path, s -> {
            try {
                log.debug("Loading image {}", path);
                return ImageIO.read(HighLowCards.class.getResourceAsStream(path));
            } catch (IOException e) {
                log.error("Error loading card image {}", path, e);
                return null;
            }
        });
    }


}
