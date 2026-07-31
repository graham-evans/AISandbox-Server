/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

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
import dev.aisandbox.server.engine.SimulationRandomNumberGenerator;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.exception.IllegalActionException;
import dev.aisandbox.server.engine.exception.SimulationRuntimeException;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.telemetry.TelemetryEngine;
import dev.aisandbox.server.engine.telemetry.event.EpisodeScoreEvent;
import dev.aisandbox.server.engine.telemetry.event.StepProfileEvent;
import dev.aisandbox.server.engine.widget.GraphicsUtils;
import dev.aisandbox.server.engine.widget.RollingValueChartWidget;
import dev.aisandbox.server.engine.widget.TextWidget;
import dev.aisandbox.server.engine.widget.TitleWidget;
import dev.aisandbox.server.simulation.cascade.model.CascadeBoard;
import dev.aisandbox.server.simulation.cascade.model.CascadeCell;
import dev.aisandbox.server.simulation.cascade.proto.CascadeAction;
import dev.aisandbox.server.simulation.cascade.proto.CascadeResult;
import dev.aisandbox.server.simulation.cascade.proto.CascadeSignal;
import dev.aisandbox.server.simulation.cascade.proto.CascadeState;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Runtime implementation of the Cascade match-3 simulation.
 *
 * <p>Each call to {@link #step(OutputRenderer)} runs one full turn:
 * <ol>
 *   <li>Send the current {@link CascadeState} to the agent.</li>
 *   <li>Receive a {@link CascadeAction} (a tile swap request).</li>
 *   <li>If the swap is valid, apply it and resolve all cascading matches.</li>
 *   <li>Decrement the move counter and send a {@link CascadeResult}.</li>
 *   <li>When the move budget reaches zero, begin a new episode automatically.</li>
 * </ol>
 *
 * <p>If the board reaches a state with no valid moves (deadlock), it is reshuffled automatically
 * before the next state is sent; reshuffling does not affect the score or move count.
 */
@Slf4j
public final class CascadeRuntime implements Simulation {

  // ── Layout constants ────────────────────────────────────────────────────────

  /**
   * Pixel size of each board cell.
   */
  private static final int CELL_SIZE = 90;

  /**
   * Pixel gap between cells.
   */
  private static final int CELL_GAP = 4;

  /**
   * Stroke width of the highlight border drawn around activated cells.
   */
  private static final int ACTIVATED_BORDER_WIDTH = 3;

  /**
   * Full board pixel width / height (8 cells).
   */
  private static final int BOARD_PX = CascadeBoard.WIDTH * (CELL_SIZE + CELL_GAP) - CELL_GAP;

  /**
   * X coordinate where the board grid starts.
   */
  private static final int BOARD_X = LEFT_MARGIN;

  /**
   * Y coordinate where the board grid starts.
   */
  private static final int BOARD_Y = TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING;

  /**
   * X coordinate where the right panel starts.
   */
  private static final int PANEL_X = BOARD_X + BOARD_PX + WIDGET_SPACING;

  /**
   * Pixel width of the right panel.
   */
  private static final int PANEL_W = HD_WIDTH - PANEL_X - RIGHT_MARGIN;

  /**
   * Pixel height available below the title bar.
   */
  private static final int CONTENT_H = HD_HEIGHT - BOARD_Y - BOTTOM_MARGIN;

  /**
   * Height of each right-panel widget.
   */
  private static final int WIDGET_H = (CONTENT_H - WIDGET_SPACING) / 2;

  // ── Instance state ───────────────────────────────────────────────────────────

  private final Agent agent;
  private final SimulationRandomNumberGenerator random;
  private final Theme theme;
  private final TelemetryEngine telemetryEngine;
  private final CascadeIconLoader iconLoader = new CascadeIconLoader();
  @Getter
  private final String sessionId = UUID.randomUUID().toString();
  private String episodeID;
  private int episodeNumber = 0;
  private CascadeBoard board;
  private boolean gameOver = true; // triggers first episode creation in step()
  private long sessionStep = 0;

  // ── Widgets ──────────────────────────────────────────────────────────────────

  private final TitleWidget titleWidget;
  private final TextWidget logWidget;
  private final RollingValueChartWidget scoreChart;

  /**
   * Constructs a new Cascade runtime.
   *
   * @param agent           the agent that will play the game
   * @param theme           the visual theme for rendering
   * @param random          the source of randomness for board generation and tile refill
   * @param telemetryEngine
   */
  public CascadeRuntime(Agent agent, Theme theme, SimulationRandomNumberGenerator random,
      TelemetryEngine telemetryEngine) {
    this.agent = agent;
    this.theme = theme;
    this.random = random;
    this.telemetryEngine = telemetryEngine;
    titleWidget = TitleWidget.builder().title("Cascade").theme(theme).build();
    logWidget = TextWidget.builder()
        .width(PANEL_W).height(WIDGET_H).font(LOG_FONT).theme(theme).build();
    scoreChart = RollingValueChartWidget.builder()
        .width(PANEL_W).height(WIDGET_H).window(100)
        .title("Score per episode (last 100)").xTitle("Episode").yTitle("Score")
        .theme(theme).build();
  }

  // ── Simulation interface ─────────────────────────────────────────────────────

  /**
   * Advances the simulation by one turn.
   *
   * <p>A new episode is started automatically when the previous one has ended.
   * The board is reshuffled (without affecting score or moves) whenever it reaches a deadlock.
   *
   * @param output the renderer used to display the current state after processing
   * @throws SimulationRuntimeException if agent communication fails
   */
  @Override
  public void step(OutputRenderer output)
      throws SimulationRuntimeException, IllegalActionException {
    sessionStep++;
    long startStepTime = System.nanoTime();
    if (gameOver) {
      startNewEpisode();
    }

    // Reshuffle if the board has no valid moves (deadlock)
    while (!CascadeBoardUtils.isValid(board)) {
      log.debug("Board deadlocked – reshuffling");
      CascadeBoardUtils.reshuffleBoard(board, random);
      logWidget.addText("Board reshuffled (no valid moves)");
    }

    long startAgentAsk = System.nanoTime();
    agent.send(buildState());
    CascadeAction action = agent.receive(CascadeAction.class);
    telemetryEngine.writeTelemetryEvent(
        new StepProfileEvent(CascadeScenario.CASCADE_NAME, sessionId, Instant.now(), sessionStep,
            StepProfileEvent.PHASE_AGENT_ASK, System.nanoTime() - startAgentAsk));

    int ax1 = action.getX1();
    int ay1 = action.getY1();
    int ax2 = action.getX2();
    int ay2 = action.getY2();

    board.setMultiplier(1);
    long oldScore = board.getScore();
    board.consumeMove();
    try {
      board = CascadeBoardUtils.makeMove(board, ax1, ay1, ax2, ay2);
      long startVisualise1 = System.nanoTime();
      output.display();
      telemetryEngine.writeTelemetryEvent(
          new StepProfileEvent(CascadeScenario.CASCADE_NAME, sessionId, Instant.now(), sessionStep,
              StepProfileEvent.PHASE_RENDER, System.nanoTime() - startVisualise1));
      log.debug("Swapped {},{} with {},{}", ax1, ay1, ax2, ay2);
      while (!CascadeBoardUtils.isStable(board)) {
        board = CascadeBoardUtils.updateBoard(board, random);
        long startVisualise2 = System.nanoTime();
        output.display();
        telemetryEngine.writeTelemetryEvent(
            new StepProfileEvent(CascadeScenario.CASCADE_NAME, sessionId, Instant.now(),
                sessionStep, StepProfileEvent.PHASE_RENDER, System.nanoTime() - startVisualise2));
        log.debug("Board updated, score now {}", board.getScore());
      }
      logWidget.addText(
          "Swap (" + ax1 + "," + ay1 + ")<->(" + ax2 + "," + ay2 + ") +"
              + (board.getScore() - oldScore) + " pts");
    } catch (InvalidCascadeAction e) {
      logWidget.addText(
          "Invalid swap (" + ax1 + "," + ay1 + ")<->(" + ax2 + "," + ay2
              + ") - wasted move");
    }

    gameOver = board.isGameOver();

    CascadeSignal signal = gameOver ? CascadeSignal.GAME_OVER : CascadeSignal.CONTINUE;
    long startAgentReport = System.nanoTime();
    agent.send(CascadeResult.newBuilder()
        .setX1(ax1).setY1(ay1).setX2(ax2).setY2(ay2)
        .setScoreGained((board.getScore() - oldScore))
        .setTotalScore(board.getScore())
        .setSignal(signal)
        .build());
    telemetryEngine.writeTelemetryEvent(
        new StepProfileEvent(CascadeScenario.CASCADE_NAME, sessionId, Instant.now(), sessionStep,
            StepProfileEvent.PHASE_AGENT_REPORT, System.nanoTime() - startAgentReport));

    if (gameOver) {
      long finalScore = board.getScore();
      logWidget.addText("Episode ended. Final score: " + finalScore);
      scoreChart.addValue((double) finalScore);
      telemetryEngine.writeTelemetryEvent(
          new EpisodeScoreEvent(CascadeScenario.CASCADE_NAME, sessionId, episodeID, episodeNumber,
              Instant.now(), finalScore));
    }

    long startVisualise3 = System.nanoTime();
    output.display();
    telemetryEngine.writeTelemetryEvent(
        new StepProfileEvent(CascadeScenario.CASCADE_NAME, sessionId, Instant.now(), sessionStep,
            StepProfileEvent.PHASE_RENDER, System.nanoTime() - startVisualise3));

    telemetryEngine.writeTelemetryEvent(
        new StepProfileEvent(CascadeScenario.CASCADE_NAME, sessionId, Instant.now(), sessionStep,
            StepProfileEvent.PHASE_STEP, System.nanoTime() - startStepTime));
  }

  /**
   * Renders the current board state, score chart, and log to the provided graphics context.
   *
   * @param graphics2D the 1920×1080 graphics surface to draw on
   */
  @Override
  public void visualise(Graphics2D graphics2D) {
    GraphicsUtils.setupRenderingHints(graphics2D);

    // Background
    graphics2D.setColor(theme.getBase());
    graphics2D.fillRect(0, 0, HD_WIDTH, HD_HEIGHT);

    // Title bar and logo
    graphics2D.drawImage(titleWidget.getImage(), 0, TOP_MARGIN, null);
    graphics2D.drawImage(theme.getLogoImage(),
        HD_WIDTH - LOGO_WIDTH - RIGHT_MARGIN,
        (TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING - LOGO_HEIGHT) / 2, null);

    // Right-panel widgets
    graphics2D.drawImage(scoreChart.getImage(), PANEL_X, BOARD_Y, null);
    graphics2D.drawImage(logWidget.getImage(),
        PANEL_X, BOARD_Y + WIDGET_H + WIDGET_SPACING, null);

    // Board background
    graphics2D.setColor(theme.getBackground());
    graphics2D.fillRect(BOARD_X - CELL_GAP, BOARD_Y - CELL_GAP,
        BOARD_PX + CELL_GAP * 2, BOARD_PX + CELL_GAP * 2);

    // Board cells
    if (board != null) {
      for (int x = 0; x < CascadeBoard.WIDTH; x++) {
        for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
          drawCell(graphics2D, board.getCell(x, y),
              BOARD_X + x * (CELL_SIZE + CELL_GAP),
              BOARD_Y + y * (CELL_SIZE + CELL_GAP));
        }
      }
    }
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private void startNewEpisode() {
    episodeID = UUID.randomUUID().toString();
    episodeNumber++;
    board = new CascadeBoard();
    CascadeBoardUtils.initialise(board, random);
    gameOver = false;
    log.debug("New episode {} started", episodeID);
  }

  private CascadeState buildState() {
    return CascadeState.newBuilder()
        .setSessionID(sessionId)
        .setEpisodeID(episodeID)
        .setMovesRemaining(board.getMovesRemaining())
        .setScore(board.getScore())
        .addAllRow(CascadeBoardUtils.serialiseBoard(board))
        .build();
  }

  private void drawCell(Graphics2D g, CascadeCell cell, int px, int py) {
    BufferedImage icon = iconLoader.getIcon(cell.getType(), cell.getColour());
    if (icon != null) {
      g.drawImage(icon, px, py, CELL_SIZE, CELL_SIZE, null);
    }
    if (cell.isActivated()) {
      g.setColor(theme.getAccent());
      g.setStroke(new BasicStroke(ACTIVATED_BORDER_WIDTH));
      int inset = ACTIVATED_BORDER_WIDTH / 2;
      g.drawRoundRect(px + inset, py + inset, CELL_SIZE - inset * 2, CELL_SIZE - inset * 2, 12,
          12);
      g.setStroke(new BasicStroke(1));
    }
  }
}
