package dev.aisandbox.server.simulation.mine;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.output.SpriteLoader;
import dev.aisandbox.server.simulation.mine.proto.*;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static dev.aisandbox.server.engine.output.OutputConstants.*;

@Slf4j
public class MineHunterRuntime implements Simulation {
    // agents
    private final Agent agent;
    // puzzle elements
    private final Random random;
    private final MineSize mineSize;
    private final Theme theme;
    private final String sessionID = UUID.randomUUID().toString();
    Font myFont = new Font("Sans-Serif", Font.PLAIN, 28);
    private Board board = null;
    private int flagsLeft;
    private List<BufferedImage> sprites;
    //    private SuccessRateGraph winRateGraph = new SuccessRateGraph();
//    private BufferedImage winRateGraphImage = null;
    private long boardsWon = 0;
    private long boardsLost = 0;
    private double scale = 1.0;
    private String episodeID;

    public MineHunterRuntime(Agent agent, MineSize mineSize, Theme theme, Random random) {
        this.agent = agent;
        this.random = random;
        this.mineSize = mineSize;
        this.theme = theme;
        // load sprites
        sprites = SpriteLoader.loadSpritesFromResources("/images/mine/grid.png", 40, 40);
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
        // draw logo
        graphics2D.drawImage(LOGO, HD_WIDTH - LOGO_WIDTH - MARGIN, HD_HEIGHT - LOGO_HEIGHT - MARGIN, null);

        // draw mines
//        g.drawImage(winRateGraphImage, 1200, 200, null);
        graphics2D.setColor(Color.BLACK);
        graphics2D.setFont(myFont);
        graphics2D.drawString("Mines Remaining : " + board.getUnfoundMines(), 1200, 500);
        // transform for drawing the board
        graphics2D.translate(100, 200);
        graphics2D.scale(scale, scale);
        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                Cell c = board.getCell(x, y);
                switch (c.getPlayerView()) {
                    case '#':
                        graphics2D.drawImage(sprites.get(11), x * 40, y * 40, null);
                        break;
                    case 'F':
                        graphics2D.drawImage(sprites.get(12), x * 40, y * 40, null);
                        break;
                    case 'f':
                        graphics2D.drawImage(sprites.get(13), x * 40, y * 40, null);
                        break;
                    case 'X':
                        graphics2D.drawImage(sprites.get(10), x * 40, y * 40, null);
                        break;
                    case '.':
                        graphics2D.drawImage(sprites.get(0), x * 40, y * 40, null);
                        break;
                    case '1':
                        graphics2D.drawImage(sprites.get(1), x * 40, y * 40, null);
                        break;
                    case '2':
                        graphics2D.drawImage(sprites.get(2), x * 40, y * 40, null);
                        break;
                    case '3':
                        graphics2D.drawImage(sprites.get(3), x * 40, y * 40, null);
                        break;
                    case '4':
                        graphics2D.drawImage(sprites.get(4), x * 40, y * 40, null);
                        break;
                    case '5':
                        graphics2D.drawImage(sprites.get(5), x * 40, y * 40, null);
                        break;
                    case '6':
                        graphics2D.drawImage(sprites.get(6), x * 40, y * 40, null);
                        break;
                    case '7':
                        graphics2D.drawImage(sprites.get(7), x * 40, y * 40, null);
                        break;
                    case '8':
                        graphics2D.drawImage(sprites.get(8), x * 40, y * 40, null);
                        break;
                    case '9':
                        graphics2D.drawImage(sprites.get(9), x * 40, y * 40, null);
                        break;
                    default:
                        log.warn("Unexpected char");
                }
            }
        }
    }
}
