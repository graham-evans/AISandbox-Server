/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents the game board for the Mine Hunter simulation.
 * <p>
 * This class manages the state of a minesweeper-style game board, including mine placement,
 * cell uncovering, flag placement, and game state tracking. The board consists of a 2D grid
 * of cells, each of which may contain a mine and tracks its covered/uncovered state.
 * </p>
 * <p>
 * Game mechanics handled by this class:
 * </p>
 * <ul>
 *   <li>Random mine placement during board initialization</li>
 *   <li>Neighbor counting for each cell (number of adjacent mines)</li>
 *   <li>Cell uncovering with automatic flood-fill for empty areas</li>
 *   <li>Flag placement and validation</li>
 *   <li>Win/loss state detection</li>
 * </ul>
 * <p>
 * The board maintains game state through the {@link GameState} enum, progressing from
 * INIT → PLAYING → (WON or LOST) as the game proceeds.
 * </p>
 *
 * @see Cell
 * @see GameState
 */
@Slf4j
public class Board {

  @Getter
  private final int width;
  @Getter
  private final int height;

  private final Cell[][] grid;
  @Getter
  private final String boardID = UUID.randomUUID().toString();
  @Getter
  private GameState state = GameState.INIT;
  @Getter
  private int unfoundMines = 0;

