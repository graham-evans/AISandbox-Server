/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

import dev.aisandbox.server.simulation.cascade.model.CascadeBoard;
import dev.aisandbox.server.simulation.cascade.model.CascadeCell;
import dev.aisandbox.server.simulation.cascade.model.TileColour;
import dev.aisandbox.server.simulation.cascade.model.TileType;
import java.util.List;
import java.util.Random;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility methods for creating and manipulating {@link CascadeBoard} instances.
 */
@Slf4j
@UtilityClass
public class CascadeBoardUtils {

  /** The playable colours. Stored as a local reference to avoid repeated array allocation. */
  private static final TileColour[] COLOURS = TileColour.playableValues();

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
    initialise(board, random);
    return board;
  }

  /**
   * Fills every cell of {@code board} with a random standard tile such that no
   * three-or-more-in-a-row match exists in the resulting configuration.
   *
   * <p>The algorithm places a random colour in each cell left-to-right, top-to-bottom, then
   * replaces it with a different random colour if it would create a horizontal or vertical match of
   * three. The five-colour palette guarantees that at least two valid colours are always available
   * (a cell can be blocked by at most one horizontal run and one vertical run, each eliminating one
   * colour, leaving three or more valid choices).
   *
   * @param board  the board to populate (existing contents are overwritten)
   * @param random the source of randomness used for colour selection
   */
  private static void initialise(CascadeBoard board, Random random) {
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        if (!board.getCell(x, y).isOccupied()) {
          board.setCell(x, y, CascadeCell.standard(pickNonMatchingColour(board, x, y, random)));
        }
      }
    }
    log.debug("Board initialised with no pre-existing matches");
  }

  /**
   * Chooses a random colour for position ({@code x},{@code y}) that does not create a horizontal
   * or vertical match of three with the already-placed neighbours.
   *
   * <p>The method shuffles through the five colours in a random order and returns the first one
   * that is safe. Because there are always at most two colours that would form a match (one for
   * the horizontal direction, one for the vertical), a valid colour will always be found in at
   * most five attempts.
   *
   * @param board  the board being populated (cells to the left and above are already placed)
   * @param x      column of the cell being placed
   * @param y      row of the cell being placed
   * @param random source of randomness
   * @return a {@link TileColour} that does not extend an existing run of two at ({@code x},{@code y})
   */
  private static TileColour pickNonMatchingColour(CascadeBoard board, int x, int y, Random random) {
    TileColour[] candidates = COLOURS.clone();
    // Fisher-Yates shuffle
    for (int i = candidates.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      TileColour tmp = candidates[i];
      candidates[i] = candidates[j];
      candidates[j] = tmp;
    }
    for (TileColour colour : candidates) {
      if (!wouldMatchHorizontal(board, x, y, colour) && !wouldMatchVertical(board, x, y, colour)) {
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
   */
  private static boolean wouldMatchHorizontal(CascadeBoard board, int x, int y, TileColour colour) {
    return x >= 2
        && board.getCell(x - 1, y).getColour() == colour
        && board.getCell(x - 2, y).getColour() == colour;
  }

  /**
   * Returns {@code true} if placing {@code colour} at ({@code x},{@code y}) would complete a
   * vertical run of three with the two cells immediately above.
   *
   * <p>Only looks upward because the board is filled top-to-bottom, so cells below have not been
   * placed yet.
   */
  private static boolean wouldMatchVertical(CascadeBoard board, int x, int y, TileColour colour) {
    return y >= 2
        && board.getCell(x, y - 1).getColour() == colour
        && board.getCell(x, y - 2).getColour() == colour;
  }

  /**
   * Returns {@code true} if the board contains no matches of three or more matchable tiles of the
   * same colour in any row or column.
   *
   * <p>Only cells that return {@code true} from {@link CascadeCell#isMatchable()} are considered.
   * Prisms, Ice Blocks, Stones, and empty cells break a run without contributing to it.
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
        if (cell.isMatchable() && cell.getColour() == runColour) {
          runLength++;
          if (runLength >= 3) {
            return false;
          }
        } else {
          runLength = 1;
          runColour = cell.isMatchable() ? cell.getColour() : TileColour.NONE;
        }
      }
    }
    // Check vertical runs
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      int runLength = 1;
      TileColour runColour = TileColour.NONE;
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        var cell = board.getCell(x, y);
        if (cell.isMatchable() && cell.getColour() == runColour) {
          runLength++;
          if (runLength >= 3) {
            return false;
          }
        } else {
          runLength = 1;
          runColour = cell.isMatchable() ? cell.getColour() : TileColour.NONE;
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
   *       matchable tiles of the same colour.</li>
   * </ul>
   *
   * @param board the board to inspect
   * @return {@code true} if at least one valid move exists
   */
  public static boolean isValid(CascadeBoard board) {
    // A special object on the board always constitutes a playable move
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        TileType type = board.getCell(x, y).getType();
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
    TileType type = board.getCell(x, y).getType();
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
    TileColour colour = cell.getColour();
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
        && board.getCell(nx, ny).getColour() == colour) {
      count++;
      nx += dx;
      ny += dy;
    }
    return count;
  }

  /**
   * Builds a {@link CascadeBoard} from a list of eight row strings, suitable for constructing
   * specific board states in unit tests.
   *
   * <p>Each string represents one row (y = 0 at the top), and must contain exactly eight
   * whitespace-separated cell tokens. The token format is:
   *
   * <pre>
   *   .          empty cell
   *   r/b/g/y/p  standard tile  (red / blue / green / yellow / purple)
   *   Br/Bb/…    bomb of that colour
   *   Hr/Hb/…    horizontal rocket of that colour
   *   Vr/Vb/…    vertical rocket of that colour
   *   X          prism  (no colour)
   *   Ir/Ib/…    ice block encasing a tile of that colour
   *   #          stone  (no colour)
   * </pre>
   *
   * <p>Append {@code !} to any bomb or rocket token to mark it as already activated, e.g.
   * {@code Br!} is an activated red bomb. The {@code !} suffix is silently ignored on tile types
   * that do not use the activated flag.
   *
   * @param rows exactly {@value CascadeBoard#HEIGHT} row strings
   * @return a populated {@link CascadeBoard} reflecting the described layout
   * @throws IllegalArgumentException if the row count, token count, or any token is invalid
   */
  public static CascadeBoard boardFromStrings(List<String> rows) {
    if (rows.size() != CascadeBoard.HEIGHT) {
      throw new IllegalArgumentException(
          "Expected " + CascadeBoard.HEIGHT + " rows, got " + rows.size());
    }
    CascadeBoard board = new CascadeBoard();
    for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
      String[] tokens = rows.get(y).trim().split("\\s+");
      if (tokens.length != CascadeBoard.WIDTH) {
        throw new IllegalArgumentException(
            "Row " + y + ": expected " + CascadeBoard.WIDTH + " tokens, got " + tokens.length);
      }
      for (int x = 0; x < CascadeBoard.WIDTH; x++) {
        board.setCell(x, y, parseCell(tokens[x]));
      }
    }
    return board;
  }

  /**
   * Parses a single cell token into a {@link CascadeCell}.
   *
   * @param token the token string (see {@link #boardFromStrings} for the format)
   * @return the corresponding cell
   * @throws IllegalArgumentException if the token is not recognised
   */
  private static CascadeCell parseCell(String token) {
    boolean activated = token.endsWith("!");
    String t = activated ? token.substring(0, token.length() - 1) : token;

    CascadeCell cell = switch (t) {
      case "." -> CascadeCell.empty();
      case "r" -> CascadeCell.standard(TileColour.RED);
      case "b" -> CascadeCell.standard(TileColour.BLUE);
      case "g" -> CascadeCell.standard(TileColour.GREEN);
      case "y" -> CascadeCell.standard(TileColour.YELLOW);
      case "p" -> CascadeCell.standard(TileColour.PURPLE);
      case "X" -> CascadeCell.prism();
      case "#" -> CascadeCell.stone();
      default -> parseTwoCharCell(t, token);
    };

    if (activated) {
      cell.activate();
    }
    return cell;
  }

  /**
   * Parses a two-character cell token (type letter + colour letter) into a {@link CascadeCell}.
   *
   * @param t     the token with any trailing {@code !} already stripped
   * @param token the original token (used in error messages)
   * @return the corresponding cell
   * @throws IllegalArgumentException if the token is not a recognised two-character cell code
   */
  private static CascadeCell parseTwoCharCell(String t, String token) {
    if (t.length() != 2) {
      throw new IllegalArgumentException("Unrecognised cell token: '" + token + "'");
    }
    TileColour colour = switch (t.charAt(1)) {
      case 'r' -> TileColour.RED;
      case 'b' -> TileColour.BLUE;
      case 'g' -> TileColour.GREEN;
      case 'y' -> TileColour.YELLOW;
      case 'p' -> TileColour.PURPLE;
      default -> throw new IllegalArgumentException(
          "Unknown colour '" + t.charAt(1) + "' in token: '" + token + "'");
    };
    return switch (t.charAt(0)) {
      case 'B' -> CascadeCell.bomb(colour);
      case 'H' -> CascadeCell.rocketH(colour);
      case 'V' -> CascadeCell.rocketV(colour);
      case 'I' -> CascadeCell.ice(colour);
      default -> throw new IllegalArgumentException(
          "Unknown tile type '" + t.charAt(0) + "' in token: '" + token + "'");
    };
  }
}
