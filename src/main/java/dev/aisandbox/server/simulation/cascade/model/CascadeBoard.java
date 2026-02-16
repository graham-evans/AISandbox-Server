/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade.model;

import java.util.Random;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * The 8×8 game board for the Cascade match-3 simulation.
 *
 * <p>The board is indexed by column ({@code x}, 0 = left) and row ({@code y}, 0 = top). This
 * follows the same convention used throughout the engine where (0,0) is the top-left corner.
 *
 * <p>On construction the board is empty. Call {@link #initialise(Random)} to fill it with random
 * standard tiles such that no three-in-a-row matches exist in the starting position.
 *
 * <p>Game state tracked here:
 * <ul>
 *   <li>{@code movesRemaining} — decremented once per completed turn (default: 30).
 *   <li>{@code score} — accumulated points across all cascade waves in the current game.
 * </ul>
 */
@Slf4j
public class CascadeBoard {

  /** Number of columns on the board. */
  public static final int WIDTH = 8;

  /** Number of rows on the board. */
  public static final int HEIGHT = 8;

  /** Standard number of moves in a full game. */
  public static final int DEFAULT_MOVES = 30;

  /** The playable colours. Stored as a local reference to avoid repeated array allocation. */
  private static final TileColour[] COLOURS = TileColour.playableValues();

  /** The grid of cells. Indexed as {@code grid[x][y]} (column-major, origin top-left). */
  private final CascadeCell[][] grid;

  /** Moves remaining before the game ends. */
  @Getter
  private int movesRemaining;

  /** Accumulated score for the current game. */
  @Getter
  private long score;

  /**
   * Creates a new, empty board with the default move budget.
   *
   * <p>All cells are initialised to {@link TileType#EMPTY}. Call {@link #initialise(Random)} to
   * populate the board with a valid starting configuration.
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
   * this board. Because {@link CascadeCell} is an immutable record, the cells themselves do not
   * need to be copied — only the grid array and scalar fields are duplicated.
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
    for (int x = 0; x < WIDTH; x++) {
      System.arraycopy(this.grid[x], 0, copy.grid[x], 0, HEIGHT);
    }
    return copy;
  }

  /**
   * Fills the board with random standard tiles such that no three-or-more-in-a-row match exists.
   *
   * <p>The algorithm places a random colour in each cell, then replaces it with a different random
   * colour if it would create a horizontal or vertical match of three. In the worst case a cell may
   * require a few retries, but the five-colour palette guarantees that at least two valid colours
   * are always available (a cell can be blocked by at most one horizontal run and one vertical run,
   * each eliminating one colour, leaving three or more valid choices).
   *
   * @param random the source of randomness used for colour selection
   */
  public void initialise(Random random) {
    for (int x = 0; x < WIDTH; x++) {
      for (int y = 0; y < HEIGHT; y++) {
        grid[x][y] = CascadeCell.standard(pickNonMatchingColour(x, y, random));
      }
    }
    log.debug("Board initialised with no pre-existing matches");
  }

  /**
   * Chooses a random colour for position ({@code x}, {@code y}) that does not create a horizontal
   * or vertical match of three with the already-placed neighbours.
   *
   * <p>The method shuffles through the five colours in a random order and returns the first one
   * that is safe. Because there are always at most two colours that would form a match (one for
   * the horizontal direction, one for the vertical), a valid colour will always be found in at
   * most five attempts.
   *
   * @param x      column of the cell being placed
   * @param y      row of the cell being placed
   * @param random source of randomness
   * @return a {@link TileColour} that does not extend an existing run of two at ({@code x},{@code y})
   */
  private TileColour pickNonMatchingColour(int x, int y, Random random) {
    // Build a shuffled copy of the five colours
    TileColour[] candidates = COLOURS.clone();
    // Fisher-Yates shuffle
    for (int i = candidates.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      TileColour tmp = candidates[i];
      candidates[i] = candidates[j];
      candidates[j] = tmp;
    }
    for (TileColour colour : candidates) {
      if (!wouldMatchHorizontal(x, y, colour) && !wouldMatchVertical(x, y, colour)) {
        return colour;
      }
    }
    // Unreachable with five colours, but return the first as a safe fallback
    return candidates[0];
  }

  /**
   * Returns {@code true} if placing {@code colour} at ({@code x},{@code y}) would complete a
   * horizontal run of three with the two cells immediately to the left.
   *
   * <p>Only looks leftward because the board is filled left-to-right, so cells to the right have
   * not been placed yet.
   *
   * @param x      column index of the candidate cell
   * @param y      row index of the candidate cell
   * @param colour the colour being considered
   * @return {@code true} if a horizontal 3-in-a-row would result
   */
  private boolean wouldMatchHorizontal(int x, int y, TileColour colour) {
    return x >= 2
        && grid[x - 1][y].colour() == colour
        && grid[x - 2][y].colour() == colour;
  }

  /**
   * Returns {@code true} if placing {@code colour} at ({@code x},{@code y}) would complete a
   * vertical run of three with the two cells immediately above.
   *
   * <p>Only looks upward because the board is filled top-to-bottom, so cells below have not been
   * placed yet.
   *
   * @param x      column index of the candidate cell
   * @param y      row index of the candidate cell
   * @param colour the colour being considered
   * @return {@code true} if a vertical 3-in-a-row would result
   */
  private boolean wouldMatchVertical(int x, int y, TileColour colour) {
    return y >= 2
        && grid[x][y - 1].colour() == colour
        && grid[x][y - 2].colour() == colour;
  }
}