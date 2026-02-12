/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mancala;

import static dev.aisandbox.server.engine.output.OutputConstants.BOTTOM_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.HD_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.HD_WIDTH;
import static dev.aisandbox.server.engine.output.OutputConstants.LEFT_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO_WIDTH;
import static dev.aisandbox.server.engine.output.OutputConstants.LOG_FONT;
import static dev.aisandbox.server.engine.output.OutputConstants.RIGHT_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.STATISTICS_FONT;
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
import dev.aisandbox.server.simulation.mancala.proto.MancalaAction;
import dev.aisandbox.server.simulation.mancala.proto.MancalaResult;
import dev.aisandbox.server.simulation.mancala.proto.MancalaSignal;
import dev.aisandbox.server.simulation.mancala.proto.MancalaState;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Mancala (Kalah variant) simulation implementation.
 *
 * <p>This simulation runs a two-player Mancala game where agents communicate using protocol
 * buffers. Each step sends the current state to the active player, receives their move,
 * executes the game logic, and handles extra turns, captures, and game-over conditions.
 */
@Slf4j
public final class MancalaGame implements Simulation {

  // Layout constants
  private static final int BAIZE_HEIGHT =
      HD_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN - TITLE_HEIGHT - WIDGET_SPACING;
  private static final int BAIZE_WIDTH = BAIZE_HEIGHT * 4 / 3;
  private static final int LOG_WIDTH =
      HD_WIDTH - LEFT_MARGIN - RIGHT_MARGIN - BAIZE_WIDTH - WIDGET_SPACING;
  private static final int LOG_HEIGHT = (BAIZE_HEIGHT - WIDGET_SPACING) / 2;

  // Board drawing constants
  private static final int PIT_DIAMETER = 80;
  private static final int PIT_SPACING = 20;
  private static final int STORE_WIDTH = 80;
  private static final int STORE_HEIGHT = 280;
  private static final int BOARD_PADDING = 30;
  private static final Font PIT_FONT = new Font("Arimo Regular", Font.BOLD, 28);
  private static final Font LABEL_FONT = new Font("Arimo Regular", Font.PLAIN, 20);

  // UI widgets
  private final Theme theme;
  private final TitleWidget titleWidget;
  private final TextWidget logWidget;
  private final RollingPieChartWidget pieChartWidget;

  // Game state
  private final String sessionId = UUID.randomUUID().toString();
  private final Agent[] agents = new Agent[2];
  private final boolean[] agentMoved = new boolean[2];
  private final int seedsPerPit;
  private MancalaBoard board;
  private int firstPlayer = 1;
  private int currentPlayer = 0;
  private String episodeId;

  /**
   * Creates a new Mancala game simulation.
   *
   * @param agents      the list of two agents
   * @param seedsPerPit the initial number of seeds in each pit
   * @param theme       the visual theme
   */
  public MancalaGame(List<Agent> agents, int seedsPerPit, Theme theme) {
    assert agents.size() == 2;
    this.agents[0] = agents.get(0);
    this.agents[1] = agents.get(1);
    this.seedsPerPit = seedsPerPit;
    this.theme = theme;

    titleWidget = TitleWidget.builder().title("Mancala").theme(theme).build();
    logWidget = TextWidget.builder()
        .width(LOG_WIDTH).height(LOG_HEIGHT).font(LOG_FONT).theme(theme).build();
    pieChartWidget = RollingPieChartWidget.builder()
        .width(LOG_WIDTH).height(LOG_HEIGHT)
        .title("Winner of the last 200 episodes.").theme(theme).build();

    reset();
  }

  /**
   * Resets the game to a new episode.
   */
  private void reset() {
    board = new MancalaBoard(seedsPerPit);
    episodeId = UUID.randomUUID().toString();
    agentMoved[0] = false;
    agentMoved[1] = false;
    firstPlayer = (firstPlayer + 1) % 2;
    currentPlayer = firstPlayer;
  }

