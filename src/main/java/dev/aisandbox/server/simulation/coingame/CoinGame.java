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
import dev.aisandbox.server.engine.exception.SimulationException;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.widget.RollingPieChartWidget;
import dev.aisandbox.server.engine.widget.TextWidget;
import dev.aisandbox.server.engine.widget.TitleWidget;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameAction;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameResult;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameSignal;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameState;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
  // UI elements
  private final Theme theme;
  private final TitleWidget titleWidget;
  private final TextWidget logWidget;
  private final RollingPieChartWidget pieChartWidget;
  // Agent & Game elements
  private final String sessionId = UUID.randomUUID().toString();
  private final Agent[] agents = new Agent[2];
  private final boolean[] agentMoved = new boolean[2];
  private final CoinScenario scenario;
  private int firstPlayer = 1;
  private int currentPlayer = 0;
  // UI Images
  private BufferedImage[] rowImages;
  private BufferedImage[] coinImages;
  private String episodeId;
  private int[] coins;


  /**
   * Simulation constructor.
   *
   * @param agents   List of {@link dev.aisandbox.server.engine.Agent} to run the simulation with.
   * @param scenario The specific {@link CoinScenario} to run - defines the number of piles.
   * @param theme    The {@link dev.aisandbox.server.engine.Theme} to use while drawing.
   */
  public CoinGame(final List<Agent> agents, final CoinScenario scenario, final Theme theme) {
    assert agents.size() == 2;
    this.agents[0] = agents.get(0);
    this.agents[1] = agents.get(1);
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
    // reset the game to the initial stage
    reset();
  }

  private void reset() {
    // reset the number of coins in each pile
    System.arraycopy(scenario.getRows(), 0, coins, 0, scenario.getRows().length);
    // change the episode ID
    episodeId = UUID.randomUUID().toString();
    // mark that neither player has moved;
    agentMoved[0] = false;
    agentMoved[1] = false;
    // choose a different starting player from last time
    firstPlayer = (firstPlayer + 1) % 2;
    currentPlayer = firstPlayer;
  }

  @Override
  public void step(OutputRenderer output) throws SimulationException {
    // draw the current state
    output.display();
    // get the current player
    Agent currentAgent = agents[currentPlayer];
    // send state to current player and ask for an action
    log.debug("ask {} to move from state {}", currentAgent.getAgentName(), coins);
    currentAgent.send(generateCurrentState());
    CoinGameAction action = currentAgent.receive(CoinGameAction.class);
    log.debug("{} asked for {} coins from row {}", currentAgent.getAgentName(),
        action.getRemoveCount(), action.getSelectedRow());
    // mark that we have recieved a move from this agent (so we know who to send a win/lose
    // message if this triggers one)
    agentMoved[currentPlayer] = true;
    // try and make the move
    try {
      coins = makeMove(action.getSelectedRow(), action.getRemoveCount());
      logWidget.addText(
          currentAgent.getAgentName() + " takes " + action.getRemoveCount() + " from row "
              + action.getSelectedRow() + " leaving " + coins[action.getSelectedRow()] + ".");
      if (isGameFinished()) {
        // current player lost
        logWidget.addText(currentAgent.getAgentName() + " lost");
        informResult((currentPlayer + 1) % 2);
        output.display();
        reset();
      } else {
        // play continues - tell the other agent
        int otherPlayer = (currentPlayer + 1) % 2;
        if (agentMoved[otherPlayer]) {
          agents[otherPlayer].send(
              CoinGameResult.newBuilder().setStatus(CoinGameSignal.PLAY).build());
        }
        // move to the next player
        currentPlayer = otherPlayer;
      }
    } catch (InvalidCoinAction e) {
      // current player has made an illegal move and losses the game
      log.error(e.getMessage());
      logWidget.addText(currentAgent.getAgentName() + " makes an invalid move.");
      // player has tried an illegal move - end the game
      informResult((currentPlayer + 1) % 2);
      output.display();
      reset();
    }

  }

  /**
   * Creates a protocol buffer message representing the current state of the game.
   * <p>
   * This state contains all information needed by an agent to make a decision, including the
   * current coin counts, maximum allowed move, and game ID's.
   *
   * @return A new CoinGameState protobuf message
   */
  private CoinGameState generateCurrentState() {
    // Create and return a protocol buffer message containing the game state
    return CoinGameState.newBuilder()
        // Convert int array of coin counts to a List<Integer>
        .addAllCoinCount(Arrays.stream(coins).boxed().toList())
        // Set the number of rows
        .setRowCount(coins.length)
        // Set the maximum number of coins a player can remove in one turn
        .setMaxPick(scenario.getMaximumTake())
        // Include session ID for tracking the entire simulation
        .setSessionID(sessionId)
        // Include episode ID for tracking the current episode
        .setEpisodeID(episodeId).build();
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
   * @throws IllegalCoinAction if the move is illegal (always wrong)
   * @throws InvalidCoinAction if the move is invalid (wrong for this state)
   */
  private int[] makeMove(int row, int amount) throws SimulationException {
    // is the amount out of the allowed range
    if (amount < 1 || amount > scenario.getMaximumTake()) {
      throw new IllegalCoinAction(
          "Must remove between 1 and " + scenario.getMaximumTake() + " coins.");
    }
    // is the row out of the allowed indexing range
    if (row < 0 || row >= coins.length) {
      throw new IllegalCoinAction("Must select row from 0 to " + (coins.length - 1) + ".");
    }
    // does the selected row have enough coins (this can be caught and turned into a loss)
    if (coins[row] < amount) {
      throw new InvalidCoinAction(
          "Not enough coins in the selected row, asked for " + amount + " from row " + row
              + ", only " + coins[row] + " available");
    }
    // make the move
    int[] newCoins = Arrays.copyOf(coins, coins.length);
    newCoins[row] -= amount;
    return newCoins;
  }

  /**
   * Checks if the game has reached a terminal state, returning true if this is the case.
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

  /**
   * Inform both players that the episode has finished.
   * <p>
   * Only send messages to players who have made moves.
   *
   * @param winner the agent index of the winner
   * @throws SimulationException
   */
  private void informResult(int winner) throws SimulationException {
    if (agentMoved[0]) {
      agents[0].send(CoinGameResult.newBuilder()
          .setStatus(winner == 0 ? CoinGameSignal.WIN : CoinGameSignal.LOSE).build());
    }
    if (agentMoved[1]) { // NOPMD - AvoidLiteralsInIfCondition: clear in context
      agents[1].send(CoinGameResult.newBuilder()
          .setStatus(winner == 1 ? CoinGameSignal.WIN : CoinGameSignal.LOSE).build());
    }
    logWidget.addText(agents[winner].getAgentName() + " wins");
    pieChartWidget.addValue(agents[winner].getAgentName(), theme.getPrimary());
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
    graphics2D.setColor(theme.getBase());
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
    graphics2D.setColor(theme.getBaizeBorder());
    graphics2D.drawRect(LEFT_MARGIN, TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, BAIZE_WIDTH,
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
    graphics2D.drawImage(theme.getLogoImage(), HD_WIDTH - LOGO_WIDTH - RIGHT_MARGIN,
        (TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING - LOGO_HEIGHT) / 2, null);

  }
}
