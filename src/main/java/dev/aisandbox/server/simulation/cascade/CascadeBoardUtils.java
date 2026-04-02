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
import java.util.ArrayList;
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
   *   <li>A {@link TileType#PRISM} is present and has at least one adjacent non-{@link
   *       TileType#ICE}, non-{@link TileType#STONE} neighbour — a prism can be swapped with any
   *       such tile to activate it.</li>
   *   <li>Swapping any two adjacent (horizontally or vertically neighbouring) non-{@link
   *       TileType#ICE}, non-{@link TileType#STONE} tiles would create a run of three or more
   *       matchable tiles of the same colour. This includes swaps involving
   *       {@link TileType#BOMB} and {@link TileType#ROCKET_H}/{@link TileType#ROCKET_V} tiles,
   *       which are matchable and participate in colour runs.</li>
   * </ul>
   *
   * @param board the board to inspect
   * @return {@code true} if at least one valid move exists
   */
  public static boolean isValid(CascadeBoard board) {
    // A prism with at least one swappable neighbour constitutes a valid move
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        if (board.getCell(x, y).getType() == TileType.PRISM
            && hasSwappableNeighbour(board, x, y)) {
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
   * Returns {@code true} if at least one of the four orthogonal neighbours of ({@code x},{@code y})
   * is within bounds and swappable.
   */
  private static boolean hasSwappableNeighbour(CascadeBoard board, int x, int y) {
    return (board.inBounds(x - 1, y) && isSwappable(board, x - 1, y))
        || (board.inBounds(x + 1, y) && isSwappable(board, x + 1, y))
        || (board.inBounds(x, y - 1) && isSwappable(board, x, y - 1))
        || (board.inBounds(x, y + 1) && isSwappable(board, x, y + 1));
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

  // ---------------------------------------------------------------------------
  // Serialisation
  // ---------------------------------------------------------------------------

  /**
   * Serialises the board to a list of eight strings (y=0 to y=7).
   *
   * <p>Each string contains eight space-separated two-character cell tokens (x=0 to x=7).
   * The first character encodes the tile colour and the second the tile type. Both characters are
   * uppercased when the cell's {@code activated} flag is set. The three special fixed tokens are:
   * <ul>
   *   <li>{@code ..} — empty cell</li>
   *   <li>{@code ##} — stone</li>
   *   <li>{@code xx} / {@code XX} — prism (inactive / active)</li>
   * </ul>
   *
   * @param board the board to serialise
   * @return a list of eight row strings
   */
  public static List<String> serialiseBoard(CascadeBoard board) {
    List<String> rows = new ArrayList<>(CascadeBoard.HEIGHT);
    for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
      StringBuilder sb = new StringBuilder();
      for (int x = 0; x < CascadeBoard.WIDTH; x++) {
        if (x > 0) {
          sb.append(' ');
        }
        sb.append(serialiseCell(board.getCell(x, y)));
      }
      rows.add(sb.toString());
    }
    return rows;
  }

  /**
   * Updates every cell of {@code board} by parsing the given serialised row strings.
   *
   * <p>The {@code score} and {@code movesRemaining} fields of the board are not modified.
   *
   * @param board the board whose cells will be replaced
   * @param rows  exactly {@value CascadeBoard#HEIGHT} strings, each containing exactly
   *              {@value CascadeBoard#WIDTH} space-separated two-character tokens
   * @throws IllegalArgumentException if the row count is wrong, a row contains the wrong number
   *         of tokens, or a token is unrecognised or malformed
   */
  public static void deserialiseBoard(CascadeBoard board, List<String> rows) {
    if (rows.size() != CascadeBoard.HEIGHT) {
      throw new IllegalArgumentException(
          "Expected " + CascadeBoard.HEIGHT + " rows, got " + rows.size());
    }
    for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
      String[] tokens = rows.get(y).split(" ");
      if (tokens.length != CascadeBoard.WIDTH) {
        throw new IllegalArgumentException(
            "Row " + y + ": expected " + CascadeBoard.WIDTH + " tokens, got " + tokens.length);
      }
      for (int x = 0; x < CascadeBoard.WIDTH; x++) {
        board.setCell(x, y, deserialiseToken(tokens[x], x, y));
      }
    }
  }

  /** Converts a single {@link CascadeCell} to its two-character serialised token. */
  private static String serialiseCell(CascadeCell cell) {
    return switch (cell.getType()) {
      case EMPTY -> "..";
      case STONE -> "##";
      case PRISM -> cell.isActivated() ? "XX" : "xx";
      default -> {
        char c = colourToChar(cell.getColour());
        char t = typeToChar(cell.getType());
        if (cell.isActivated()) {
          yield "" + Character.toUpperCase(c) + Character.toUpperCase(t);
        } else {
          yield "" + c + t;
        }
      }
    };
  }

  private static char colourToChar(TileColour colour) {
    return switch (colour) {
      case RED -> 'r';
      case BLUE -> 'b';
      case GREEN -> 'g';
      case YELLOW -> 'y';
      case PURPLE -> 'p';
      case NONE -> throw new IllegalArgumentException("Cannot serialise NONE colour");
    };
  }

  private static char typeToChar(TileType type) {
    return switch (type) {
      case STANDARD -> 'o';
      case BOMB -> 'b';
      case ROCKET_H -> 'h';
      case ROCKET_V -> 'v';
      case ICE -> 'i';
      case EMPTY, STONE, PRISM -> throw new IllegalArgumentException(
          "Type " + type + " does not use colour+type encoding");
    };
  }

  /**
   * Parses a two-character token into a {@link CascadeCell}.
   *
   * @param token the two-character string to parse
   * @param x     column index (used in error messages only)
   * @param y     row index (used in error messages only)
   * @return the corresponding cell
   * @throws IllegalArgumentException if the token is not exactly two characters or is unrecognised
   */
  private static CascadeCell deserialiseToken(String token, int x, int y) {
    if (token.length() != 2) {
      throw new IllegalArgumentException(
          "Token at (" + x + "," + y + ") must be 2 characters, got: \"" + token + "\"");
    }
    if (token.equals("..")) {
      return CascadeCell.empty();
    }
    if (token.equals("##")) {
      return CascadeCell.stone();
    }
    boolean activated = Character.isUpperCase(token.charAt(0))
        && Character.isUpperCase(token.charAt(1));
    String lower = token.toLowerCase();
    if (lower.equals("xx")) {
      CascadeCell cell = CascadeCell.prism();
      if (activated) {
        cell.setActivated(true);
      }
      return cell;
    }
    TileColour colour = charToColour(lower.charAt(0), x, y);
    TileType type = charToType(lower.charAt(1), x, y);
    CascadeCell cell = buildCell(type, colour);
    if (activated) {
      cell.setActivated(true);
    }
    return cell;
  }

  private static TileColour charToColour(char c, int x, int y) {
    return switch (c) {
      case 'r' -> TileColour.RED;
      case 'b' -> TileColour.BLUE;
      case 'g' -> TileColour.GREEN;
      case 'y' -> TileColour.YELLOW;
      case 'p' -> TileColour.PURPLE;
      default -> throw new IllegalArgumentException(
          "Unknown colour character '" + c + "' at (" + x + "," + y + ")");
    };
  }

  private static TileType charToType(char c, int x, int y) {
    return switch (c) {
      case 'o' -> TileType.STANDARD;
      case 'b' -> TileType.BOMB;
      case 'h' -> TileType.ROCKET_H;
      case 'v' -> TileType.ROCKET_V;
      case 'i' -> TileType.ICE;
      default -> throw new IllegalArgumentException(
          "Unknown type character '" + c + "' at (" + x + "," + y + ")");
    };
  }

  private static CascadeCell buildCell(TileType type, TileColour colour) {
    return switch (type) {
      case STANDARD -> CascadeCell.standard(colour);
      case BOMB -> CascadeCell.bomb(colour);
      case ROCKET_H -> CascadeCell.rocketH(colour);
      case ROCKET_V -> CascadeCell.rocketV(colour);
      case ICE -> CascadeCell.ice(colour);
      case EMPTY, STONE, PRISM -> throw new IllegalArgumentException(
          "Unexpected type in buildCell: " + type);
    };
  }

}
