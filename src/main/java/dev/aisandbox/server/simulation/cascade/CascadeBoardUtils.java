/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

import dev.aisandbox.server.simulation.cascade.model.CascadeBoard;
import dev.aisandbox.server.simulation.cascade.model.TileColour;
import dev.aisandbox.server.simulation.cascade.model.TileType;
import java.util.Random;
import lombok.experimental.UtilityClass;

/**
 * Utility methods for creating and manipulating {@link CascadeBoard} instances.
 */
@UtilityClass
public class CascadeBoardUtils {

  /**
   * Creates a new 8×8 board filled with random standard coloured tiles.
   *
   * <p>The board is guaranteed to contain no pre-existing matches of three or more in a row or
   * column. All cells are standard tiles drawn from the five playable colours (Red, Blue, Green,
   * Yellow, Purple).
   *
   * @param random the source of randomness used for colour selection
   * @return a fully populated {@link CascadeBoard} ready for play
   */
  public static CascadeBoard randomBoard(Random random) {
    CascadeBoard board = new CascadeBoard();
    board.initialise(random);
    return board;
  }

  /**
   * Returns {@code true} if the board contains no matches of three or more standard tiles of the
   * same colour in any row or column.
   *
   * <p>Only cells that return {@code true} from {@link
   * dev.aisandbox.server.simulation.cascade.model.CascadeCell#isMatchable()} (i.e.
   * {@link dev.aisandbox.server.simulation.cascade.model.TileType#STANDARD} tiles) are considered.
   * Special objects, ice blocks, stones, and empty cells break a run without contributing to it.
   *
   * @param board the board to inspect
   * @return {@code true} if the board is stable (no pending matches)
   */
  public static boolean isStable(CascadeBoard board) {
    // Check horizontal runs
    for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
      int runLength = 1;
      TileColour runColour = TileColour.NONE;
      for (int x = 0; x < CascadeBoard.WIDTH; x++) {
        var cell = board.getCell(x, y);
        if (cell.isMatchable() && cell.colour() == runColour) {
          runLength++;
          if (runLength >= 3) {
            return false;
          }
        } else {
          runLength = 1;
          runColour = cell.isMatchable() ? cell.colour() : TileColour.NONE;
        }
      }
    }
    // Check vertical runs
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      int runLength = 1;
      TileColour runColour = TileColour.NONE;
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        var cell = board.getCell(x, y);
        if (cell.isMatchable() && cell.colour() == runColour) {
          runLength++;
          if (runLength >= 3) {
            return false;
          }
        } else {
          runLength = 1;
          runColour = cell.isMatchable() ? cell.colour() : TileColour.NONE;
        }
      }
    }
    return true;
  }

  /**
   * Returns {@code true} if the board contains at least one valid move.
   *
   * <p>A board is valid (has moves available) when any of the following is true:
   * <ul>
   *   <li>A {@link TileType#BOMB}, {@link TileType#ROCKET_H}, {@link TileType#ROCKET_V}, or
   *       {@link TileType#PRISM} is present — these can always be triggered.</li>
   *   <li>Swapping any two adjacent (horizontally or vertically neighbouring) non-{@link
   *       TileType#ICE}, non-{@link TileType#STONE} tiles would create a run of three or more
   *       standard tiles of the same colour.</li>
   * </ul>
   *
   * @param board the board to inspect
   * @return {@code true} if at least one valid move exists
   */
  public static boolean isValid(CascadeBoard board) {
    // A special object on the board always constitutes a playable move
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        TileType type = board.getCell(x, y).type();
        if (type == TileType.BOMB || type == TileType.ROCKET_H
            || type == TileType.ROCKET_V || type == TileType.PRISM) {
          return true;
        }
      }
    }
    // Check every horizontal adjacent pair
    for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
      for (int x = 0; x < CascadeBoard.WIDTH - 1; x++) {
        if (isSwappable(board, x, y) && isSwappable(board, x + 1, y)
            && swapCreatesMatch(board, x, y, x + 1, y)) {
          return true;
        }
      }
    }
    // Check every vertical adjacent pair
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT - 1; y++) {
        if (isSwappable(board, x, y) && isSwappable(board, x, y + 1)
            && swapCreatesMatch(board, x, y, x, y + 1)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns {@code true} if the cell at ({@code x},{@code y}) may participate in a swap —
   * i.e. it is neither a {@link TileType#ICE} block nor a {@link TileType#STONE}.
   */
  private static boolean isSwappable(CascadeBoard board, int x, int y) {
    TileType type = board.getCell(x, y).type();
    return type != TileType.ICE && type != TileType.STONE;
  }

  /**
   * Returns {@code true} if swapping the two cells would create a run of three or more matchable
   * tiles at either swapped position.
   *
   * <p>The swap is performed on the board, the check is made, then the swap is immediately
   * reversed, leaving the board unchanged on return.
   */
  private static boolean swapCreatesMatch(CascadeBoard board, int x1, int y1, int x2, int y2) {
    board.swap(x1, y1, x2, y2);
    boolean match = hasMatchAt(board, x1, y1) || hasMatchAt(board, x2, y2);
    board.swap(x1, y1, x2, y2);
    return match;
  }

  /**
   * Returns {@code true} if the cell at ({@code x},{@code y}) is part of a horizontal or vertical
   * run of three or more matchable tiles of the same colour.
   */
  private static boolean hasMatchAt(CascadeBoard board, int x, int y) {
    var cell = board.getCell(x, y);
    if (!cell.isMatchable()) {
      return false;
    }
    TileColour colour = cell.colour();
    int horizontal = 1
        + countRun(board, x, y, -1, 0, colour)
        + countRun(board, x, y, 1, 0, colour);
    int vertical = 1
        + countRun(board, x, y, 0, -1, colour)
        + countRun(board, x, y, 0, 1, colour);
    return horizontal >= 3 || vertical >= 3;
  }

  /**
   * Counts how many consecutive matchable cells of {@code colour} lie in the direction
   * ({@code dx},{@code dy}) starting from (but not including) ({@code x},{@code y}).
   */
  private static int countRun(CascadeBoard board, int x, int y, int dx, int dy,
      TileColour colour) {
    int count = 0;
    int nx = x + dx;
    int ny = y + dy;
    while (board.inBounds(nx, ny)
        && board.getCell(nx, ny).isMatchable()
        && board.getCell(nx, ny).colour() == colour) {
      count++;
      nx += dx;
      ny += dy;
    }
    return count;
  }
}