  @Override
  public void step(OutputRenderer output) throws SimulationException {
    output.display();

    Agent currentAgent = agents[currentPlayer];
    List<Integer> validMoves = board.getValidMoves(currentPlayer);

    // Send state to current player
    currentAgent.send(buildState(validMoves));
    MancalaAction action = currentAgent.receive(MancalaAction.class);

    int pit = action.getSelectedPit();
    log.debug("{} selects pit {}", currentAgent.getAgentName(), pit);

    agentMoved[currentPlayer] = true;

    // Validate move
    try {
      validateMove(pit, validMoves);
    } catch (IllegalMancalaAction e) {
      throw e;
    } catch (InvalidMancalaAction e) {
      log.error(e.getMessage());
      logWidget.addText(currentAgent.getAgentName() + " makes an invalid move.");
      informResult((currentPlayer + 1) % 2);
      output.display();
      reset();
      return;
    }

    // Execute sow
    MancalaBoard.SowResult result = board.sow(currentPlayer, pit);
    logWidget.addText(currentAgent.getAgentName() + " sows from pit " + pit + ".");

    switch (result) {
      case EXTRA_TURN:
        logWidget.addText(currentAgent.getAgentName() + " gets an extra turn!");
        // Send PLAY to current player to continue their turn
        if (agentMoved[currentPlayer]) {
          currentAgent.send(
              MancalaResult.newBuilder().setSignal(MancalaSignal.PLAY).build());
        }
        break;
      case GAME_OVER:
        handleGameOver(output);
        break;
      default:
        // Normal move - switch to other player
        int otherPlayer = (currentPlayer + 1) % 2;
        if (agentMoved[otherPlayer]) {
          agents[otherPlayer].send(
              MancalaResult.newBuilder().setSignal(MancalaSignal.PLAY).build());
        }
        currentPlayer = otherPlayer;
        break;
    }
  }

  /**
   * Validates the selected pit index.
   *
   * @param pit        the selected pit (0-5)
   * @param validMoves the list of valid pit indices
   * @throws IllegalMancalaAction if pit index is out of range
   * @throws InvalidMancalaAction if pit is empty
   */
  private void validateMove(int pit, List<Integer> validMoves)
      throws IllegalMancalaAction, InvalidMancalaAction {
    if (pit < 0 || pit >= MancalaBoard.PITS_PER_PLAYER) {
      throw new IllegalMancalaAction(
          "Pit index must be between 0 and " + (MancalaBoard.PITS_PER_PLAYER - 1)
              + ", got " + pit);
    }
    if (!validMoves.contains(pit)) {
      throw new InvalidMancalaAction("Pit " + pit + " is empty, cannot sow from it.");
    }
  }

  /**
   * Handles end-of-game: determines winner, informs agents, and resets.
   *
   * @param output the output renderer
   * @throws SimulationException if there is an error sending results
   */
  private void handleGameOver(OutputRenderer output) throws SimulationException {
    int winner = board.getWinner();
    if (winner == -1) {
      logWidget.addText("Game ends in a draw! ("
          + board.getStore(0) + "-" + board.getStore(1) + ")");
      informDraw();
      pieChartWidget.addValue("Draw", theme.getAccent());
    } else {
      logWidget.addText(agents[winner].getAgentName() + " wins! ("
          + board.getStore(0) + "-" + board.getStore(1) + ")");
      informResult(winner);
      pieChartWidget.addValue(agents[winner].getAgentName(),
          winner == 0 ? theme.getPrimary() : theme.getSecondary());
    }
    output.display();
    reset();
  }

