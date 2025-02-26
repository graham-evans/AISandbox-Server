package dev.aisandbox.server.simulation.maze;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class Maze {

    @Getter
    private final String boardID = UUID.randomUUID().toString();
    @Getter
    private final int width;
    @Getter
    private final int height;
    @Getter
    private final Cell[][] cellArray;
    @Getter
    private final List<Cell> cellList = new ArrayList<>();
    @Getter
    private final int zoomLevel;
    @Getter
    @Setter
    private Cell startCell = null;
    @Getter
    @Setter
    private Cell endCell = null;

    /**
     * Constructor for Maze.
     *
     * @param width  a int.
     * @param height a int.
     * @param zoomLevel the amount to zoom in to the output.
     */
    public Maze(int width, int height, int zoomLevel) {
        log.info("Generated maze {} with dimensions {}x{}", boardID, width, height);
        this.width = width;
        this.height = height;
        this.zoomLevel = zoomLevel;
        cellArray = new Cell[width][height];
        prepareGrid();
        joinGrid();
    }

    /**
     * prepareGrid.
     */
    protected void prepareGrid() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Cell c = new Cell(x, y);
                cellArray[x][y] = c;
                cellList.add(c);
            }
        }
    }

    /**
     * joinGrid.
     */
    protected void joinGrid() {
        for (Cell c : cellList) {
            if (c.getPositionY() > 0) {
                c.linkBi(Direction.NORTH, cellArray[c.getPositionX()][c.getPositionY() - 1]);
            }
            if (c.getPositionX() > 0) {
                c.linkBi(Direction.WEST, cellArray[c.getPositionX() - 1][c.getPositionY()]);
            }
        }
    }
}