  /**
   * Creates a new mine board with the specified dimensions.
   * <p>
   * Initializes an empty board grid where all cells start uncovered and without mines.
   * After construction, mines should be placed using {@link #placeMines(Random, int)}
   * and neighbor counts calculated using {@link #countNeighbours()}.
   * </p>
   *
   * @param width  the width of the board in cells (must be positive)
   * @param height the height of the board in cells (must be positive)
   */
  public Board(int width, int height) {
    this.width = width;
    this.height = height;
    grid = new Cell[width][height];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        grid[x][y] = new Cell();
      }
    }
  }

  /**
   * Randomly places the specified number of mines on the board.
   * <p>
   * Mines are placed randomly across the board using the provided random number generator.
   * If the requested count exceeds the total number of cells, only as many mines as there
   * are cells will be placed (one per cell maximum).
   * </p>
   * <p>
   * Each successfully placed mine increments the {@code unfoundMines} counter, which is
   * used to track game completion.
   * </p>
   *
   * @param rand  random number generator for mine placement
   * @param count the desired number of mines to place (will be capped at total cell count)
   */
  public void placeMines(Random rand, int count) {
    // check we dont have more mines than cells
    count = Math.min(count, width * height);
    // place the mines randomly
    while (count > 0) {
      Cell c = grid[rand.nextInt(width)][rand.nextInt(height)];
      if (!c.isMine()) {
        c.setMine(true);
        count--;
        unfoundMines++;
      }
    }
  }

  /**
   * Returns the cell at the specified coordinates.
   * <p>
   * This method provides package-private access to individual cells for use by other
   * classes in the simulation package. No bounds checking is performed.
   * </p>
   *
   * @param x the x-coordinate (0-based, from left)
   * @param y the y-coordinate (0-based, from top)
   * @return the Cell object at the specified position
   * @throws ArrayIndexOutOfBoundsException if coordinates are outside the board bounds
   */
  protected Cell getCell(int x, int y) {
    return grid[x][y];
  }

  /**
   * Converts the current board state to a string array representation.
   * <p>
   * Each element in the returned array represents one row of the board from top to bottom.
   * This representation shows the board from the player's perspective, with covered cells,
   * flags, uncovered numbers, and mines displayed according to the current game state.
   * </p>
   * <p>
   * The string format uses the character encoding defined by {@link Cell#getPlayerView()}.
   * </p>
   *
   * @return an array of strings, one per board row, representing the current board state
   * @see Cell#getPlayerView()
   */
  public String[] getBoardToString() {
    String[] result = new String[grid[0].length];
    for (int y = 0; y < height; y++) {
      result[y] = getRowToString(y);
    }
    return result;
  }

  /**
   * Converts a single row of the board to its string representation.
   * <p>
   * This method generates the player's view of a specific row, with each cell
   * represented by a single character according to its current state.
   * </p>
   *
   * @param y the row number (0-based, from top)
   * @return a string representing the specified row's current state
   * @throws ArrayIndexOutOfBoundsException if the row number is invalid
   * @see Cell#getPlayerView()
   */
  public String getRowToString(int y) {
    StringBuilder sb = new StringBuilder();
    for (int x = 0; x < width; x++) {
      sb.append(grid[x][y].getPlayerView());
    }
    return sb.toString();
  }

  /**
   * Calculates and stores the neighbor mine count for all cells on the board.
   * <p>
   * This method must be called after mine placement is complete and before the game begins.
   * It iterates through all cells and counts the number of adjacent mines for each cell,
   * storing the result in the cell's {@code neighbours} field.
   * </p>
   * <p>
   * After completion, the game state is set to {@link GameState#PLAYING}, indicating
   * the board is ready for player interaction.
   * </p>
   * 
   * @see #countNeighbours(int, int)
   */
  public void countNeighbours() {
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        grid[x][y].setNeighbours(countNeighbours(x, y));
      }
    }
    state = GameState.PLAYING;
  }

  /**
   * Counts the number of mines adjacent to the specified cell position.
   * <p>
   * This method examines all eight adjacent cells (horizontally, vertically, and diagonally)
   * and counts how many contain mines. Cells outside the board boundaries are ignored.
   * </p>
   * <p>
   * The count will be:
   * </p>
   * <ul>
   *   <li>0-8 for interior cells (surrounded by 8 neighbors)</li>
   *   <li>0-5 for edge cells (5 valid neighbors)</li>
   *   <li>0-3 for corner cells (3 valid neighbors)</li>
   * </ul>
   *
   * @param x the x-coordinate of the cell
   * @param y the y-coordinate of the cell
   * @return the number of adjacent cells containing mines
   */
  private int countNeighbours(int x, int y) {
    int count = 0;
    for (int dx = -1; dx < 2; dx++) {
      for (int dy = -1; dy < 2; dy++) {
        if ((dx != 0) || (dy != 0)) {
          count += getMineBounds(x + dx, y + dy);
        }
      }
    }
    return count;
  }

  /**
   * Checks if the cell at the specified coordinates contains a mine.
   * <p>
   * This is a bounds-safe helper method that returns 1 if the specified position
   * contains a mine and is within the board boundaries, or 0 otherwise. This method
   * is used internally by {@link #countNeighbours(int, int)} to safely check
   * neighboring cells without worrying about array bounds.
   * </p>
   *
   * @param x the x-coordinate to check
   * @param y the y-coordinate to check
   * @return 1 if the position is valid and contains a mine, 0 otherwise
   */
  private int getMineBounds(int x, int y) {
    try {
      return grid[x][y].isMine() ? 1 : 0;
    } catch (IndexOutOfBoundsException e) {
      return 0;
    }
  }

  /**
   * Places a flag at the specified position on the board.
   * <p>
   * Flag placement follows these rules:
   * </p>
   * <ul>
   *   <li>Flagging an already flagged cell has no effect</li>
   *   <li>Flagging an uncovered cell has no effect</li>
   *   <li>Correctly flagging a mine decrements the unfound mines counter</li>
   *   <li>Incorrectly flagging a non-mine cell triggers game loss</li>
   *   <li>Finding all mines by correct flagging triggers game win</li>
   * </ul>
   * <p>
   * This method handles both win and loss condition detection automatically.
   * </p>
   *
   * @param x the x-coordinate where to place the flag
   * @param y the y-coordinate where to place the flag
   * @return true if the board state changed as a result of this action, false otherwise
   */
  public boolean placeFlag(int x, int y) {
    log.info("Placing flag @ {},{}", x, y);
    Cell c = grid[x][y];
    boolean change = false;
    if (c.isFlagged()) {
      log.info("Flagging an already flagged cell - ignoring");
    } else if (!c.isCovered()) {
      log.info("Flagging an uncovered tile - ignoring");
    } else if (c.isMine()) {
      log.info("Correctly found a mine");
      c.setFlagged(true);
      unfoundMines--;
      change = true;
    } else {
      log.info("Incorrectly marked a mine");
      c.setFlagged(true);
      state = GameState.LOST;
      change = true;
    }
    if (unfoundMines == 0) {
      state = GameState.WON;
    }
    return change;
  }

  /**
   * Uncovers the cell at the specified position.
   * <p>
   * Cell uncovering behavior:
   * </p>
   * <ul>
   *   <li>Attempting to uncover a flagged or already uncovered cell has no effect</li>
   *   <li>Uncovering a mine triggers immediate game loss</li>
   *   <li>Uncovering a cell with no adjacent mines triggers flood-fill uncovering</li>
   *   <li>Uncovering a cell with adjacent mines simply reveals the count</li>
   * </ul>
   * <p>
   * The flood-fill behavior automatically uncovers connected areas of cells that have
   * no adjacent mines, providing the classic minesweeper experience where large areas
   * can be revealed with a single click.
   * </p>
   *
   * @param x the x-coordinate of the cell to uncover
   * @param y the y-coordinate of the cell to uncover
   * @return true if the board state changed as a result of this action, false otherwise
   * @see #floodFill(int, int)
   */
  public boolean uncover(int x, int y) {
    Cell c = grid[x][y];
    boolean change = false;
    if (c.isFlagged() || (!c.isCovered())) {
      log.warn("trying to uncover an used cell - ignoring");
    } else {
      c.setCovered(false);
      change = true;
      if (c.isMine()) {
        log.info("Bad move");
        state = GameState.LOST;
      } else if (c.getNeighbours() == 0) {
        floodFill(x, y);
      }
    }
    return change;
  }

  /**
   * Performs flood-fill uncovering starting from the specified position.
   * <p>
   * This method implements the classic minesweeper flood-fill algorithm that automatically
   * uncovers connected areas of cells with no adjacent mines. It uses a breadth-first
   * search approach with a stack to iteratively process cells.
   * </p>
   * <p>
   * The algorithm:
   * </p>
   * <ol>
   *   <li>Starts with the initial position on the stack</li>
   *   <li>Processes each cell by uncovering it and marking as visited</li>
   *   <li>If the cell has no adjacent mines, adds all unvisited neighbors to the stack</li>
   *   <li>Continues until no more cells can be processed</li>
   * </ol>
   * <p>
   * This creates the satisfying gameplay mechanic where uncovering one empty cell
   * can reveal large connected areas of the board.
   * </p>
   *
   * @param x the x-coordinate to start the flood-fill from
   * @param y the y-coordinate to start the flood-fill from
   */
  private void floodFill(int x, int y) {
    Set<CellLocation> visited = new HashSet<>();
    List<CellLocation> stack = new ArrayList<>();
    stack.add(new CellLocation(x, y));
    while (!stack.isEmpty()) {
      // take the next sell from the stack
      CellLocation currentCellLocation = stack.remove(0);
      Cell currentCell = grid[currentCellLocation.x()][currentCellLocation.y()];
      // add it to the visited pile
      visited.add(currentCellLocation);
      // uncover it
      currentCell.setCovered(false);
      // look at neighbours
      if (currentCell.getNeighbours() == 0) {
        // place neighbours on the stack
        List<CellLocation> neighbours = getNeighbours(currentCellLocation);
        for (CellLocation c : neighbours) {
          if ((!visited.contains(c)) && (!stack.contains(c))) {
            stack.add(c);
          }
        }
      }
    }
  }

  /**
   * Returns a list of all valid neighboring positions for the given cell location.
   * <p>
   * This method generates coordinates for all cells adjacent to the specified position,
   * including diagonal neighbors, while ensuring all returned coordinates are within
   * the board boundaries. Used by the flood-fill algorithm to find cells to process.
   * </p>
   *
   * @param c the center cell location to find neighbors for
   * @return a list of CellLocation objects representing all valid adjacent positions
   */
  private List<CellLocation> getNeighbours(CellLocation c) {
    List<CellLocation> result = new ArrayList<>();
    for (int dx = -1; dx < 2; dx++) {
      int x = c.x() + dx;
      if ((x >= 0) && (x < width)) {
        for (int dy = -1; dy < 2; dy++) {
          int y = c.y() + dy;
          if ((y >= 0) && (y < height)) {
            result.add(new CellLocation(x, y));
          }
        }
      }
    }
    return result;
  }

}
