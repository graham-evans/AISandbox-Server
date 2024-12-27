package dev.aisandbox.server.simulation.highlowcards;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputConstants;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.widget.RollingValueHistogramWidget;
import dev.aisandbox.server.engine.widget.RollingStatisticsWidget;
import dev.aisandbox.server.engine.widget.RollingValueChartWidget;
import dev.aisandbox.server.engine.widget.TextWidget;
import dev.aisandbox.server.simulation.common.Card;
import dev.aisandbox.server.simulation.common.Deck;
import dev.aisandbox.server.simulation.highlowcards.proto.*;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static dev.aisandbox.server.engine.output.OutputConstants.*;

@Slf4j
public class HighLowCards implements Simulation {

    // UI Elements and constants
    private static final int MARGIN = 80;
    private static final int TEXT_HEIGHT = 280;
    private static final int TEXT_WIDTH = HD_WIDTH - 3 * MARGIN - 920;
    private static final int GRAPH_WIDTH = 920;
    private static final int GRAPH_HEIGHT = (HD_HEIGHT - TEXT_HEIGHT - MARGIN * 4) / 2;
    private final Map<String, BufferedImage> cardImages = new HashMap<>();
    private final Theme theme;
    // simulation elements
    private final Player player;
    private final int cardCount;
    private final Random rand = new Random();
    private final List<Card> faceUpCards = new ArrayList<>();
    private final List<Card> faceDownCards = new ArrayList<>();
    private final String sessionID = UUID.randomUUID().toString();
    // statistics and reporting elements
    private final RollingValueChartWidget scoreWidget;
    private final RollingValueHistogramWidget scoreHistogramWidget;
    private final TextWidget textWidget;
    private final RollingStatisticsWidget statisticsWidget;
    private BufferedImage logo;
    private String episodeID;
    private int score = 0;

    public HighLowCards(Player player, int cardCount, Theme theme) {
        this.player = player;
        this.cardCount = cardCount;
        this.theme = theme;
        scoreWidget = RollingValueChartWidget.builder()
                .width(GRAPH_WIDTH)
                .height(GRAPH_HEIGHT)
                .window(200)
                .theme(theme)
                .title("Final Score")
                .build();
        scoreHistogramWidget = RollingValueHistogramWidget.builder()
                .width(GRAPH_WIDTH)
                .height(GRAPH_HEIGHT)
                .window(200)
                .build();
        textWidget = TextWidget.builder()
                .width(TEXT_WIDTH)
                .height(TEXT_HEIGHT)
                .fontHeight(24)
                .fontName("Ariel")
                .theme(theme)
                .build();
        statisticsWidget = RollingStatisticsWidget.builder()
                .width(TEXT_WIDTH)
                .height(GRAPH_HEIGHT * 2 - 107)
                .fontHeight(32)
                .fontName("Ariel")
                .theme(theme)
                .build();
        try {
            logo = ImageIO.read(HighLowCards.class.getResourceAsStream("/images/AILogo.png"));
        } catch (Exception e) {
            log.error("Error loading logo", e);
            logo = new BufferedImage(LOGO_WIDTH, LOGO_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        }
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
        // create a new episode
        episodeID = UUID.randomUUID().toString();
        // reset the score
        score = 0;
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
        HighLowCardsAction action = player.recieve(
                HighLowCardsState.newBuilder()
                        .setCardCount(cardCount)
                        .addAllDealtCard(faceUpCards.stream().map(Card::getShortDrescription).toList())
                        .setScore(score)
                        .setSessionID(sessionID)
                        .setEpisodeID(episodeID)
                        .build(),
                HighLowCardsAction.class);
        log.debug("Client action: {}", action.getAction().name());
        // turn over the next card
        faceUpCards.add(faceDownCards.removeFirst());
        // did the player guess correctly
        boolean correctGuess =
                (action.getAction() == HighLowChoice.LOW && nextCard.cardValue().getValueAceHigh() < previousCard.cardValue().getValueAceHigh())
                        ||
                        (action.getAction() == HighLowChoice.HIGH && nextCard.cardValue().getValueAceHigh() > previousCard.cardValue().getValueAceHigh());
        // adjust score and show result
        if (correctGuess) {
            score++;
            textWidget.addText("[" + player.getPlayerName() + "] " + action.getAction().name() + " - correct");
        } else {
            textWidget.addText("[" + player.getPlayerName() + "] " + action.getAction().name() + " - wrong");
        }
        output.display();
        // is the episode finished?
        if (!correctGuess || faceDownCards.isEmpty()) {
            // episode ends
            scoreWidget.addValue(score);
            statisticsWidget.addScore(score);
            scoreHistogramWidget.addValue(score);
            player.send(HighLowCardsReward.newBuilder().setScore(score).setSignal(Signal.RESET).build());
            reset();
        } else {
            // play continues
            player.send(HighLowCardsReward.newBuilder().setScore(score).setSignal(Signal.CONTINUE).build());
        }
    }

    @Override
    public void visualise(Graphics2D graphics2D) {
        graphics2D.setColor(theme.getBackground());
        graphics2D.fillRect(0, 0, HD_WIDTH, HD_HEIGHT);
        for (int dx = 0; dx < faceUpCards.size(); dx++) {
            Card card = faceUpCards.get(dx);
            BufferedImage cardImage = getCardImage(card.getImageName());
            graphics2D.drawImage(cardImage, dx * 60 + MARGIN, MARGIN, null);
        }
        for (int dx = faceDownCards.size() - 1; dx >= 0; dx--) {
            BufferedImage cardImage = getCardImage("/images/cards/back.png");
            graphics2D.drawImage(cardImage, (dx + faceUpCards.size() + 4) * 60 + MARGIN, MARGIN, null);
        }
        graphics2D.drawImage(scoreWidget.getImage(), MARGIN, HD_HEIGHT - 2 * MARGIN - GRAPH_HEIGHT * 2, null);
        graphics2D.drawImage(scoreHistogramWidget.getImage(), MARGIN, HD_HEIGHT - MARGIN - GRAPH_HEIGHT, null);
        graphics2D.drawImage(textWidget.getImage(), MARGIN * 2 + 720 + 200, MARGIN, null);
        graphics2D.drawImage(statisticsWidget.getImage(), MARGIN * 2 + 720 + 200, HD_HEIGHT - 2 * MARGIN - GRAPH_HEIGHT * 2, null);
        graphics2D.drawImage(logo, OutputConstants.HD_WIDTH - LOGO_WIDTH - MARGIN, HD_HEIGHT - LOGO_HEIGHT - MARGIN, null);
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
