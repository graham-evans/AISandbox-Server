/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.twisty;


import static dev.aisandbox.server.engine.output.OutputConstants.BOTTOM_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.HD_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.HD_WIDTH;
import static dev.aisandbox.server.engine.output.OutputConstants.LEFT_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO_WIDTH;
import static dev.aisandbox.server.engine.output.OutputConstants.RIGHT_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.TITLE_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.TOP_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.WIDGET_SPACING;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.exception.SimulationException;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.widget.RollingIconWidget;
import dev.aisandbox.server.engine.widget.RollingSuccessStatisticsWidget;
import dev.aisandbox.server.engine.widget.TitleWidget;
import dev.aisandbox.server.simulation.twisty.model.Move;
import dev.aisandbox.server.simulation.twisty.model.MoveResult;
import dev.aisandbox.server.simulation.twisty.model.TwistyPuzzle;
import dev.aisandbox.server.simulation.twisty.proto.TwistyAction;
import dev.aisandbox.server.simulation.twisty.proto.TwistyResult;
import dev.aisandbox.server.simulation.twisty.proto.TwistySignal;
import dev.aisandbox.server.simulation.twisty.proto.TwistyState;
import java.awt.Graphics2D;
import java.util.Random;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * A simulation for twisty puzzles (such as Rubik's cube and similar puzzles). This class handles
 * the puzzle state, visualization, and interaction with AI agents. It manages the simulation
 * lifecycle including scrambling puzzles, processing moves, tracking statistics, and visualizing
 * the current state.
 */
@Slf4j
public final class TwistySimulation implements Simulation {

  // Simulation constants
  /**
   * Number of random moves to apply when scrambling the puzzle.
   */
  private static final int SCRAMBLE_MOVES = 200;

  private static final String RESET_MOVE = "reset";

  // UI constants
  /**
   * Width of the sidebar widgets based on screen dimensions and puzzle width.
   */
  private static final int WIDGET_WIDTH =
      HD_WIDTH - LEFT_MARGIN - TwistyPuzzle.WIDTH - RIGHT_MARGIN - WIDGET_SPACING;
  /**
   * Height of each sidebar widget, calculated to fit evenly on screen.
   */
  private static final int WIDGET_HEIGHT =
      (HD_HEIGHT - TOP_MARGIN - TITLE_HEIGHT - WIDGET_SPACING * 2 - BOTTOM_MARGIN) / 2;

  // Core simulation components
  /**
   * Maximum number of steps allowed before failing the episode.
   */
  private static final int MAX_STEPS = 1000;
  /**
   * The AI agent that will interact with the puzzle.
   */
  private final Agent agent;
  /**
   * The twisty puzzle being simulated.
   */
  private final TwistyPuzzle puzzle;
  /**
   * Whether to start with a solved puzzle or a scrambled one.
   */
  private final boolean startSolved;
  /**
   * Visual theme for rendering.
   */
  private final Theme theme;
  /**
   * Random number generator for puzzle scrambling.
   */
  private final Random random;
  /**
   * Unique identifier for this simulation session.
   */
  private final String sessionID = UUID.randomUUID().toString();

  // UI elements
  /**
   * Widget for displaying the simulation title.
   */
  private final TitleWidget titleWidget;
  /**
   * Widget for displaying the history of moves made.
   */
  private final RollingIconWidget moveHistoryWidget;
  /**
   * Widget for displaying success/failure statistics.
   */
  private final RollingSuccessStatisticsWidget statsWidget;

  /**
   * Number of moves made in the current episode.
   */
  int moves;
  /**
   * Number of OBTM (Outer Block Turn Metric) moves made in the current episode.
   */
  int obtmMoves;
  /**
   * Unique identifier for the current episode.
   */
  private String episodeID;

  /**
   * Constructs a new TwistySimulation with the specified parameters.
   *
   * @param agent       The AI agent that will interact with the puzzle
   * @param puzzle      The twisty puzzle to be simulated
   * @param startSolved Whether to start with a solved puzzle (true) or scrambled (false)
   * @param theme       The visual theme to use for rendering
   * @param random      Random number generator for puzzle scrambling
   */
  public TwistySimulation(Agent agent, TwistyPuzzle puzzle, boolean startSolved, Theme theme,
      Random random) {
    this.agent = agent;
    this.puzzle = puzzle;
    this.startSolved = startSolved;
    this.theme = theme;
    this.random = random;

    // Setup UI components
    titleWidget = TitleWidget.builder().title("Twisty Puzzle - " + puzzle.getPuzzleName())
        .theme(theme).build();
    moveHistoryWidget = RollingIconWidget.builder().width(WIDGET_WIDTH).height(WIDGET_HEIGHT)
        .iconWidth(Move.MOVE_ICON_WIDTH).iconHeight(Move.MOVE_ICON_HEIGHT).title("Moves")
        .theme(theme).build();
    statsWidget = RollingSuccessStatisticsWidget.builder().width(WIDGET_WIDTH).height(WIDGET_HEIGHT)
        .theme(theme).build();

    // Initialize the puzzle state
    initialisePuzzle();
  }

  /**
   * Initializes or resets the puzzle to its starting state. If startSolved is false, the puzzle
   * will be scrambled.
   */
  public void initialisePuzzle() {
    puzzle.resetPuzzle();
    if (!startSolved) {
      scramblePuzzle();
    }
    moves = 0;
    obtmMoves = 0;
    moveHistoryWidget.clearIcons();
    episodeID = UUID.randomUUID().toString();
  }

  /**
   * Scrambles the puzzle by applying a sequence of random moves. The number of random moves is
   * defined by SCRAMBLE_MOVES.
   */
  private void scramblePuzzle() {
    for (int i = 0; i < SCRAMBLE_MOVES; i++) {
      try {
        // Select a random move from the valid move list
        String randomMove = puzzle.getMoveList().get(random.nextInt(puzzle.getMoveList().size()));
        puzzle.applyMove(randomMove);
      } catch (NotExistentMoveException e) {
        // This should never happen since we're selecting from valid moves
        log.error("Non existent move when trying a move defined in the class", e);
      }
    }
  }

  /**
   * Executes a single step of the simulation. This involves sending the current state to the agent,
   * receiving a move action, applying that move to the puzzle, and handling any resulting state
   * changes.
   *
   * @param output The renderer for displaying the simulation
   * @throws NotExistentMoveException If the agent attempts an invalid move
   */
  @Override
  public void step(OutputRenderer output) throws SimulationException {
    // Special case - call display if this is the start of an episode
    if (moves == 0) {
      output.display();
    }

    // Generate the current state to send to the agent
    TwistyState.Builder builder = TwistyState.newBuilder();
    builder.setSteps(moves);
    builder.setObtmMoves(obtmMoves);
    builder.setEpisodeID(episodeID);
    builder.setSessionID(sessionID);
    builder.setState(puzzle.getState());
    builder.setPuzzleName(puzzle.getPuzzleName());
    builder.addAllValidMoves(puzzle.getMoveList());

    // Get the next move from the agent
    agent.send(builder.build());
    TwistyAction action = agent.receive(TwistyAction.class);
    log.info("action: {}", action.getMove());

    // Special case - handle reset action
    if (RESET_MOVE.equalsIgnoreCase(action.getMove())) {
      // Reset the puzzle and report failure
      agent.send(TwistyResult.newBuilder().setState(puzzle.getState()).setSignal(TwistySignal.LOSE)
          .build());
      statsWidget.addFailure();
      initialisePuzzle();
    } else {
      // Apply the regular move
      MoveResult result = puzzle.applyMove(action.getMove());
      moveHistoryWidget.addIcon(result.icon());
      moves++;
      obtmMoves += result.cost();

      if (puzzle.isSolved()) {
        // Puzzle is solved - success case
        log.info("solved");
        agent.send(TwistyResult.newBuilder().setState(puzzle.getState()).setSignal(TwistySignal.WIN)
            .build());
        statsWidget.addSuccess(obtmMoves);
        output.display();
        initialisePuzzle();
      } else if (moves == MAX_STEPS) {
        // Maximum moves reached - failure case
        log.info("max moves");
        agent.send(
            TwistyResult.newBuilder().setState(puzzle.getState()).setSignal(TwistySignal.LOSE)
                .build());
        statsWidget.addFailure();
        output.display();
        initialisePuzzle();
      } else {
        // Puzzle continues - send state and continue
        agent.send(
            TwistyResult.newBuilder().setState(puzzle.getState()).setSignal(TwistySignal.CONTINUE)
                .build());
        output.display();
      }
    }
  }

  /**
   * Renders the current state of the simulation to the provided Graphics2D context. This includes
   * the background, title, puzzle visualization, logo, statistics widget, and move history widget.
   *
   * @param graphics2D The graphics context to render to
   */
  @Override
  public void visualise(Graphics2D graphics2D) {
    // Draw background
    graphics2D.setColor(theme.getBase());
    graphics2D.fillRect(0, 0, HD_WIDTH, HD_HEIGHT);

    // Draw title at the top
    graphics2D.drawImage(titleWidget.getImage(), 0, TOP_MARGIN, null);

    // Draw the puzzle in the main area
    puzzle.drawPuzzle(graphics2D, LEFT_MARGIN, TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, theme);

    // Draw the logo in the upper right
    graphics2D.drawImage(LOGO, HD_WIDTH - LOGO_WIDTH - RIGHT_MARGIN,
        (TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING - LOGO_HEIGHT) / 2, null);

    // Draw statistics widget in the middle right
    graphics2D.drawImage(statsWidget.getImage(), HD_WIDTH - RIGHT_MARGIN - WIDGET_WIDTH,
        TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, null);

    // Draw move history widget in the bottom right
    graphics2D.drawImage(moveHistoryWidget.getImage(), HD_WIDTH - RIGHT_MARGIN - WIDGET_WIDTH,
        HD_HEIGHT - BOTTOM_MARGIN - WIDGET_HEIGHT, null);
  }
}
