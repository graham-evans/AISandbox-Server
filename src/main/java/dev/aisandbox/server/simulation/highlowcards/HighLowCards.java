package dev.aisandbox.server.simulation.highlowcards;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputConstants;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.widget.RollingHistogramChart;
import dev.aisandbox.server.engine.widget.RollingScoreChart;
import dev.aisandbox.server.engine.widget.TextWidget;
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
import java.util.stream.Collectors;

@Slf4j
public class HighLowCards implements Simulation {

    private static final int MARGIN = 80;
    private static final int TEXT_HEIGHT = 280;
    private static final int TEXT_WIDTH = OutputConstants.HD_WIDTH - 3 * MARGIN - 920;
    private static final int GRAPH_WIDTH = 920;
    private static final int GRAPH_HEIGHT = (OutputConstants.HD_HEIGHT - TEXT_HEIGHT - MARGIN * 4) / 2;
    private final Player player;
    private final int cardCount;
    private final Random rand = new Random();
    private final List<Card> faceUpCards = new ArrayList<>();
    private final List<Card> faceDownCards = new ArrayList<>();
    private final Map<String, BufferedImage> cardImages = new HashMap<>();
    private final RollingScoreChart rollingScoreChart;
    private final RollingHistogramChart rollingHistogramChart;
    private final TextWidget textWidget;
    private final TextWidget summaryChart;
    private final Theme theme;

    public HighLowCards(Player player, int cardCount, Theme theme) {
        this.player = player;
        this.cardCount = cardCount;
        this.theme = theme;
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
        textWidget = new TextWidget(TEXT_WIDTH, TEXT_HEIGHT, 14, "Arel", theme);
        summaryChart = new TextWidget(TEXT_WIDTH, GRAPH_HEIGHT * 2 + MARGIN, 20, "Ariel", theme);
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
        textWidget.addText("Showing " + faceUpCards.stream().map(Card::getShortDrescription).collect(Collectors.joining(",")));
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
            textWidget.addText("[" + player.getPlayerName() + "] " + action.getAction().name() + " - correct");
            output.display();
            // reset if we're finished.
            if (faceDownCards.isEmpty()) {
                rollingScoreChart.addScore(faceUpCards.size());
                rollingHistogramChart.addScore(faceUpCards.size());
                summaryChart.addText("Median=3");
                player.send(getPlayState(Signal.RESET, faceUpCards.size()));
                reset();
            }
        } else {
            // incorrect guess - game over
            textWidget.addText("[" + player.getPlayerName() + "] " + action.getAction().name() + " - wrong");
            rollingScoreChart.addScore(faceUpCards.size() - 1);
            rollingHistogramChart.addScore(faceUpCards.size() - 1);
            summaryChart.addText("Median=3");
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
        graphics2D.setColor(theme.getBackground());
        graphics2D.fillRect(0, 0, OutputConstants.HD_WIDTH, OutputConstants.HD_HEIGHT);
        for (int dx = 0; dx < faceUpCards.size(); dx++) {
            Card card = faceUpCards.get(dx);
            BufferedImage cardImage = getCardImage(card.getImageName());
            graphics2D.drawImage(cardImage, dx * 60 + MARGIN, MARGIN, null);
        }
        for (int dx = faceDownCards.size() - 1; dx >= 0; dx--) {
            BufferedImage cardImage = getCardImage("/images/cards/back.png");
            graphics2D.drawImage(cardImage, (dx + faceUpCards.size() + 4) * 60 + MARGIN, MARGIN, null);
        }
        graphics2D.drawImage(rollingScoreChart.getImage(), MARGIN, OutputConstants.HD_HEIGHT - 2 * MARGIN - GRAPH_HEIGHT * 2, null);
        graphics2D.drawImage(rollingHistogramChart.getImage(), MARGIN, OutputConstants.HD_HEIGHT - MARGIN - GRAPH_HEIGHT, null);
        graphics2D.drawImage(textWidget.getImage(), MARGIN * 2 + 720 + 200, MARGIN, null);
        graphics2D.drawImage(summaryChart.getImage(), MARGIN * 2 + 720 + 200, OutputConstants.HD_HEIGHT - 2 * MARGIN - GRAPH_HEIGHT * 2, null);
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
