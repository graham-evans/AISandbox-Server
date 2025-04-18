package dev.aisandbox.server.simulation.maze;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a maze with its cells, dimensions, and zoom level.
 */
@Slf4j
public final class Maze {

  /**
   * Unique identifier for the board (maze).
   */
  @Getter
  private final String boardID = UUID.randomUUID().toString();
  /**
   * Width of the maze in number of cells.
   */
  @Getter
  private final int width;

  /**
   * Height of the maze in number of cells.
   */
  @Getter
  private final int height;

  /**
   * Two-dimensional array representing the cells of the maze.
   */
  @Getter
  private final Cell[][] cellArray;

  /**
   * List of all cells in the maze for efficient iteration and access.
   */
  @Getter
  private final List<Cell> cellList = new ArrayList<>();

  /**
   * Zoom level to display the maze at a certain scale.
   */
  @Getter
  private final int zoomLevel;

  /**
   * Starting point (cell) of the maze, initially set to null.
   */
  @Getter
  @Setter
  private Cell startCell = null;

  /**
   * Ending point (cell) of the maze, initially set to null.
   */
  @Getter
  @Setter
  private Cell endCell = null;

  /**
   * Constructor for creating a new Maze instance with specified dimensions and zoom level.
   *
   * @param width     Number of cells in the width dimension.
   * @param height    Number of cells in the height dimension.
   * @param zoomLevel The amount to zoom in to the output (display).
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
   * Prepares the 2D grid of cells for further processing and linking.
   */
  protected void prepareGrid() {
    // Iterate over all positions in the grid to create individual cell objects
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        Cell c = new Cell(x, y);
        cellArray[x][y] = c;
        // Store each cell in a list for efficient iteration and access
        cellList.add(c);
      }
    }
  }

  /**
   * Links adjacent cells together to form the maze structure.
   */
  protected void joinGrid() {
    // Iterate over all cells, linking neighboring cells by their positions
    for (Cell c : cellList) {
      if (c.getPositionY() > 0) { // Link northward
        c.linkBi(Direction.NORTH, cellArray[c.getPositionX()][c.getPositionY() - 1]);
      }
      if (c.getPositionX() > 0) { // Link westward
        c.linkBi(Direction.WEST, cellArray[c.getPositionX() - 1][c.getPositionY()]);
      }
    }
  }
}
