package dev.aisandbox.server.simulation.highlowcards;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.maths.bins.IntegerBinner;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.widget.*;
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

    // card sizes - from original PNG files
    private static final int CARD_WIDTH = 200;
    private static final int CARD_HEIGHT = 280;
    // the padding around the edge of the cards for the baize
    private static final int BAIZE_PADDING = 20;
    // text box (for the logs)
    private static final int TEXT_HEIGHT = CARD_HEIGHT + BAIZE_PADDING * 2; // height of the text box is the same as the height of the baize
    private static final int TEXT_WIDTH = 700;
    // baize
    private static final int BAIZE_WIDTH = HD_WIDTH - LEFT_MARGIN - RIGHT_MARGIN - TEXT_WIDTH - WIDGET_SPACING;
    private static final int BAIZE_HEIGHT = CARD_HEIGHT + BAIZE_PADDING * 2;
    private static final int GRAPH_HEIGHT = (HD_HEIGHT - TITLE_HEIGHT - BAIZE_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN - WIDGET_SPACING * 3) / 2;
    // card layout
    private static final int CARD_GAP = 50; // the horizontal gap between the left (face up) card and the right (face down) card
    private static final int GRAPH_WIDTH = 920;
    private final int CARD_SPACE; // this is calculated based on the card count
    // statistics widget
    private static final int STATISTICS_WIDTH = HD_WIDTH - LEFT_MARGIN-RIGHT_MARGIN-WIDGET_SPACING-GRAPH_WIDTH;
    private static final int STATISTICS_HEIGHT = HD_HEIGHT - TITLE_HEIGHT - BOTTOM_MARGIN-TOP_MARGIN-WIDGET_SPACING*2-BAIZE_HEIGHT;


    private final Map<String, BufferedImage> cardImages = new HashMap<>();
    private final Theme theme;
    // simulation elements
    private final Agent agent;
    private final int cardCount;
    private final Random rand = new Random();
    private final List<Card> faceUpCards = new ArrayList<>();
    private final List<Card> faceDownCards = new ArrayList<>();
    private final String sessionID = UUID.randomUUID().toString();
    // statistics and reporting elements
    private final TitleWidget titleWidget;
    private final RollingValueChartWidget scoreWidget;
    private final RollingValueHistogramWidget scoreHistogramWidget;
    private final TextWidget textWidget;
    private final RollingStatisticsWidget statisticsWidget;
    private String episodeID;
    private int score = 0;

    public HighLowCards(Agent agent, int cardCount, Theme theme) {
        this.agent = agent;
        this.cardCount = cardCount;
        this.theme = theme;
        // setup widgets
        titleWidget = TitleWidget.builder().title("High/Low Cards").theme(theme).build();
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
                .binEngine(new IntegerBinner(0, cardCount))
                .theme(theme)
                .title("Score Distribution")
                .build();
        textWidget = TextWidget.builder()
                .width(TEXT_WIDTH)
                .height(TEXT_HEIGHT)
                .fontHeight(24)
                .fontName("Ariel")
                .theme(theme)
                .build();
        statisticsWidget = RollingStatisticsWidget.builder()
                .width(STATISTICS_WIDTH)
                .height(STATISTICS_HEIGHT)
                .fontHeight(32)
                .fontName("Ariel")
                .theme(theme)
                .opaque(false)
                .build();
        CARD_SPACE = (BAIZE_WIDTH - BAIZE_PADDING * 2 - CARD_WIDTH * 2 - CARD_GAP) / (cardCount - 2);
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
        HighLowCardsAction action = agent.receive(
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
            textWidget.addText("[" + agent.getAgentName() + "] " + action.getAction().name() + " - correct");
        } else {
            textWidget.addText("[" + agent.getAgentName() + "] " + action.getAction().name() + " - wrong");
        }
        output.display();
        // is the episode finished?
        if (!correctGuess || faceDownCards.isEmpty()) {
            // episode ends
            scoreWidget.addValue(score);
            statisticsWidget.addScore(score);
            scoreHistogramWidget.addValue(score);
            agent.send(HighLowCardsReward.newBuilder().setScore(score).setSignal(Signal.RESET).build());
            reset();
        } else {
            // play continues
            agent.send(HighLowCardsReward.newBuilder().setScore(score).setSignal(Signal.CONTINUE).build());
        }
    }

    @Override
    public void visualise(Graphics2D graphics2D) {
        graphics2D.setColor(theme.getBackground());
        graphics2D.fillRect(0, 0, HD_WIDTH, HD_HEIGHT);
        // draw title
        graphics2D.drawImage(titleWidget.getImage(), 0, TOP_MARGIN, null);
        // draw cards
        graphics2D.setColor(theme.getBaize());
        graphics2D.fillRect(LEFT_MARGIN, TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, BAIZE_WIDTH, BAIZE_HEIGHT);
        for (int dx = 0; dx < faceUpCards.size(); dx++) {
            Card card = faceUpCards.get(dx);
            BufferedImage cardImage = getCardImage(card.getImageName());
            graphics2D.drawImage(cardImage, dx * CARD_SPACE + LEFT_MARGIN + BAIZE_PADDING, TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + BAIZE_PADDING, null);
        }
        for (int dx = faceDownCards.size() - 1; dx >= 0; dx--) {
            BufferedImage cardImage = getCardImage("/images/cards/back.png");
            graphics2D.drawImage(cardImage, LEFT_MARGIN + BAIZE_PADDING + CARD_GAP + (dx + faceUpCards.size() - 1) * CARD_SPACE + CARD_WIDTH, TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + BAIZE_PADDING, null);
        }
        // draw widgets
        graphics2D.drawImage(scoreWidget.getImage(), LEFT_MARGIN, TOP_MARGIN + TITLE_HEIGHT + BAIZE_HEIGHT + WIDGET_SPACING * 2, null);
        graphics2D.drawImage(scoreHistogramWidget.getImage(), LEFT_MARGIN, TOP_MARGIN + TITLE_HEIGHT + BAIZE_HEIGHT + WIDGET_SPACING * 3 + GRAPH_HEIGHT, null);
        graphics2D.drawImage(textWidget.getImage(), HD_WIDTH - RIGHT_MARGIN - TEXT_WIDTH, TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, null);
        graphics2D.drawImage(statisticsWidget.getImage(), LEFT_MARGIN + GRAPH_WIDTH + WIDGET_SPACING, TOP_MARGIN + TITLE_HEIGHT + BAIZE_HEIGHT + WIDGET_SPACING * 2, null);
        graphics2D.drawImage(LOGO, HD_WIDTH - LOGO_WIDTH - RIGHT_MARGIN, HD_HEIGHT - LOGO_HEIGHT - BOTTOM_MARGIN, null);
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
