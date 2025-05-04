package dev.aisandbox.server.simulation.twisty;


import static dev.aisandbox.server.engine.output.OutputConstants.HD_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.HD_WIDTH;
import static dev.aisandbox.server.engine.output.OutputConstants.LEFT_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO_WIDTH;
import static dev.aisandbox.server.engine.output.OutputConstants.MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.RIGHT_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.TITLE_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.TOP_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.WIDGET_SPACING;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.widget.TitleWidget;
import dev.aisandbox.server.simulation.twisty.model.Move;
import dev.aisandbox.server.simulation.twisty.model.TwistyPuzzle;
import dev.aisandbox.server.simulation.twisty.proto.TwistyAction;
import dev.aisandbox.server.simulation.twisty.proto.TwistyResult;
import dev.aisandbox.server.simulation.twisty.proto.TwistySignal;
import dev.aisandbox.server.simulation.twisty.proto.TwistyState;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TwistySimulation implements Simulation {

  private static final int SCRAMBLE_MOVES = 200;
  private static final int HISTORY_WIDTH = 9;
  private static final int HISTORY_HEIGHT = 5;
  private static final int MOVE_HISTORY_MAX = HISTORY_WIDTH * HISTORY_HEIGHT;
  // agents
  private final Agent agent;
  private final TwistyPuzzle puzzle;
  private final boolean startSolved;
  private final Theme theme;
  // puzzle elements
  private final Random random;
  private final String sessionID = UUID.randomUUID().toString();
  private final int MAX_MOVES = 1000;
  // UI elements
  private final TitleWidget titleWidget;
  private final List<String> moveHistory = new ArrayList<>();
  String savedState;
  List<String> actions = new ArrayList<>();
  int moves;
  private String episodeID;


  public TwistySimulation(Agent agent, TwistyPuzzle puzzle, boolean startSolved, Theme theme,
      Random random) {
    this.agent = agent;
    this.puzzle = puzzle;
    this.startSolved = startSolved;
    this.theme = theme;
    this.random = random;
    // setup puzzle
    initialisePuzzle();
    // setup ui
    titleWidget = TitleWidget.builder().title("Twisty Puzzle - "+puzzle.getPuzzleName()).theme(theme).build();
    /*frequencyGraph.setTitle("# Moves to solve");
    frequencyGraph.setXaxisHeader("# Moves");
    frequencyGraph.setYaxisHeader("Frequency");
    frequencyGraph.setGraphWidth(HISTORY_WIDTH * Move.MOVE_ICON_WIDTH);
    frequencyGraph.setGraphHeight(350);*/
  }


  public void initialisePuzzle() {
    puzzle.resetPuzzle();
    if (!startSolved) {
      scramblePuzzle();
    }
    moves = 0;
    moveHistory.clear();
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
    builder.setMoves(moves);
    builder.setEpisodeID(episodeID);
    builder.setSessionID(sessionID);
    builder.setState(puzzle.getState());
    builder.addAllValidMoves(puzzle.getMoveList());
    // get the next move
    TwistyAction action = agent.receive(builder.build(), TwistyAction.class);
    // special case - reset action
    if ("reset".equalsIgnoreCase(action.getMove())) {
      // reset the puzzle
      agent.send(TwistyResult.newBuilder().setState(puzzle.getState()).setSignal(TwistySignal.LOSE)
          .build());
      initialisePuzzle();
    } else {
      // apply the move
      puzzle.applyMove(action.getMove());
      moves++;
      moveHistory.add(action.getMove());
      if (puzzle.isSolved()) {
        // puzzle solved
        agent.send(TwistyResult.newBuilder().setState(puzzle.getState()).setSignal(TwistySignal.WIN)
            .build());
        output.display();
        initialisePuzzle();
      } else if (moves == MAX_MOVES) {
        // ran out of moves
        agent.send(
            TwistyResult.newBuilder().setState(puzzle.getState()).setSignal(TwistySignal.LOSE)
                .build());
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

 /* @Override
  public void writeStatistics(File statisticsOutputFile) {
    try {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(statisticsOutputFile)));
      out.print("mean,");
      out.println(frequencyGraph.getMean());
      out.print("std,");
      out.println(frequencyGraph.getStandardDeviation());
      out.println("Values");
      Iterator<Entry<Comparable<?>, Long>> iterator =
          frequencyGraph.getFrequencyTable().entrySetIterator();
      while (iterator.hasNext()) {
        Entry<Comparable<?>, Long> e = iterator.next();
        out.print(e.getKey());
        out.print(",");
        out.println(e.getValue());
      }
      out.close();
    } catch (IOException e) {
      log.error("Error writing stats to file " + statisticsOutputFile.getAbsolutePath(), e);
    }
  }*/

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
    puzzle.drawPuzzle(graphics2D, LEFT_MARGIN, TOP_MARGIN+TITLE_HEIGHT+WIDGET_SPACING, theme);
    // add logo
    graphics2D.drawImage(LOGO, HD_WIDTH - LOGO_WIDTH - RIGHT_MARGIN,
        (TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING - LOGO_HEIGHT) / 2, null);
    // draw history
    for (int i = 0; i < moveHistory.size(); i++) {
      BufferedImage moveImage = puzzle.getMoveImage(moveHistory.get(i));
      if (moveImage != null) {
        graphics2D.drawImage(moveImage, (i % HISTORY_WIDTH) * Move.MOVE_ICON_WIDTH + 1350,
            (i / HISTORY_WIDTH) * Move.MOVE_ICON_HEIGHT + 550, null);
      }
    }
    //draw graphs
/*    g.setColor(Color.BLACK);
    if (frequencyGraphImage != null) {
      g.drawImage(frequencyGraphImage, 1350, 100, null);
      g.drawString(
          "Mean : " + BaseAWTGraph.toSignificantDigitString(frequencyGraph.getMean(), 5),
          1400,
          480);
      g.drawString(
          "\u03C3\u00B2 : "
              + BaseAWTGraph.toSignificantDigitString(frequencyGraph.getStandardDeviation(), 5),
          1400,
          480 + 24);
    }*/
  }
}
