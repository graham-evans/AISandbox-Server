/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.exception.IllegalActionException;
import dev.aisandbox.server.engine.exception.SimulationRuntimeException;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.telemetry.EpisodeLongScoreEvent;
import dev.aisandbox.server.engine.telemetry.TelemetryEngine;
import dev.aisandbox.server.engine.widget.GraphicsUtils;
import dev.aisandbox.server.engine.widget.RollingValueChartWidget;
import dev.aisandbox.server.engine.widget.TextWidget;
import dev.aisandbox.server.engine.widget.TitleWidget;
import dev.aisandbox.server.simulation.cascade.model.CascadeBoard;
import dev.aisandbox.server.simulation.cascade.model.CascadeCell;
import dev.aisandbox.server.simulation.cascade.model.TileType;
import dev.aisandbox.server.simulation.cascade.proto.CascadeAction;
import dev.aisandbox.server.simulation.cascade.proto.CascadeResult;
import dev.aisandbox.server.simulation.cascade.proto.CascadeSignal;
import dev.aisandbox.server.simulation.cascade.proto.CascadeState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import static dev.aisandbox.server.engine.output.OutputConstants.*;

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

  /** Pixel size of each board cell. */
  private static final int CELL_SIZE = 90;

  /** Pixel gap between cells. */
  private static final int CELL_GAP = 4;

  /** Full board pixel width / height (8 cells). */
  private static final int BOARD_PX = CascadeBoard.WIDTH * (CELL_SIZE + CELL_GAP) - CELL_GAP;

  /** X coordinate where the board grid starts. */
  private static final int BOARD_X = LEFT_MARGIN;

  /** Y coordinate where the board grid starts. */
  private static final int BOARD_Y = TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING;

  /** X coordinate where the right panel starts. */
  private static final int PANEL_X = BOARD_X + BOARD_PX + WIDGET_SPACING;

  /** Pixel width of the right panel. */
  private static final int PANEL_W = HD_WIDTH - PANEL_X - RIGHT_MARGIN;

  /** Pixel height available below the title bar. */
  private static final int CONTENT_H = HD_HEIGHT - BOARD_Y - BOTTOM_MARGIN;

  /** Height of each right-panel widget. */
  private static final int WIDGET_H = (CONTENT_H - WIDGET_SPACING) / 2;

  // ── Tile colours ────────────────────────────────────────────────────────────

  private static final Color COLOR_RED    = new Color(210, 50,  50);
  private static final Color COLOR_BLUE   = new Color(50,  100, 210);
  private static final Color COLOR_GREEN  = new Color(50,  175, 60);
  private static final Color COLOR_YELLOW = new Color(210, 190, 30);
  private static final Color COLOR_PURPLE = new Color(150, 50,  200);
  private static final Color COLOR_STONE  = new Color(110, 110, 110);
  private static final Color COLOR_ICE    = new Color(160, 215, 235);
  private static final Color COLOR_PRISM  = new Color(255, 255, 255);

  private static final Font CELL_FONT = new Font("Arimo Regular", Font.BOLD, 20);

  // ── Instance state ───────────────────────────────────────────────────────────

  private final Agent agent;
  private final Random random;
  private final Theme theme;
  private final TelemetryEngine telemetryEngine;
  @Getter
  private final String sessionId = UUID.randomUUID().toString();
  private String episodeID;

  private CascadeBoard board;
  private boolean gameOver = true; // triggers first episode creation in step()

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
  public CascadeRuntime(Agent agent, Theme theme, Random random, TelemetryEngine telemetryEngine) {
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
  public void step(OutputRenderer output) throws SimulationRuntimeException, IllegalActionException {
    if (gameOver) {
      startNewEpisode();
    }

    // Reshuffle if the board has no valid moves (deadlock)
    while (!CascadeBoardUtils.isValid(board)) {
      log.debug("Board deadlocked – reshuffling");
      CascadeBoardUtils.reshuffleBoard(board, random);
      logWidget.addText("Board reshuffled (no valid moves)");
    }

    agent.send(buildState());
    CascadeAction action = agent.receive(CascadeAction.class);

    int ax1 = action.getX1();
    int ay1 = action.getY1();
    int ax2 = action.getX2();
    int ay2 = action.getY2();

    board.setMultiplier(1);
    long oldScore = board.getScore();
    board.consumeMove();
    try {
      board = CascadeBoardUtils.makeMove(board, ax1, ay1, ax2, ay2);
      output.display();
      log.debug("Swapped {},{} with {},{}", ax1, ay1, ax2, ay2);
      while (!CascadeBoardUtils.isStable(board)) {
        board = CascadeBoardUtils.updateBoard(board, random);
        output.display();
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
    agent.send(CascadeResult.newBuilder()
        .setX1(ax1).setY1(ay1).setX2(ax2).setY2(ay2)
        .setScoreGained((board.getScore() - oldScore))
        .setTotalScore(board.getScore())
        .setSignal(signal)
        .build());

    if (gameOver) {
      long finalScore = board.getScore();
      logWidget.addText("Episode ended. Final score: " + finalScore);
      scoreChart.addValue((double) finalScore);
      telemetryEngine.writeTelemetryEvent(new EpisodeLongScoreEvent(CascadeScenario.CASCADE_NAME, sessionId,episodeID
              , Instant.now(), finalScore));
    }

    output.display();
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
    board = new CascadeBoard();
    CascadeBoardUtils.initialise(board,random);
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
    Color fill = cellColor(cell);
    if (fill == null) {
      // Empty cell
      g.setColor(theme.getBackground().darker());
      g.fillRoundRect(px, py, CELL_SIZE, CELL_SIZE, 12, 12);
      return;
    }

    g.setColor(fill);
    g.fillRoundRect(px, py, CELL_SIZE, CELL_SIZE, 12, 12);

    // Darker border
    g.setColor(fill.darker());
    g.setStroke(new BasicStroke(2));
    g.drawRoundRect(px, py, CELL_SIZE, CELL_SIZE, 12, 12);
    g.setStroke(new BasicStroke(1));

    // Type label for non-standard tiles
    String label = typeLabel(cell.getType());
    if (!label.isEmpty()) {
      g.setFont(CELL_FONT);
      g.setColor(Color.WHITE);
      GraphicsUtils.drawCenteredText(g, px, py, CELL_SIZE, CELL_SIZE, label, CELL_FONT,
          Color.WHITE);
    }
  }

  private static Color cellColor(CascadeCell cell) {
    if (cell.getType() == TileType.EMPTY) {
      return null;
    }
    if (cell.getType() == TileType.STONE) {
      return COLOR_STONE;
    }
    if (cell.getType() == TileType.PRISM) {
      return COLOR_PRISM;
    }
    // Colour-bearing types (standard, bomb, rocket, ice)
    Color base = switch (cell.getColour()) {
      case RED    -> COLOR_RED;
      case BLUE   -> COLOR_BLUE;
      case GREEN  -> COLOR_GREEN;
      case YELLOW -> COLOR_YELLOW;
      case PURPLE -> COLOR_PURPLE;
      default     -> Color.GRAY;
    };
    if (cell.getType() == TileType.ICE) {
      // Blend base colour with ice tint
      return blend(base, COLOR_ICE, 0.5f);
    }
    return base;
  }

  private static String typeLabel(TileType type) {
    return switch (type) {
      case BOMB     -> "B";
      case ROCKET_H -> "H";
      case ROCKET_V -> "V";
      case PRISM    -> "P";
      case ICE      -> "i";
      default       -> "";
    };
  }

  private static Color blend(Color a, Color b, float t) {
    int r = (int) (a.getRed()   * (1 - t) + b.getRed()   * t);
    int gr = (int) (a.getGreen() * (1 - t) + b.getGreen() * t);
    int bl = (int) (a.getBlue()  * (1 - t) + b.getBlue()  * t);
    return new Color(r, gr, bl);
  }
}
