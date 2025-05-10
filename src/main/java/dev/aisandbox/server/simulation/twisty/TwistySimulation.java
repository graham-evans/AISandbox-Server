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

@Slf4j
public final class TwistySimulation implements Simulation {

  // simulation constants
  private static final int SCRAMBLE_MOVES = 200;
  // ui constants
  private static final int WIDGET_WIDTH =
      HD_WIDTH - LEFT_MARGIN - TwistyPuzzle.WIDTH - RIGHT_MARGIN - WIDGET_SPACING;
  private static final int WIDGET_HEIGHT =
      (HD_HEIGHT - TOP_MARGIN - TITLE_HEIGHT - WIDGET_SPACING * 2 - BOTTOM_MARGIN) / 2;

  // agents
  private final Agent agent;
  private final TwistyPuzzle puzzle;
  private final boolean startSolved;
  private final Theme theme;
  // puzzle elements
  private final Random random;
  private final String sessionID = UUID.randomUUID().toString();
  private final int MAX_STEPS = 1000;
  // UI elements
  private final TitleWidget titleWidget;
  private final RollingIconWidget moveHistoryWidget;
  private final RollingSuccessStatisticsWidget statsWidget;
  int moves;
  int obtmMoves;

  private String episodeID;


  public TwistySimulation(Agent agent, TwistyPuzzle puzzle, boolean startSolved, Theme theme,
      Random random) {
    this.agent = agent;
    this.puzzle = puzzle;
    this.startSolved = startSolved;
    this.theme = theme;
    this.random = random;

    // setup ui
    titleWidget = TitleWidget.builder().title("Twisty Puzzle - " + puzzle.getPuzzleName())
        .theme(theme).build();
    moveHistoryWidget = RollingIconWidget.builder().width(WIDGET_WIDTH).height(WIDGET_HEIGHT)
        .iconWidth(Move.MOVE_ICON_WIDTH).iconHeight(Move.MOVE_ICON_HEIGHT).title("Moves")
        .theme(theme).build();
    statsWidget = RollingSuccessStatisticsWidget.builder().width(WIDGET_WIDTH).height(WIDGET_HEIGHT)
        .theme(theme).build();
    // setup puzzle
    initialisePuzzle();
  }

  public void initialisePuzzle() {
    puzzle.resetPuzzle();
    if (!startSolved) {
      scramblePuzzle();
    }
    moves = 0;
    obtmMoves=0;
    moveHistoryWidget.clearIcons();
    episodeID = UUID.randomUUID().toString();
  }

  @Override
  public void step(OutputRenderer output) throws NotExistentMoveException {
    // special case - call display if this is the start of an episode
    if (moves == 0) {
      output.display();
    }
    // generate the current state
    TwistyState.Builder builder = TwistyState.newBuilder();
    builder.setSteps(moves);
    builder.setObtmMoves(obtmMoves);
    builder.setEpisodeID(episodeID);
    builder.setSessionID(sessionID);
    builder.setState(puzzle.getState());
    builder.addAllValidMoves(puzzle.getMoveList());
    // get the next move
    TwistyAction action = agent.receive(builder.build(), TwistyAction.class);
    log.info("action: {}", action.getMove());
    // special case - reset action
    if ("reset".equalsIgnoreCase(action.getMove())) {
      // reset the puzzle
      agent.send(TwistyResult.newBuilder().setState(puzzle.getState()).setSignal(TwistySignal.LOSE)
          .build());
      statsWidget.addFailure();
      initialisePuzzle();
    } else {
      // apply the move
      MoveResult result = puzzle.applyMove(action.getMove());
      moveHistoryWidget.addIcon(result.icon());
      moves++;
      obtmMoves+=result.cost();
      if (puzzle.isSolved()) {
        log.info("solved");
        // puzzle solved
        agent.send(TwistyResult.newBuilder().setState(puzzle.getState()).setSignal(TwistySignal.WIN)
            .build());
        statsWidget.addSuccess(obtmMoves);
        output.display();
        initialisePuzzle();
      } else if (moves == MAX_STEPS) {
        log.info("max moves");
        // ran out of moves
        agent.send(
            TwistyResult.newBuilder().setState(puzzle.getState()).setSignal(TwistySignal.LOSE)
                .build());
        statsWidget.addFailure();
        output.display();
        initialisePuzzle();
      } else {
        // puzzle continues
        agent.send(
            TwistyResult.newBuilder().setState(puzzle.getState()).setSignal(TwistySignal.CONTINUE)
                .build());
        output.display();
      }
    }
  }

  private void scramblePuzzle() {
    for (int i = 0; i < SCRAMBLE_MOVES; i++) {
      try {
        String randomMove = puzzle.getMoveList().get(random.nextInt(puzzle.getMoveList().size()));
        puzzle.applyMove(randomMove);
      } catch (NotExistentMoveException e) {
        log.error("Non existent move when trying a move defined in the class", e);
      }
    }
  }


  @Override
  public void visualise(Graphics2D graphics2D) {
    // draw background
    graphics2D.setColor(theme.getBackground());
    graphics2D.fillRect(0, 0, HD_WIDTH, HD_HEIGHT);
    // draw title
    graphics2D.drawImage(titleWidget.getImage(), 0, TOP_MARGIN, null);
    // draw puzzle
    puzzle.drawPuzzle(graphics2D, LEFT_MARGIN, TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, theme);
    // add logo
    graphics2D.drawImage(LOGO, HD_WIDTH - LOGO_WIDTH - RIGHT_MARGIN,
        (TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING - LOGO_HEIGHT) / 2, null);
    // draw stats
    graphics2D.drawImage(statsWidget.getImage(), HD_WIDTH - RIGHT_MARGIN - WIDGET_WIDTH,
        TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, null);
    // draw history
    graphics2D.drawImage(moveHistoryWidget.getImage(), HD_WIDTH - RIGHT_MARGIN - WIDGET_WIDTH,
        HD_HEIGHT - BOTTOM_MARGIN - WIDGET_HEIGHT, null);

  }
}
