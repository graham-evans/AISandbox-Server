/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.maze;

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
import dev.aisandbox.server.engine.output.SpriteLoader;
import dev.aisandbox.server.engine.widget.RollingValueChartWidget;
import dev.aisandbox.server.engine.widget.TextWidget;
import dev.aisandbox.server.engine.widget.TitleWidget;
import dev.aisandbox.server.simulation.maze.proto.MazeAction;
import dev.aisandbox.server.simulation.maze.proto.MazeResult;
import dev.aisandbox.server.simulation.maze.proto.MazeState;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MazeRunner implements Simulation {

  public static final int SPRITE_SIZE = 25;
  // layout constants
  private static final int BAIZE_HEIGHT =
      HD_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN - TITLE_HEIGHT - WIDGET_SPACING; // 1173
  private static final int BAIZE_WIDTH = BAIZE_HEIGHT * 4 / 3; // 880
  private static final int LOG_WIDTH =
      HD_WIDTH - LEFT_MARGIN - RIGHT_MARGIN - BAIZE_WIDTH - WIDGET_SPACING;
  private static final int MAZE_START_X = LEFT_MARGIN + (BAIZE_WIDTH - 1000) / 2;
  private static final int LOG_HEIGHT = (BAIZE_HEIGHT - WIDGET_SPACING) / 2;
  private static final int MAZE_START_Y =
      TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + (BAIZE_HEIGHT - 750) / 2;
  // Rewards
  private static final double REWARD_STEP = -1.0;
  private static final double REWARD_HIT_WALL = -10.0;
  private static final double REWARD_GOAL = +1000.0;

  private static final int EPISODE_LENGTH = 2000;
  // widgets
  private final TitleWidget titleWidget;
  private final TextWidget logWidget;
  private final RollingValueChartWidget episodeScoreWidget;
  // maze settings
  private final MazeSize mazeSize;
  private final MazeType mazeType;
  private final Theme theme;
  private final List<BufferedImage> sprites;
  private final Random random;
  private final Agent agent;
  private final String sessionID = UUID.randomUUID().toString();
  private int stepsLeft;
  private Maze maze;
  private BufferedImage mazeImage = null;
  private Cell currentCell;
  private String episodeID;

  private double episodeScore;


  public MazeRunner(Agent agent, MazeSize mazeSize, MazeType mazeType, Theme theme, Random random) {
    this.agent = agent;
    this.mazeSize = mazeSize;
    this.mazeType = mazeType;
    this.theme = theme;
    this.random = random;
    // load images
    sprites = SpriteLoader.loadSpritesFromResources("/images/maze/bridge.png", SPRITE_SIZE,
        SPRITE_SIZE);
    titleWidget = TitleWidget.builder().title("Maze - " + mazeType.name()).theme(theme).build();
    logWidget = TextWidget.builder().width(LOG_WIDTH).height(LOG_HEIGHT).font(LOG_FONT).theme(theme)
        .build();
    episodeScoreWidget = RollingValueChartWidget.builder().width(LOG_WIDTH).height(LOG_HEIGHT).theme(theme).window(200).build();
    // create a new maze
    initialiseMaze();
  }

  @Override
  public void step(OutputRenderer output) {
    // draw the current position
    output.display();
    // save the starting positions
    int startX = currentCell.getPositionX();
    int startY = currentCell.getPositionY();
    // ask for a direction to move in
    MazeState state = MazeState.newBuilder().setSessionID(sessionID).setEpisodeID(episodeID)
        .setMovesLeft(stepsLeft).setStartX(startX).setStartY(startY).setWidth(maze.getWidth()).setHeight(maze.getHeight()).build();
    MazeAction action = agent.receive(state, MazeAction.class);
    Direction direction = Direction.fromProto(action.getDirection());
    log.info("{} moves {}", agent.getAgentName(), direction);
    // try and make this move
    double score;
    stepsLeft--;
    if (currentCell.getPaths().contains(direction)) {
      // move cell
      currentCell = currentCell.getNeighbours().get(direction);
      // is this the finish
      if (currentCell.equals(maze.getEndCell())) {
        score = REWARD_GOAL;
      } else {
        score = REWARD_STEP;
      }
    } else {
      // hit wall - dont move
      score = REWARD_HIT_WALL;

    }
    // report result
    logWidget.addText("Move "+direction.name()+" ("+score+")");
    episodeScore += score;
    agent.send(MazeResult.newBuilder().setStartX(startX).setStartY(startY)
        .setEndX(currentCell.getPositionX()).setEndY(currentCell.getPositionY())
        .setDirection(action.getDirection()).setStepScore(score).setAccumulatedScore(episodeScore)
        .build());
    // SPECIAL CASE 1 - found end point of maze
    if (currentCell.equals(maze.getEndCell())) {
      // draw the screen an extra time
      output.display();
      currentCell = maze.getStartCell();
    }
    // SPECIAL CASE  - end of the episode?
    if (stepsLeft == 0) {
      logWidget.addText("Episode finished, resetting maze");
      episodeScoreWidget.addValue(episodeScore);
      initialiseMaze();
    }

  }

  @Override
  public void visualise(Graphics2D graphics2D) {
    graphics2D.setColor(theme.getBackground());
    graphics2D.fillRect(0, 0, HD_WIDTH, HD_HEIGHT);
    // draw title
    graphics2D.drawImage(titleWidget.getImage(), 0, TOP_MARGIN, null);
    graphics2D.drawImage(LOGO, HD_WIDTH - LOGO_WIDTH - RIGHT_MARGIN,
        (TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING - LOGO_HEIGHT) / 2, null);
    // draw baize
    graphics2D.setColor(theme.getBaize());
    graphics2D.fillRect(LEFT_MARGIN, TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, BAIZE_WIDTH,
        BAIZE_HEIGHT);
    // draw maze
    graphics2D.drawImage(mazeImage, MAZE_START_X, MAZE_START_Y,
        maze.getWidth() * SPRITE_SIZE * maze.getZoomLevel(),
        maze.getHeight() * SPRITE_SIZE * maze.getZoomLevel(), null);
    // draw the player
    graphics2D.setColor(theme.getAgent1Main());
    graphics2D.fillOval(
        currentCell.getPositionX() * mazeSize.getZoomLevel() * SPRITE_SIZE + MAZE_START_X,
        MAZE_START_Y + currentCell.getPositionY() * mazeSize.getZoomLevel() * SPRITE_SIZE,
        mazeSize.getZoomLevel() * SPRITE_SIZE, mazeSize.getZoomLevel() * SPRITE_SIZE);
    // draw log
    graphics2D.drawImage(logWidget.getImage(), HD_WIDTH - RIGHT_MARGIN - LOG_WIDTH,
        TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + LOG_HEIGHT + WIDGET_SPACING, null);
    // draw episode scores
    graphics2D.drawImage(episodeScoreWidget.getImage(),HD_WIDTH - RIGHT_MARGIN - LOG_WIDTH,
        TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING,null);
  }

  private void initialiseMaze() {
    // create a new maze
    maze = MazeGenerator.generateMaze(mazeSize, mazeType, random);
    // reset the steps
    stepsLeft = EPISODE_LENGTH;
    // reset the score
    episodeScore = 0.0;
    // generate maze image
    mazeImage = renderMaze(maze);
    // set start cell
    currentCell = maze.getStartCell();
    // set episode ID
    episodeID = UUID.randomUUID().toString();
  }

  /**
   * Render a maze at 1:1 scale
   *
   * @param maze a {@link Maze} object.
   * @return a {@link java.awt.image.BufferedImage} object.
   */
  public BufferedImage renderMaze(Maze maze) {
    BufferedImage image = new BufferedImage(maze.getWidth() * SPRITE_SIZE,
        maze.getHeight() * SPRITE_SIZE, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();
    for (Cell c : maze.getCellList()) {
      // work out which sprite to load
      int icon = 0;
      if (!c.isPath(Direction.NORTH)) {
        icon += 1;
      }
      if (!c.isPath(Direction.EAST)) {
        icon += 2;
      }
      if (!c.isPath(Direction.SOUTH)) {
        icon += 4;
      }
      if (!c.isPath(Direction.WEST)) {
        icon += 8;
      }
      g.drawImage(sprites.get(icon), c.getPositionX() * SPRITE_SIZE, c.getPositionY() * SPRITE_SIZE,
          null);
    }

    Cell finish = maze.getEndCell();
    if (finish != null) {
      g.drawImage(sprites.get(18), finish.getPositionX() * SPRITE_SIZE,
          finish.getPositionY() * SPRITE_SIZE, null);
    }
    return image;
  }

}
