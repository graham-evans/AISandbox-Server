/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mine;

import static dev.aisandbox.server.engine.output.OutputConstants.BOTTOM_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.HD_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.HD_WIDTH;
import static dev.aisandbox.server.engine.output.OutputConstants.LEFT_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO_WIDTH;
import static dev.aisandbox.server.engine.output.OutputConstants.LOG_FONT;
import static dev.aisandbox.server.engine.output.OutputConstants.RIGHT_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.STATISTICS_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.TITLE_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.TOP_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.WIDGET_SPACING;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputConstants;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.output.SpriteLoader;
import dev.aisandbox.server.engine.widget.GraphicsUtils;
import dev.aisandbox.server.engine.widget.RollingPieChartWidget;
import dev.aisandbox.server.engine.widget.TextWidget;
import dev.aisandbox.server.engine.widget.TitleWidget;
import dev.aisandbox.server.simulation.mine.proto.FlagAction;
import dev.aisandbox.server.simulation.mine.proto.MazeSignal;
import dev.aisandbox.server.simulation.mine.proto.MineAction;
import dev.aisandbox.server.simulation.mine.proto.MineResult;
import dev.aisandbox.server.simulation.mine.proto.MineState;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Runtime implementation for the Mine Hunter simulation.
 * <p>
 * This class handles the execution of the Mine Hunter game, including board generation, processing
 * agent actions, updating game state, and visualizing the current state. It implements the
 * {@link Simulation} interface to integrate with the AI Sandbox framework.
 * </p>
 */
@Slf4j
public final class MineHunterRuntime implements Simulation {

  /**
   * Height of the game play area in pixels
   */
  private static final int BAIZE_HEIGHT =
      HD_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN - TITLE_HEIGHT - WIDGET_SPACING; // 1173

  /**
   * Width of the game play area in pixels (4:3 aspect ratio)
   */
  private static final int BAIZE_WIDTH = BAIZE_HEIGHT * 4 / 3; // 880
  /**
   * X-coordinate for the start of the board rendering
   */
  private static final int BOARD_START_X = LEFT_MARGIN + (BAIZE_WIDTH - 800) / 2;
  /**
   * Width of the log widget in pixels
   */
  private static final int LOG_WIDTH =
      HD_WIDTH - LEFT_MARGIN - RIGHT_MARGIN - BAIZE_WIDTH - WIDGET_SPACING;
  /**
   * Vertical spacing between elements in the game area
   */
  private static final int BAIZE_SPACING = (BAIZE_HEIGHT - 800 - STATISTICS_HEIGHT) / 3;
  /**
   * Y-coordinate for the start of the board rendering
   */
  private static final int BOARD_START_Y =
      TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + BAIZE_SPACING * 2 + STATISTICS_HEIGHT;
  /**
   * Height of the log widget in pixels
   */
  private static final int LOG_HEIGHT = (BAIZE_HEIGHT - WIDGET_SPACING) / 2;
  /**
   * The agent playing the game
   */
  private final Agent agent;

  /**
   * Random number generator for mine placement
   */
  private final Random random;

  /**
   * Configuration for the mine field dimensions and density
   */
  private final MineSize mineSize;

  /**
   * Visual theme for the simulation
   */
  private final Theme theme;

  /**
   * Unique identifier for this simulation session
   */
  private final String sessionID = UUID.randomUUID().toString();

  /**
   * Sprite images for the game board cells
   */
  private final List<BufferedImage> sprites;

  /**
   * Widget displaying win/loss statistics as a pie chart
   */
  private final RollingPieChartWidget pieChartWidget;

  /**
   * Widget displaying the game title
   */
  private final TitleWidget titleWidget;

  /**
   * Widget for logging game events
   */
  private final TextWidget logWidget;

  /**
   * Counter for boards successfully completed
   */
  private final long boardsWon = 0;

  /**
   * Counter for boards where the player hit a mine
   */
  private final long boardsLost = 0;

  /**
   * Current game board state
   */
  private Board board = null;

  /**
   * Number of flags the player has left to place
   */
  private int flagsLeft;

  /**
   * Scaling factor for rendering the board
   */
  private double scale = 1.0;

  /**
   * Unique identifier for the current game episode
   */
  private String episodeID;

  /**
   * Constructs a new Mine Hunter simulation runtime.
   *
   * @param agent    The agent that will play the game
   * @param mineSize The configuration for the mine field size and density
   * @param theme    The visual theme for rendering
   * @param random   A random number generator for creating the board
   */
  public MineHunterRuntime(Agent agent, MineSize mineSize, Theme theme, Random random) {
    this.agent = agent;
    this.random = random;
    this.mineSize = mineSize;
    this.theme = theme;

    // Load cell sprite images from resources
    sprites = SpriteLoader.loadSpritesFromResources("/images/mine/grid.png", 40, 40);

    // Initialize UI widgets
    titleWidget = TitleWidget.builder().title("Mine Hunter").theme(theme).build();
    logWidget = TextWidget.builder().width(LOG_WIDTH).height(LOG_HEIGHT).font(LOG_FONT).theme(theme)
        .build();
    pieChartWidget = RollingPieChartWidget.builder().width(LOG_WIDTH).height(LOG_HEIGHT)
        .title("% success of the last 200 episodes.").theme(theme).build();

    // Create the first game board
    getNewBoard();
  }