  /**
   * Builds the MancalaState protobuf message.
   *
   * @param validMoves the list of valid pit indices for the current player
   * @return the state message
   */
  private MancalaState buildState(List<Integer> validMoves) {
    int[] p1Pits = board.getPitsForPlayer(0);
    int[] p2Pits = board.getPitsForPlayer(1);
    return MancalaState.newBuilder()
        .setSessionID(sessionId)
        .setEpisodeID(episodeId)
        .addAllPits(Arrays.stream(p1Pits).boxed().toList())
        .addAllPits(Arrays.stream(p2Pits).boxed().toList())
        .addStores(board.getStore(0))
        .addStores(board.getStore(1))
        .setCurrentPlayer(currentPlayer)
        .addAllValidMoves(validMoves)
        .build();
  }

  /**
   * Informs both players of the game result (win/lose).
   *
   * @param winner the winning player index
   * @throws SimulationException if there is an error sending results
   */
  private void informResult(int winner) throws SimulationException {
    List<Integer> finalStores = List.of(board.getStore(0), board.getStore(1));
    if (agentMoved[0]) {
      agents[0].send(MancalaResult.newBuilder()
          .setSignal(winner == 0 ? MancalaSignal.WIN : MancalaSignal.LOSE)
          .addAllFinalStores(finalStores).build());
    }
    if (agentMoved[1]) { // NOPMD - AvoidLiteralsInIfCondition: clear in context
      agents[1].send(MancalaResult.newBuilder()
          .setSignal(winner == 1 ? MancalaSignal.WIN : MancalaSignal.LOSE)
          .addAllFinalStores(finalStores).build());
    }
  }

  /**
   * Informs both players of a draw.
   *
   * @throws SimulationException if there is an error sending results
   */
  private void informDraw() throws SimulationException {
    List<Integer> finalStores = List.of(board.getStore(0), board.getStore(1));
    for (int i = 0; i < 2; i++) {
      if (agentMoved[i]) {
        agents[i].send(MancalaResult.newBuilder()
            .setSignal(MancalaSignal.DRAW)
            .addAllFinalStores(finalStores).build());
      }
    }
  }

  @Override
  public void visualise(Graphics2D graphics2D) {
    // Fill background
    graphics2D.setColor(theme.getBase());
    graphics2D.fillRect(0, 0, HD_WIDTH, HD_HEIGHT);

    // Draw widgets
    graphics2D.drawImage(titleWidget.getImage(), 0, TOP_MARGIN, null);
    graphics2D.drawImage(pieChartWidget.getImage(), HD_WIDTH - RIGHT_MARGIN - LOG_WIDTH,
        TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, null);
    graphics2D.drawImage(logWidget.getImage(), HD_WIDTH - RIGHT_MARGIN - LOG_WIDTH,
        TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + LOG_HEIGHT + WIDGET_SPACING, null);

    // Draw baize
    int baizeX = LEFT_MARGIN;
    int baizeY = TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING;
    graphics2D.setColor(theme.getBaize());
    graphics2D.fillRect(baizeX, baizeY, BAIZE_WIDTH, BAIZE_HEIGHT);
    graphics2D.setColor(theme.getBaizeBorder());
    graphics2D.drawRect(baizeX, baizeY, BAIZE_WIDTH, BAIZE_HEIGHT);

    drawBoard(graphics2D, baizeX, baizeY);

    // Draw logo
    graphics2D.drawImage(theme.getLogoImage(), HD_WIDTH - LOGO_WIDTH - RIGHT_MARGIN,
        (TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING - LOGO_HEIGHT) / 2, null);
  }

