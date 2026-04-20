/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade.model;

import lombok.Getter;
import lombok.Setter;

/**
 * The 8×8 game board for the Cascade match-3 simulation.
 *
 * <p>The board is indexed by column ({@code x}, 0 = left) and row ({@code y}, 0 = top). This
 * follows the same convention used throughout the engine where (0,0) is the top-left corner.
 *
 * <p>On construction the board is empty. Use
 * {@link dev.aisandbox.server.simulation.cascade.CascadeBoardUtils#randomBoard(java.util.Random)}
 * to obtain a fully populated board ready for play.
 *
 * <p>Game state tracked here:
 * <ul>
 *   <li>{@code movesRemaining} — decremented once per completed turn (default: 30).
 *   <li>{@code score} — accumulated points across all cascade waves in the current game.
 * </ul>
 */
public class CascadeBoard {

  /** Number of columns on the board. */
  public static final int WIDTH = 8;

  /** Number of rows on the board. */
  public static final int HEIGHT = 8;

  /** Standard number of moves in a full game. */
  public static final int DEFAULT_MOVES = 30;

  /** The grid of cells. Indexed as {@code grid[x][y]} (column-major, origin top-left). */
  private final CascadeCell[][] grid;

  /** Moves remaining before the game ends. */
  @Getter
  private int movesRemaining;

  /** Accumulated score for the current game. */
  @Getter
  @Setter
  private long score;

  @Getter
  @Setter
  private long multiplier = 1;

  public static final long TILE_SCORE = 10;

  /**
   * Creates a new, empty board with the default move budget.
   *
   * <p>All cells are initialised to {@link TileType#EMPTY}. Use
   * {@link dev.aisandbox.server.simulation.cascade.CascadeBoardUtils#randomBoard(java.util.Random)}
   * to populate the board with a valid starting configuration.
   */
  public CascadeBoard() {
    grid = new CascadeCell[WIDTH][HEIGHT];
    for (int x = 0; x < WIDTH; x++) {
      for (int y = 0; y < HEIGHT; y++) {
        grid[x][y] = CascadeCell.empty();
      }
    }
    movesRemaining = DEFAULT_MOVES;
    score = 0;
  }

  /**
   * Returns the cell at the specified board position.
   *
   * @param x column index (0 = left, {@value #WIDTH}-1 = right)
   * @param y row index (0 = top, {@value #HEIGHT}-1 = bottom)
   * @return the {@link CascadeCell} at that position
   * @throws ArrayIndexOutOfBoundsException if the coordinates are outside the board
   */
  public CascadeCell getCell(int x, int y) {
    return grid[x][y];
  }

  /**
   * Replaces the cell at the specified position.
   *
   * <p>This is the primary mutation point used by the match-resolution and refill logic.
   *
   * @param x    column index
   * @param y    row index
   * @param cell the new cell value (must not be {@code null})
   */
  public void setCell(int x, int y, CascadeCell cell) {
    grid[x][y] = cell;
  }

  /**
   * Swaps the cells at two positions on the board.
   *
   * <p>No validity check is performed here — callers are responsible for ensuring the swap is
   * legal before calling this method.
   *
   * @param x1 column of the first cell
   * @param y1 row of the first cell
   * @param x2 column of the second cell
   * @param y2 row of the second cell
   */
  public void swap(int x1, int y1, int x2, int y2) {
    CascadeCell tmp = grid[x1][y1];
    grid[x1][y1] = grid[x2][y2];
    grid[x2][y2] = tmp;
  }

  /**
   * Returns {@code true} if the given coordinates lie within the board boundaries.
   *
   * @param x column index
   * @param y row index
   * @return {@code true} when both coordinates are in range
   */
  public boolean inBounds(int x, int y) {
    return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
  }

  /**
   * Adds points to the accumulated game score.
   *
   * @param points the number of points to add (should be non-negative)
   */
  public void addScore(long points) {
    score += points;
  }

  /**
   * Decrements the move counter by one.
   *
   * <p>Called once after the board has fully stabilised following a player's swap action
   * (i.e., after all cascades, gravity, and refill steps are complete).
   */
  public void consumeMove() {
    if (movesRemaining > 0) {
      movesRemaining--;
    }
  }

  /**
   * Returns {@code true} when the game is over (no moves remaining).
   *
   * @return {@code true} if {@code movesRemaining == 0}
   */
  public boolean isGameOver() {
    return movesRemaining == 0;
  }

  /**
   * Creates and returns a deep copy of this board.
   *
   * <p>The returned board has the same {@code movesRemaining}, {@code score}, and cell grid as
   * this board. Each {@link CascadeCell} is individually copied via {@link CascadeCell#copy()} so
   * that mutations to either board's cells do not affect the other.
   *
   * <p>Mutating the returned board (swapping cells, calling {@link #consumeMove()}, etc.) will
   * not affect this board, and vice versa.
   *
   * @return a new {@link CascadeBoard} that is an independent copy of this one
   */
  public CascadeBoard copy() {
    CascadeBoard copy = new CascadeBoard();
    copy.movesRemaining = this.movesRemaining;
    copy.score = this.score;
    copy.multiplier = this.multiplier;
    for (int x = 0; x < WIDTH; x++) {
      for (int y = 0; y < HEIGHT; y++) {
        copy.grid[x][y] = this.grid[x][y].copy();
      }
    }
    return copy;
  }
}
