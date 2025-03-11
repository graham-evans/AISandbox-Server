package dev.aisandbox.server.simulation.maze;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputConstants;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.output.SpriteLoader;
import dev.aisandbox.server.simulation.bandit.BanditRuntime;
import dev.aisandbox.server.simulation.maze.proto.MazeAction;
import dev.aisandbox.server.simulation.maze.proto.MazeResult;
import dev.aisandbox.server.simulation.maze.proto.MazeState;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static dev.aisandbox.server.engine.output.OutputConstants.HD_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.HD_WIDTH;

@Slf4j
public class MazeRunner implements Simulation {

    public static final int SPRITE_SIZE = 25;
    private static final int MARGIN = 100;
    private final MazeSize mazeSize;
    private final MazeType mazeType;
    private final Theme theme;
    private final List<BufferedImage> sprites;
    private final Random rand = new Random();
    private final Agent agent;
    private int stepsLeft;
    private Maze maze;
    private BufferedImage mazeImage = null;
    private BufferedImage logo;
    private Cell currentCell;
    private final String sessionID = UUID.randomUUID().toString();
    private String episodeID;
    private double episodeScore;
    private static final double REWARD_STEP = -1.0;
    private static final double REWARD_HIT_WALL = -10.0;
    private static final double REWARD_GOAL = +1000.0;
    private static int EPISODE_LENGTH = 2000;

    public MazeRunner(Agent agent,MazeSize mazeSize, MazeType mazeType, Theme theme) {
        this.agent = agent;
        this.mazeSize = mazeSize;
        this.mazeType = mazeType;
        this.theme = theme;
        // load images
        sprites = SpriteLoader.loadSpritesFromResources("/images/maze/bridge.png", SPRITE_SIZE, SPRITE_SIZE);
        // load logo
        try {
            logo = ImageIO.read(BanditRuntime.class.getResourceAsStream("/images/AILogo.png"));
        } catch (Exception e) {
            log.error("Error loading logo", e);
            logo = new BufferedImage(OutputConstants.LOGO_WIDTH, OutputConstants.LOGO_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        }
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
        MazeState state = MazeState.newBuilder()
                .setSessionID(sessionID)
                .setEpisodeID(episodeID)
                .setMovesLeft(stepsLeft)
                .setStartX(startX)
                .setStartY(startY).build();
        MazeAction action = agent.receive(state, MazeAction.class);
        Direction direction = Direction.fromProto(action.getDirection());
        log.info("{} moves {}",agent.getAgentName(),direction);
        // try and make this move
        double score;
        stepsLeft --;
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
        episodeScore += score;
        agent.send(
                MazeResult.newBuilder()
                        .setStartX(startX)
                        .setStartY(startY)
                        .setEndX(currentCell.getPositionX())
                        .setEndY(currentCell.getPositionY())
                        .setDirection(action.getDirection())
                        .setStepScore(score)
                        .setAccumulatedScore(episodeScore)
                        .build());
        // SPECIAL CASE 1 - found end point of maze
        if (currentCell.equals(maze.getEndCell())) {
            // draw the screen an extra time
            output.display();
            currentCell = maze.getStartCell();
        }
        // SPECIAL CASE  - end of the episode?
        if (stepsLeft==0) {
            initialiseMaze();
        }

    }

    @Override
    public void visualise(Graphics2D graphics2D) {
        graphics2D.setColor(theme.getBackground());
        graphics2D.fillRect(0, 0, HD_WIDTH, HD_HEIGHT);

        // draw maze
        graphics2D.drawImage(mazeImage,MARGIN,MARGIN,maze.getWidth()*SPRITE_SIZE*maze.getZoomLevel(),maze.getHeight()*SPRITE_SIZE*maze.getZoomLevel(),null);
        // draw the player
        graphics2D.setColor(theme.getAgent1Main());
        graphics2D.fillOval(
                currentCell.getPositionX() * mazeSize.getZoomLevel() * SPRITE_SIZE + MARGIN,
                MARGIN + currentCell.getPositionY() * mazeSize.getZoomLevel() * SPRITE_SIZE ,
                mazeSize.getZoomLevel() * SPRITE_SIZE ,
                mazeSize.getZoomLevel() * SPRITE_SIZE );
        // draw logo
        graphics2D.drawImage(logo, OutputConstants.HD_WIDTH - OutputConstants.LOGO_WIDTH - MARGIN, OutputConstants.HD_HEIGHT - OutputConstants.LOGO_HEIGHT - MARGIN, null);
    }

    private void initialiseMaze() {
        maze = MazeGenerator.generateMaze(mazeSize, mazeType, rand);
        stepsLeft = EPISODE_LENGTH;
        episodeScore = 0.0;
        mazeImage = renderMaze(maze);
        currentCell = maze.getStartCell();
        episodeID = UUID.randomUUID().toString();
    }

    /**
     * Render a maze at 1:1 scale
     *
     * @param maze a {@link Maze} object.
     * @return a {@link java.awt.image.BufferedImage} object.
     */
    public BufferedImage renderMaze(Maze maze) {
        BufferedImage image =
                new BufferedImage(
                        maze.getWidth() * SPRITE_SIZE, maze.getHeight() * SPRITE_SIZE, BufferedImage.TYPE_INT_RGB);
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
            g.drawImage(sprites.get(icon), c.getPositionX() * SPRITE_SIZE, c.getPositionY() * SPRITE_SIZE, null);
        }
        Cell start = maze.getStartCell();
        if (start != null) {
            g.drawImage(
                    sprites.get(17), start.getPositionX() * SPRITE_SIZE, start.getPositionY() * SPRITE_SIZE, null);
        }
        Cell finish = maze.getEndCell();
        if (finish != null) {
            g.drawImage(
                    sprites.get(18), finish.getPositionX() * SPRITE_SIZE, finish.getPositionY() * SPRITE_SIZE, null);
        }
        return image;
    }

}