  /**
   * Draws the Mancala board on the baize area.
   *
   * @param g      the graphics context
   * @param baizeX the x-coordinate of the baize area
   * @param baizeY the y-coordinate of the baize area
   */
  private void drawBoard(Graphics2D g, int baizeX, int baizeY) {
    // Calculate board dimensions
    int boardWidth =
        STORE_WIDTH * 2 + MancalaBoard.PITS_PER_PLAYER * (PIT_DIAMETER + PIT_SPACING)
            - PIT_SPACING + BOARD_PADDING * 4;
    int boardHeight = STORE_HEIGHT + BOARD_PADDING * 2 + 60; // extra space for labels
    int boardX = baizeX + (BAIZE_WIDTH - boardWidth) / 2;
    int boardY = baizeY + (BAIZE_HEIGHT - boardHeight) / 2;

    // Draw board background
    Color boardColor = darker(theme.getBaize(), 0.85f);
    g.setColor(boardColor);
    g.fillRoundRect(boardX, boardY, boardWidth, boardHeight, 40, 40);
    g.setColor(darker(theme.getBaizeBorder(), 0.85f));
    g.drawRoundRect(boardX, boardY, boardWidth, boardHeight, 40, 40);

    // Draw Player 2's store (left side)
    int storeLeftX = boardX + BOARD_PADDING;
    int storeY = boardY + BOARD_PADDING + 30;
    drawStore(g, storeLeftX, storeY, board.getStore(1));

    // Draw Player 1's store (right side)
    int storeRightX = boardX + boardWidth - BOARD_PADDING - STORE_WIDTH;
    drawStore(g, storeRightX, storeY, board.getStore(0));

    // Calculate pit area start
    int pitsStartX = storeLeftX + STORE_WIDTH + BOARD_PADDING;
    int pitRowTopY = storeY;
    int pitRowBottomY = storeY + STORE_HEIGHT - PIT_DIAMETER;

    // Draw Player 2's pits (top row, right to left: pit 5, 4, 3, 2, 1, 0)
    int[] p2Pits = board.getPitsForPlayer(1);
    for (int i = 0; i < MancalaBoard.PITS_PER_PLAYER; i++) {
      int displayIndex = MancalaBoard.PITS_PER_PLAYER - 1 - i;
      int pitX = pitsStartX + i * (PIT_DIAMETER + PIT_SPACING);
      boolean highlight = currentPlayer == 1 && board.getValidMoves(1).contains(displayIndex);
      drawPit(g, pitX, pitRowTopY, p2Pits[displayIndex], highlight);
      drawPitLabel(g, pitX, pitRowTopY - 22, String.valueOf(displayIndex));
    }

    // Draw Player 1's pits (bottom row, left to right: pit 0, 1, 2, 3, 4, 5)
    int[] p1Pits = board.getPitsForPlayer(0);
    for (int i = 0; i < MancalaBoard.PITS_PER_PLAYER; i++) {
      int pitX = pitsStartX + i * (PIT_DIAMETER + PIT_SPACING);
      boolean highlight = currentPlayer == 0 && board.getValidMoves(0).contains(i);
      drawPit(g, pitX, pitRowBottomY, p1Pits[i], highlight);
      drawPitLabel(g, pitX, pitRowBottomY + PIT_DIAMETER + 5, String.valueOf(i));
    }

    // Draw player labels
    g.setFont(LABEL_FONT);
    g.setColor(theme.getText());

    // Player 2 label (top)
    String p2Label = agents[1].getAgentName() + (currentPlayer == 1 ? " *" : "");
    g.drawString(p2Label, pitsStartX, boardY + 22);

    // Player 1 label (bottom)
    String p1Label = agents[0].getAgentName() + (currentPlayer == 0 ? " *" : "");
    FontMetrics fm = g.getFontMetrics();
    g.drawString(p1Label, pitsStartX,
        boardY + boardHeight - 10);

    // Store labels
    g.setFont(LABEL_FONT);
    drawCenteredString(g, agents[1].getAgentName(), storeLeftX, storeY + STORE_HEIGHT + 5,
        STORE_WIDTH);
    drawCenteredString(g, agents[0].getAgentName(), storeRightX, storeY + STORE_HEIGHT + 5,
        STORE_WIDTH);
  }

