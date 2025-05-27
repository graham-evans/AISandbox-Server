/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.highlowcards;

import static dev.aisandbox.server.engine.output.OutputConstants.BOTTOM_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.HD_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.HD_WIDTH;
import static dev.aisandbox.server.engine.output.OutputConstants.LEFT_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO_WIDTH;
import static dev.aisandbox.server.engine.output.OutputConstants.LOG_FONT;
import static dev.aisandbox.server.engine.output.OutputConstants.RIGHT_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.TITLE_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.TOP_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.WIDGET_SPACING;
import static dev.aisandbox.server.simulation.common.Card.CARD_HEIGHT;
import static dev.aisandbox.server.simulation.common.Card.CARD_WIDTH;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.exception.SimulationException;
import dev.aisandbox.server.engine.maths.bins.IntegerBinner;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.widget.RollingStatisticsWidget;
import dev.aisandbox.server.engine.widget.RollingValueChartWidget;
import dev.aisandbox.server.engine.widget.RollingValueHistogramWidget;
import dev.aisandbox.server.engine.widget.TextWidget;
import dev.aisandbox.server.engine.widget.TitleWidget;
import dev.aisandbox.server.simulation.common.Card;
import dev.aisandbox.server.simulation.common.Deck;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardsAction;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardsReward;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardsState;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowChoice;
import dev.aisandbox.server.simulation.highlowcards.proto.Signal;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the High-Low Cards game simulation.
 * <p>
 * This simulation presents a sequence of playing cards to the agent, who must predict whether the
 * next card will be higher or lower than the current one. The game continues until either the agent
 * makes an incorrect prediction or all cards have been revealed.
 * <p>
 * The simulation tracks and displays the agent's score, statistics, and game state using various
 * graphical widgets.
 */
@Slf4j
public final class HighLowCards implements Simulation {

  // UI Elements and constants
  /**
   * Width of the baize (green felt area) where cards are displayed
   */
  private static final int BAIZE_WIDTH =
      HD_WIDTH - LEFT_MARGIN - RIGHT_MARGIN - WIDGET_SPACING - 400;

  /**
   * Height of the baize area
   */
  private static final int BAIZE_HEIGHT =
      (HD_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN - TITLE_HEIGHT - WIDGET_SPACING * 2) * 5 / 8;

  /**
   * Padding inside the baize area
   */
  private static final int BAIZE_PADDING = 30;

  // card layout
  /**
   * Horizontal gap between face up and face down cards
   */
  private static final int CARD_GAP = 30;

  // statistics widget
  /**
   * Width of the statistics panel
   */
  private static final int STATISTICS_WIDTH =
      HD_WIDTH - LEFT_MARGIN - RIGHT_MARGIN - WIDGET_SPACING - BAIZE_WIDTH;

  /**
   * Height of the statistics panel
   */
  private static final int STATISTICS_HEIGHT = BAIZE_HEIGHT;

  // results widgets
  /**
   * Width of each results widget
   */
  private static final int RESULTS_WIDTH =
      (HD_WIDTH - LEFT_MARGIN - RIGHT_MARGIN - WIDGET_SPACING * 2) / 3;

  /**
   * Height of each results widget
   */
  private static final int RESULTS_HEIGHT =
      HD_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN - TITLE_HEIGHT - WIDGET_SPACING * 2 - BAIZE_HEIGHT;

  /**
   * Horizontal spacing between cards - calculated based on card count
   */
  private final int CARD_SPACE;

  /**
   * Cache for card images to avoid reloading
   */
  private final Map<String, BufferedImage> cardImages = new HashMap<>();

  /**
   * Visual theme for the simulation
   */
  private final Theme theme;

  // simulation elements
  /**
   * The agent playing the game
   */
  private final Agent agent;

  /**
   * Number of cards to be used in each episode
   */
  private final int cardCount;

  /**
   * Random number generator for shuffling
   */
  private final Random random;

  /**
   * List of cards that have been revealed (face-up)
   */
  private final List<Card> faceUpCards = new ArrayList<>();

  /**
   * List of cards that are still hidden (face-down)
   */
  private final List<Card> faceDownCards = new ArrayList<>();