  /**
   * Creates a new game board with mines placed randomly. This resets the game state and prepares
   * for a new episode.
   */
  private void getNewBoard() {
    // create a board
    log.debug("Initialising board");
    board = new Board(mineSize.getWidth(), mineSize.getHeight());
    board.placeMines(random, mineSize.getCount());
    flagsLeft = mineSize.getCount();
    board.countNeighbours();

    // Calculate scaling factor based on board height
    scale = 20.0 / board.getHeight();
    log.debug("Scaling board to {}", scale);

    // Generate new episode ID
    episodeID = UUID.randomUUID().toString();
  }

  /**
   * Advances the simulation by one step, processing the agent's action and updating the game state
   * accordingly.
   *
   * @param output The renderer for displaying the game state
   */
  @Override
  public void step(OutputRenderer output) {
    // Get action from agent
    MineAction action = agent.receive(getState(), MineAction.class);

    // Process the action (place flag or dig)
    if (action.getAction().equals(FlagAction.PLACE_FLAG)) {
      logWidget.addText(
          agent.getAgentName() + ": placing flag @ " + action.getX() + "," + action.getY());
      board.placeFlag(action.getX(), action.getY());
    } else {
      logWidget.addText(
          agent.getAgentName() + ": uncovering flag @ " + action.getX() + "," + action.getY());
      board.uncover(action.getX(), action.getY());
    }

    // Build result to send back to agent
    MineResult.Builder rBuilder = MineResult.newBuilder();
    rBuilder.setAction(action.getAction());
    rBuilder.setX(action.getX());
    rBuilder.setY(action.getY());
    rBuilder.setAction(action.getAction());

    // Set appropriate signal based on game state
    rBuilder.setSignal(switch (board.getState()) {
      case GameState.LOST -> MazeSignal.LOSE;
      case GameState.WON -> MazeSignal.WIN;
      default -> MazeSignal.CONTINUE;
    });
    agent.send(rBuilder.build());

    // Update statistics if game ended
    if (board.getState() == GameState.WON) {
      logWidget.addText(agent.getAgentName() + ": won");
      pieChartWidget.addValue("win", theme.getAgent1Main());
    } else if (board.getState() == GameState.LOST) {
      logWidget.addText(agent.getAgentName() + ": lost");
      pieChartWidget.addValue("loss", theme.getAgent2Main());
    }

    // Render the current state
    output.display();

    // Create a new board if the game ended
    if (board.getState() != GameState.PLAYING) {
      getNewBoard();
    }
  }

  /**
   * Creates a protocol buffer message representing the current state of the game to be sent to the
   * agent.
   *
   * @return A {@link MineState} object containing the current game state
   */
  public MineState getState() {
    MineState.Builder builder = MineState.newBuilder();
    builder.setEpisodeID(episodeID);
    builder.setSessionID(sessionID);
    builder.setHeight(mineSize.getHeight());
    builder.setWidth(mineSize.getWidth());
    builder.setFlagsLeft(flagsLeft);
    builder.addAllRow(List.of(board.getBoardToString()));
    return builder.build();
  }

  /**
   * Renders the current state of the simulation to a graphics context. This includes the game
   * board, statistics, and UI elements.
   *
   * @param graphics2D The graphics context to render to
   */
  @Override
  public void visualise(Graphics2D graphics2D) {
    // Set up rendering quality and background
    GraphicsUtils.setupRenderingHints(graphics2D);
    graphics2D.setColor(theme.getBackground());
    graphics2D.fillRect(0, 0, HD_WIDTH, HD_HEIGHT);

    // Draw title area and logo
    graphics2D.drawImage(titleWidget.getImage(), 0, TOP_MARGIN, null);
    graphics2D.drawImage(LOGO, HD_WIDTH - LOGO_WIDTH - RIGHT_MARGIN,
        (TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING - LOGO_HEIGHT) / 2, null);

    // Draw log widget
    graphics2D.drawImage(logWidget.getImage(), HD_WIDTH - RIGHT_MARGIN - LOG_WIDTH,
        TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + LOG_HEIGHT + WIDGET_SPACING, null);

    // Draw statistics pie chart
    graphics2D.drawImage(pieChartWidget.getImage(), HD_WIDTH - RIGHT_MARGIN - LOG_WIDTH,
        TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, null);

    // Draw game area background
    graphics2D.setColor(theme.getBaize());
    graphics2D.fillRect(LEFT_MARGIN, TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, BAIZE_WIDTH,
        BAIZE_HEIGHT);

    // Draw remaining mines counter
    GraphicsUtils.drawCenteredText(graphics2D, LEFT_MARGIN,
        TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + BAIZE_SPACING, BAIZE_WIDTH,
        OutputConstants.STATISTICS_HEIGHT, "Mines Remaining : " + board.getUnfoundMines(),
        OutputConstants.STATISTICS_FONT, theme.getText());

    // Set transformation for board rendering
    graphics2D.translate(BOARD_START_X, BOARD_START_Y);
    graphics2D.scale(scale, scale);

    // Draw each cell of the board
    for (int x = 0; x < board.getWidth(); x++) {
      for (int y = 0; y < board.getHeight(); y++) {
        Cell c = board.getCell(x, y);
        graphics2D.drawImage(sprites.get(c.getPlayerViewSprite()), x * 40, y * 40, null);
      }
    }
  }
}