  /**
   * Draws a store (mancala) with its seed count.
   *
   * @param g      the graphics context
   * @param x      the x-coordinate
   * @param y      the y-coordinate
   * @param seeds  the number of seeds in the store
   */
  private void drawStore(Graphics2D g, int x, int y, int seeds) {
    g.setColor(darker(theme.getBaize(), 0.7f));
    g.fillRoundRect(x, y, STORE_WIDTH, STORE_HEIGHT, 30, 30);
    g.setColor(darker(theme.getBaizeBorder(), 0.7f));
    g.drawRoundRect(x, y, STORE_WIDTH, STORE_HEIGHT, 30, 30);

    // Draw seed count
    g.setFont(STATISTICS_FONT);
    g.setColor(theme.getText());
    drawCenteredString(g, String.valueOf(seeds), x, y + STORE_HEIGHT / 2 - 10, STORE_WIDTH);
  }

  /**
   * Draws a pit (house) with its seed count.
   *
   * @param g         the graphics context
   * @param x         the x-coordinate
   * @param y         the y-coordinate
   * @param seeds     the number of seeds in the pit
   * @param highlight whether this pit is a valid move for the current player
   */
  private void drawPit(Graphics2D g, int x, int y, int seeds, boolean highlight) {
    if (highlight) {
      g.setColor(brighter(theme.getBaize(), 0.7f));
    } else {
      g.setColor(darker(theme.getBaize(), 0.7f));
    }
    g.fillOval(x, y, PIT_DIAMETER, PIT_DIAMETER);
    g.setColor(darker(theme.getBaizeBorder(), 0.7f));
    g.drawOval(x, y, PIT_DIAMETER, PIT_DIAMETER);

    // Draw seed count in center
    g.setFont(PIT_FONT);
    g.setColor(theme.getText());
    FontMetrics fm = g.getFontMetrics();
    String text = String.valueOf(seeds);
    int textX = x + (PIT_DIAMETER - fm.stringWidth(text)) / 2;
    int textY = y + (PIT_DIAMETER + fm.getAscent() - fm.getDescent()) / 2;
    g.drawString(text, textX, textY);
  }

  /**
   * Draws a pit index label centered above or below a pit.
   *
   * @param g    the graphics context
   * @param x    the x-coordinate of the pit
   * @param y    the y-coordinate for the label
   * @param text the label text
   */
  private void drawPitLabel(Graphics2D g, int x, int y, String text) {
    g.setFont(LABEL_FONT);
    g.setColor(theme.getText());
    FontMetrics fm = g.getFontMetrics();
    int textX = x + (PIT_DIAMETER - fm.stringWidth(text)) / 2;
    g.drawString(text, textX, y + fm.getAscent());
  }

  /**
   * Draws a string centered horizontally within a given width.
   *
   * @param g     the graphics context
   * @param text  the text to draw
   * @param x     the left x-coordinate of the area
   * @param y     the y-coordinate baseline
   * @param width the width of the area
   */
  private void drawCenteredString(Graphics2D g, String text, int x, int y, int width) {
    FontMetrics fm = g.getFontMetrics();
    int textX = x + (width - fm.stringWidth(text)) / 2;
    g.drawString(text, textX, y + fm.getAscent());
  }

  /**
   * Returns a darker version of the given color.
   *
   * @param color  the original color
   * @param factor the darkening factor (0.0 = black, 1.0 = original)
   * @return the darker color
   */
  private static Color darker(Color color, float factor) {
    return new Color(
        Math.max((int) (color.getRed() * factor), 0),
        Math.max((int) (color.getGreen() * factor), 0),
        Math.max((int) (color.getBlue() * factor), 0),
        color.getAlpha());
  }

  /**
   * Returns a brighter version of the given color.
   *
   * @param color  the original color
   * @param factor the brightening factor (0.0 = original, 1.0 = white)
   * @return the brighter color
   */
  private static Color brighter(Color color, float factor) {
    int r = color.getRed();
    int g = color.getGreen();
    int b = color.getBlue();
    return new Color(
        r + (int) ((255 - r) * factor),
        g + (int) ((255 - g) * factor),
        b + (int) ((255 - b) * factor),
        color.getAlpha());
  }
}