  /**
   * Unique ID for the entire session
   */
  private final String sessionID = UUID.randomUUID().toString();

  // statistics and reporting elements
  /**
   * Title widget at the top of the display
   */
  private final TitleWidget titleWidget;

  /**
   * Widget showing score trends over time
   */
  private final RollingValueChartWidget scoreWidget;

  /**
   * Widget showing distribution of scores
   */
  private final RollingValueHistogramWidget scoreHistogramWidget;

  /**
   * Text log widget to display game events
   */
  private final TextWidget logWidget;

  /**
   * Widget showing aggregate statistics
   */
  private final RollingStatisticsWidget statisticsWidget;

  /**
   * Unique ID for the current episode
   */
  private String episodeID;

  /**
   * Current score in the episode
   */
  private int score = 0;

  /**
   * Constructs a new High-Low Cards simulation.
   *
   * @param agent     The agent that will play the game
   * @param cardCount The number of cards to use in each episode
   * @param theme     The visual theme to apply to the simulation
   * @param random    Random number generator for shuffling cards
   */
  public HighLowCards(Agent agent, int cardCount, Theme theme, Random random) {
    this.agent = agent;
    this.cardCount = cardCount;
    this.theme = theme;
    this.random = random;

    // setup widgets
    titleWidget = TitleWidget.builder().title("High / Low Cards").theme(theme).build();
    scoreWidget = RollingValueChartWidget.builder().width(RESULTS_WIDTH).height(RESULTS_HEIGHT)
        .window(200).theme(theme).title("Final Score").build();
    scoreHistogramWidget = RollingValueHistogramWidget.builder().width(RESULTS_WIDTH)
        .height(RESULTS_HEIGHT).window(200).binEngine(new IntegerBinner(0, cardCount)).theme(theme)
        .title("Score Distribution").build();
    logWidget = TextWidget.builder().width(RESULTS_WIDTH).height(RESULTS_HEIGHT).font(LOG_FONT)
        .theme(theme).build();
    statisticsWidget = RollingStatisticsWidget.builder().width(STATISTICS_WIDTH)
        .height(STATISTICS_HEIGHT).theme(theme)
        //  .opaque(false)
        .build();

    // Calculate spacing between cards based on available width and card count
    CARD_SPACE = (BAIZE_WIDTH - BAIZE_PADDING * 2 - CARD_WIDTH * 2 - CARD_GAP) / (cardCount - 2);

    // Initialize the game state
    reset();
  }

