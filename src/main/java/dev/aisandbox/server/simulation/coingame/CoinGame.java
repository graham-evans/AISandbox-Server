/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.coingame;

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

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.widget.RollingPieChartWidget;
import dev.aisandbox.server.engine.widget.TextWidget;
import dev.aisandbox.server.engine.widget.TitleWidget;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameAction;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameState;
import dev.aisandbox.server.simulation.coingame.proto.Signal;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Simulation for the coin game, as explained in the book AlphaGo Simplified.
 */
@Slf4j
public final class CoinGame implements Simulation {


  // UI measurements
  private static final int BAIZE_HEIGHT =
      HD_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN - TITLE_HEIGHT - WIDGET_SPACING; // 1173
  private static final int BAIZE_WIDTH = BAIZE_HEIGHT * 4 / 3; // 880

  private static final int LOG_WIDTH =
      HD_WIDTH - LEFT_MARGIN - RIGHT_MARGIN - BAIZE_WIDTH - WIDGET_SPACING;
  private static final int LOG_HEIGHT = (BAIZE_HEIGHT - WIDGET_SPACING) / 2;

  private final int COIN_ROW_INDENT;
  private final int COIN_COLUMN_INDENT =
      (BAIZE_HEIGHT - CoinIcons.COINS_HEIGHT - CoinIcons.ROW_HEIGHT) / 2;

  private final List<Agent> agents;
  private final CoinScenario scenario;
  private final Theme theme;
  private final TitleWidget titleWidget;
  private final TextWidget logWidget;
  private final RollingPieChartWidget pieChartWidget;
  private final String session = UUID.randomUUID().toString();
  private final int maxPic = 2;
  private final List<Double> agentScores = new ArrayList<>();
  private int[] coins;
  private int currentPlayer = 0;
  private BufferedImage[] rowImages;
  private BufferedImage[] coinImages;
  private String episode;

  /**
   * Simulation constructor.
   *
   * @param agents   List of {@link dev.aisandbox.server.engine.Agent} to run the simulation with.
   * @param scenario The specific {@link CoinScenario} to run - defines the number of piles.
   * @param theme    The {@link dev.aisandbox.server.engine.Theme} to use while drawing.
   */
  public CoinGame(final List<Agent> agents, final CoinScenario scenario, final Theme theme) {
    this.agents = agents;
    this.scenario = scenario;
    this.theme = theme;
    // build the coin pile
    coins = new int[scenario.getRows().length];
    // set up the widgets
    titleWidget = TitleWidget.builder().title("The Coin Game").theme(theme).build();
    logWidget = TextWidget.builder().width(LOG_WIDTH).height(LOG_HEIGHT).font(LOG_FONT).theme(theme)
        .build();
    pieChartWidget = RollingPieChartWidget.builder().width(LOG_WIDTH).height(LOG_HEIGHT)
        .title("Winner of the last 200 episodes.").theme(theme).build();
    COIN_ROW_INDENT = (BAIZE_WIDTH - 290 * scenario.getRows().length) / 2;
    // generate the images
    try {
      rowImages = CoinIcons.getRowImages(scenario.getRows().length, theme);
      coinImages = CoinIcons.getCoinImages(Arrays.stream(scenario.getRows()).max().getAsInt(),
          theme);
    } catch (IOException e) {
      log.error("Error loading images", e);
    }
    // set the scores to 0
    agents.forEach(a -> agentScores.add(0.0));
    // reset the game to the initial stage
    reset();
  }

  private void reset() {
    // reset the number of coins in each pile
    System.arraycopy(scenario.getRows(), 0, coins, 0, scenario.getRows().length);
    // change the episode ID
    episode = UUID.randomUUID().toString();
  }

  @Override
  public void step(OutputRenderer output) {
    // draw the current state
    output.display();
    log.debug("ask client {} to move", currentPlayer);
    CoinGameState currentState = generateCurrentState(Signal.PLAY);
    CoinGameAction action = agents.get(currentPlayer).receive(currentState, CoinGameAction.class);
    // try and make the move
    try {
      coins = makeMove(action.getSelectedRow(), action.getRemoveCount());
      logWidget.addText(
          agents.get(currentPlayer).getAgentName() + " takes " + action.getRemoveCount()
              + " from row " + action.getSelectedRow() + " leaving "
              + coins[action.getSelectedRow()] + ".");
      if (isGameFinished()) {
        // current player lost
        logWidget.addText(agents.get(currentPlayer).getAgentName() + " lost");
        informResult((currentPlayer + 1) % 2);
        output.display();
        reset();
      }
    } catch (IllegalCoinAction e) {
      log.error(e.getMessage());
      logWidget.addText(agents.get(currentPlayer).getAgentName() + " makes an illegal move.");
      // player has tried an illegal move - end the game
      informResult((currentPlayer + 1) % 2);
      output.display();
      reset();
    }
    currentPlayer++;
    if (currentPlayer >= agents.size()) {
      currentPlayer = 0;
    }
  }

