package dev.aisandbox.server.simulation.mine;

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
import dev.aisandbox.server.engine.output.OutputConstants;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.output.SpriteLoader;
import dev.aisandbox.server.engine.widget.TitleWidget;
import dev.aisandbox.server.simulation.mine.proto.FlagAction;
import dev.aisandbox.server.simulation.mine.proto.MazeSignal;
import dev.aisandbox.server.simulation.mine.proto.MineAction;
import dev.aisandbox.server.simulation.mine.proto.MineResult;
import dev.aisandbox.server.simulation.mine.proto.MineState;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MineHunterRuntime implements Simulation {

  private static final int BAIZE_HEIGHT =
      HD_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN - TITLE_HEIGHT - WIDGET_SPACING; // 1173
  private static final int BAIZE_WIDTH = BAIZE_HEIGHT * 4 / 3; // 880

  private static final int BOARD_START_X = LEFT_MARGIN + (BAIZE_WIDTH - 800) / 2;
  private static final int BOARD_START_Y =
      TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + (BAIZE_HEIGHT - 800) / 2;
  // agents
  private final Agent agent;
  // puzzle elements
  private final Random random;
  private final MineSize mineSize;
  private final Theme theme;
  private final String sessionID = UUID.randomUUID().toString();
  private final List<BufferedImage> sprites;
  //    private SuccessRateGraph winRateGraph = new SuccessRateGraph();
//    private BufferedImage winRateGraphImage = null;
  private final TitleWidget titleWidget;
  private final long boardsWon = 0;
  private final long boardsLost = 0;
  private Board board = null;
  private int flagsLeft;
  private double scale = 1.0;
  private String episodeID;

  public MineHunterRuntime(Agent agent, MineSize mineSize, Theme theme, Random random) {
    this.agent = agent;
    this.random = random;
    this.mineSize = mineSize;
    this.theme = theme;
    // load sprites
    sprites = SpriteLoader.loadSpritesFromResources("/images/mine/grid.png", 40, 40);
    titleWidget = TitleWidget.builder().title("Mine Hunter").theme(theme).build();
    // create first board
    getNewBoard();
  }

  private void getNewBoard() {
    // create a board
    log.debug("Initialising board");
    board = new Board(mineSize.getWidth(), mineSize.getHeight());
    board.placeMines(random, mineSize.getCount());
    flagsLeft = mineSize.getCount();
    board.countNeighbours();
    scale = 20.0 / board.getHeight();
    log.debug("Scaling board to {}", scale);
//        winRateGraphImage = winRateGraph.getGraph(600, 250);
    episodeID = UUID.randomUUID().toString();
  }

  @Override
  public void step(OutputRenderer output) {
    MineAction action = agent.receive(getState(), MineAction.class);
    // place flag or dig
    if (action.getAction().equals(FlagAction.PLACE_FLAG)) {
      board.placeFlag(action.getX(), action.getY());
    } else {
      board.uncover(action.getX(), action.getY());
    }
    // report state
    MineResult.Builder rBuilder = MineResult.newBuilder();
    rBuilder.setAction(action.getAction());
    rBuilder.setX(action.getX());
    rBuilder.setY(action.getY());
    rBuilder.setAction(action.getAction());
    rBuilder.setSignal(switch (board.getState()) {
      case GameState.LOST -> MazeSignal.LOSE;
      case GameState.WON -> MazeSignal.WIN;
      default -> MazeSignal.CONTINUE;
    });
    agent.send(rBuilder.build());
    if (board.getState() == GameState.WON) {
      // update stats
    } else if (board.getState() == GameState.LOST) {
      // update stats
    }
    // draw the screen
    output.display();
    // reset?
    if (board.getState() != GameState.PLAYING) {
      getNewBoard();
    }
  }


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
    // draw mines
    graphics2D.setColor(Color.BLACK);
    graphics2D.setFont(OutputConstants.LOG_FONT);
    graphics2D.drawString("Mines Remaining : " + board.getUnfoundMines(), 1200, 500);
    // transform for drawing the board
    graphics2D.translate(BOARD_START_X, BOARD_START_Y);
    graphics2D.scale(scale, scale);
    for (int x = 0; x < board.getWidth(); x++) {
      for (int y = 0; y < board.getHeight(); y++) {
        Cell c = board.getCell(x, y);
        graphics2D.drawImage(sprites.get(c.getPlayerViewSprite()), x * 40, y * 40, null);
      }
    }
  }
}