  /**
   * Resets the simulation to start a new episode.
   * <p>
   * Creates a new deck, shuffles it, deals cards, and resets score.
   */
  private void reset() {
    // create a deck of cards
    Deck deck = new Deck();
    // shuffle
    deck.shuffle(random);
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

  /**
   * Advances the simulation by one step.
   * <p>
   * Gets the agent's prediction (high/low), reveals the next card, evaluates if the prediction was
   * correct, and updates the game state.
   *
   * @param output The renderer for displaying the simulation state
   */
  @Override
  public void step(OutputRenderer output) throws SimulationException {
    // get the previous and next cards
    Card previousCard = faceUpCards.getLast();
    Card nextCard = faceDownCards.getFirst();

    // render the current frame
    logWidget.addText("Showing " + faceUpCards.stream().map(Card::getShortDrescription)
        .collect(Collectors.joining(",")));
    output.display();

    // send the current state and request an action from the agent
    agent.send(HighLowCardsState.newBuilder().setCardCount(cardCount)
        .addAllDealtCard(faceUpCards.stream().map(Card::getShortDrescription).toList())
        .setScore(score).setSessionID(sessionID).setEpisodeID(episodeID).build());
    HighLowCardsAction action = agent.receive(HighLowCardsAction.class);
    log.debug("Client action: {}", action.getAction().name());

    // turn over the next card
    faceUpCards.add(faceDownCards.removeFirst());

    // determine if the player guessed correctly
    boolean correctGuess = (action.getAction() == HighLowChoice.LOW
        && nextCard.cardValue().getValueAceHigh() < previousCard.cardValue().getValueAceHigh()) || (
        action.getAction() == HighLowChoice.HIGH
            && nextCard.cardValue().getValueAceHigh() > previousCard.cardValue().getValueAceHigh());

    // adjust score and show result
    if (correctGuess) {
      score++;
      logWidget.addText(
          "[" + agent.getAgentName() + "] " + action.getAction().name() + " - correct ("
              + faceUpCards.getLast().getShortDrescription() + ")");
    } else {
      logWidget.addText("[" + agent.getAgentName() + "] " + action.getAction().name() + " - wrong ("
          + faceUpCards.getLast().getShortDrescription() + ")");
    }
    output.display();

    // check if the episode is finished
    if (!correctGuess || faceDownCards.isEmpty()) {
      // episode ends - update statistics and send signal to agent
      scoreWidget.addValue(score);
      statisticsWidget.addScore(score);
      scoreHistogramWidget.addValue(score);
      agent.send(HighLowCardsReward.newBuilder().setScore(score).setSignal(Signal.RESET).build());
      reset();
    } else {
      // play continues - send signal to agent
      agent.send(
          HighLowCardsReward.newBuilder().setScore(score).setSignal(Signal.CONTINUE).build());
    }
  }

  /**
   * Renders the visual state of the simulation.
   * <p>
   * Draws the background, cards, and all UI widgets including statistics and logs.
   *
   * @param graphics2D The graphics context to render to
   */
  @Override
  public void visualise(Graphics2D graphics2D) {
    // Draw background
    graphics2D.setColor(theme.getBackground());
    graphics2D.fillRect(0, 0, HD_WIDTH, HD_HEIGHT);

    // Draw title
    graphics2D.drawImage(titleWidget.getImage(), 0, TOP_MARGIN, null);

    // Draw the baize (green felt surface)
    graphics2D.setColor(theme.getBaize());
    graphics2D.fillRect(LEFT_MARGIN, TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, BAIZE_WIDTH,
        BAIZE_HEIGHT);

    // Draw face-up cards
    for (int dx = 0; dx < faceUpCards.size(); dx++) {
      Card card = faceUpCards.get(dx);
      BufferedImage cardImage = getCardImage(card.getImageName());
      graphics2D.drawImage(cardImage, dx * CARD_SPACE + LEFT_MARGIN + BAIZE_PADDING,
          TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + (BAIZE_HEIGHT - CARD_HEIGHT) / 2, null);
    }

    // Draw face-down cards (back of cards)
    for (int dx = faceDownCards.size() - 1; dx >= 0; dx--) {
      BufferedImage cardImage = getCardImage("/images/cards/1B.png");
      graphics2D.drawImage(cardImage,
          LEFT_MARGIN + BAIZE_PADDING + CARD_GAP + (dx + faceUpCards.size() - 1) * CARD_SPACE
              + CARD_WIDTH,
          TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + (BAIZE_HEIGHT - CARD_HEIGHT) / 2, null);
    }

    // Draw statistical widgets
    graphics2D.drawImage(scoreWidget.getImage(), LEFT_MARGIN,
        HD_HEIGHT - BOTTOM_MARGIN - RESULTS_HEIGHT, null);
    graphics2D.drawImage(scoreHistogramWidget.getImage(),
        LEFT_MARGIN + RESULTS_WIDTH + WIDGET_SPACING, HD_HEIGHT - BOTTOM_MARGIN - RESULTS_HEIGHT,
        null);
    graphics2D.drawImage(logWidget.getImage(), LEFT_MARGIN + RESULTS_WIDTH * 2 + WIDGET_SPACING * 2,
        HD_HEIGHT - BOTTOM_MARGIN - RESULTS_HEIGHT, null);
    graphics2D.drawImage(statisticsWidget.getImage(), HD_WIDTH - RIGHT_MARGIN - STATISTICS_WIDTH,
        TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, null);

    // Draw logo
    graphics2D.drawImage(LOGO, HD_WIDTH - LOGO_WIDTH - RIGHT_MARGIN,
        (TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING - LOGO_HEIGHT) / 2, null);
  }

  /**
   * Gets the image for a specified card, loading it on first access and caching for future use.
   *
   * @param path Path to the card image resource
   * @return BufferedImage of the requested card
   */
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