  /**
   * Creates a protocol buffer message representing the current state of the game.
   * <p>
   * This state contains all information needed by an agent to make a decision, including the
   * current coin counts, maximum allowed move, and game signals.
   *
   * @param signal The game signal to send (PLAY, WIN, LOSE)
   * @return A new CoinGameState protobuf message
   */
  private CoinGameState generateCurrentState(Signal signal) {
    // Create and return a protocol buffer message containing the game state
    return CoinGameState.newBuilder()
        // Convert int array of coin counts to a List<Integer>
        .addAllCoinCount(Arrays.stream(coins).boxed().toList())
        // Set the number of rows
        .setRowCount(coins.length)
        // Set the maximum number of coins a player can remove in one turn
        .setMaxPick(maxPic)
        // Set the signal type (PLAY, WIN, LOSE)
        .setSignal(signal)
        // Include session ID for tracking the entire simulation
        .setSessionID(session)
        // Include episode ID for tracking the current game
        .setEpisodeID(episode).build();
  }

  /**
   * Attempts to make a move in the game by removing coins from a selected row.
   * <p>
   * The method checks if the move is legal according to the game rules: - The number of coins to
   * remove must be between 1 and the maximum allowed. - The selected row must be valid and contain
   * enough coins. - The move must not exceed the maximum number of coins that can be taken in one
   * turn.
   *
   * @param row    The row from which coins will be removed
   * @param amount The number of coins to remove
   * @return The updated coin distribution after the move
   * @throws IllegalCoinAction if the move is illegal
   */
  private int[] makeMove(int row, int amount) throws IllegalCoinAction {
    // is the amount out of the allowed range
    if (amount < 1 || amount > scenario.getMax()) {
      throw new IllegalCoinAction("Must remove between 1 and " + scenario.getMax() + " coins.");
    }
    // is the row out of the allowed indexing range
    if (row < 0 || row >= coins.length) {
      throw new IllegalCoinAction("Must select row from 0 to " + (coins.length - 1) + ".");
    }
    // does the selected row have enough coins
    if (coins[row] < amount) {
      throw new IllegalCoinAction("Not enough coins in the selected row.");
    }
    // is the agent taking too many coins
    if (amount > maxPic) {
      throw new IllegalCoinAction("Taking more than the maximum amount of coins.");
    }
    // make the move
    int[] newCoins = Arrays.copyOf(coins, coins.length);
    newCoins[row] -= amount;
    return newCoins;
  }

  /**
   * Checks if the game has reached a terminal state.
   * <p>
   * The game is finished when all rows have zero coins left. According to the rules, the player who
   * takes the last coin loses.
   *
   * @return true if all piles are empty, false otherwise
   */
  private boolean isGameFinished() {
    // Check each row for remaining coins
    for (int row = 0; row < scenario.getRows().length; row++) {
      // If any row has coins, game is not finished
      if (coins[row] > 0) {
        return false;
      }
    }
    // All rows are empty, game is finished
    return true;
  }

  private void informResult(int winner) {
    Agent winAgent = agents.get(winner);
    Agent loseAgent = agents.get((winner + 1) % 2);
    winAgent.send(generateCurrentState(Signal.WIN));
    loseAgent.send(generateCurrentState(Signal.LOSE));
    logWidget.addText(agents.get(winner).getAgentName() + " wins");
    agentScores.set(winner, agentScores.get(winner) + 1);
    pieChartWidget.addValue(agents.get(winner).getAgentName(), theme.getAgentMain(winner));
  }

  /**
   * Renders the visual representation of the game.
   * <p>
   * This method is called by the output renderer to draw the game state, including the coin piles,
   * UI widgets, and game information.
   *
   * @param graphics2D The graphics context to draw on
   */
  @Override
  public void visualise(Graphics2D graphics2D) {
    // Fill background with theme color
    graphics2D.setColor(theme.getBackground());
    graphics2D.fillRect(0, 0, HD_WIDTH, HD_HEIGHT);

    // Draw UI widgets in their respective positions
    graphics2D.drawImage(titleWidget.getImage(), 0, TOP_MARGIN, null);
    graphics2D.drawImage(pieChartWidget.getImage(), HD_WIDTH - RIGHT_MARGIN - LOG_WIDTH,
        TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, null);
    graphics2D.drawImage(logWidget.getImage(), HD_WIDTH - RIGHT_MARGIN - LOG_WIDTH,
        TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + LOG_HEIGHT + WIDGET_SPACING, null);

    // Draw the baize (green felt) background for the game area
    graphics2D.setColor(theme.getBaize());
    graphics2D.fillRect(LEFT_MARGIN, TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, BAIZE_WIDTH,
        BAIZE_HEIGHT);

    // Set color for text elements
    graphics2D.setColor(theme.getText());

    // Draw each row number and its corresponding coin pile
    for (int i = 0; i < coins.length; i++) {
      // Draw row number image
      graphics2D.drawImage(rowImages[i], LEFT_MARGIN + i * CoinIcons.PILE_WIDTH + COIN_ROW_INDENT,
          TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + COIN_COLUMN_INDENT, null);

      // Draw coin pile image based on the number of coins in this row
      graphics2D.drawImage(coinImages[coins[i]],
          LEFT_MARGIN + i * CoinIcons.PILE_WIDTH + COIN_ROW_INDENT,
          TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + CoinIcons.ROW_HEIGHT + COIN_COLUMN_INDENT,
          null);
    }

    // Draw the AI Sandbox logo in the top right corner
    graphics2D.drawImage(LOGO, HD_WIDTH - LOGO_WIDTH - RIGHT_MARGIN,
        (TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING - LOGO_HEIGHT) / 2, null);

  }
}
