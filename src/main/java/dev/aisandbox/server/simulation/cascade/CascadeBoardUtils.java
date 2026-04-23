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
   * Fills every empty cell of {@code board} with a random standard tile such that no
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
  public static void initialise(CascadeBoard board, Random random) {
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
    // Any activated tile means the board is still mid-resolution
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        if (board.getCell(x, y).isActivated()) {
          return false;
        }
      }
    }
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
    // Check for unsettled empty cells.
    // An empty cell is unsettled if it is reachable from above: either a fallable tile sits above
    // it in the same column segment (gravity will move it down) or there is no STONE/ICE barrier
    // between the empty cell and the top of the board (new tiles will drop in to fill it).
    // An empty cell sealed above by a STONE or ICE is a permanent gap and does not affect stability.
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        if (board.getCell(x, y).getType() == TileType.EMPTY) {
          boolean sealedAbove = false;
          for (int above = y - 1; above >= 0; above--) {
            TileType aboveType = board.getCell(x, above).getType();
            if (aboveType == TileType.STONE || aboveType == TileType.ICE) {
              sealedAbove = true;
              break;
            }
            if (board.getCell(x, above).isFallable()) {
              return false; // fallable tile will drop into this empty cell
            }
          }
          if (!sealedAbove) {
            return false; // segment is open to the top — new tiles will enter
          }
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
  // Swap validation
  // ---------------------------------------------------------------------------

  /**
   * Returns {@code true} if swapping the cells at ({@code x1},{@code y1}) and
   * ({@code x2},{@code y2}) is a valid action.
   *
   * <p>The two positions must be orthogonally adjacent. The swap is valid when:
   * <ul>
   *   <li>one cell is a {@link TileType#PRISM} and the other is a swappable, occupied cell, or</li>
   *   <li>both cells are swappable and occupied, and performing the swap would create at least one
   *       run of three or more matchable tiles.</li>
   * </ul>
   *
   * @param board the current board state
   * @param x1    column of the first cell
   * @param y1    row of the first cell
   * @param x2    column of the second cell
   * @param y2    row of the second cell
   * @return {@code true} if the swap is legal
   */
  public static boolean isValidSwap(CascadeBoard board, int x1, int y1, int x2, int y2) {
    if (!board.inBounds(x1, y1) || !board.inBounds(x2, y2)) {
      return false;
    }
    if (Math.abs(x2 - x1) + Math.abs(y2 - y1) != 1) {
      return false;
    }
    CascadeCell c1 = board.getCell(x1, y1);
    CascadeCell c2 = board.getCell(x2, y2);
    if (c1.getType() == TileType.PRISM && c2.isOccupied() && isSwappable(board, x2, y2)) {
      return true;
    }
    if (c2.getType() == TileType.PRISM && c1.isOccupied() && isSwappable(board, x1, y1)) {
      return true;
    }
    if (!c1.isOccupied() || !c2.isOccupied()) {
      return false;
    }
    if (!isSwappable(board, x1, y1) || !isSwappable(board, x2, y2)) {
      return false;
    }
    return swapCreatesMatch(board, x1, y1, x2, y2);
  }

  // ---------------------------------------------------------------------------
  // Board resolution
  // ---------------------------------------------------------------------------

  /**
   * Resolves all pending matches on {@code board} using the full cascade loop.
   *
   * <p>Each wave finds all runs of three or more matchable tiles of the same colour, removes them,
   * applies gravity, refills empty cells with random tiles, and doubles the cascade multiplier
   * before repeating. The loop stops when the board is stable (no new matches).
   *
   * <p>The score gained during resolution is also added to {@link CascadeBoard#addScore}.
   *
   * @param board  the board to resolve (modified in place)
   * @param random the source of randomness used for tile refill
   * @return the total points scored during this resolution
   */
  public static long resolveBoard(CascadeBoard board, Random random) {
    long totalScore = 0;
    int multiplier = 1;
    while (true) {
      boolean[][] toRemove = new boolean[CascadeBoard.WIDTH][CascadeBoard.HEIGHT];
      int count = findAllMatches(board, toRemove);
      if (count == 0) {
        break;
      }
      long waveScore = (long) count * 10 * multiplier;
      totalScore += waveScore;
      board.addScore(waveScore);
      for (int x = 0; x < CascadeBoard.WIDTH; x++) {
        for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
          if (toRemove[x][y]) {
            board.setCell(x, y, CascadeCell.empty());
          }
        }
      }
      applyGravity(board);
      refill(board, random);
      multiplier *= 2;
    }
    return totalScore;
  }

  /**
   * Reshuffles all moveable tiles on the board when no valid moves remain.
   *
   * <p>All {@link CascadeCell#isFallable()} tiles are removed and the board is re-populated via
   * {@link #initialise(CascadeBoard, Random)}, which guarantees no pre-existing matches. Stones
   * and ice blocks remain in their original positions. Score and move count are unchanged.
   *
   * @param board  the board to reshuffle (modified in place)
   * @param random the source of randomness
   */
  public static void reshuffleBoard(CascadeBoard board, Random random) {
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        if (board.getCell(x, y).isFallable()) {
          board.setCell(x, y, CascadeCell.empty());
        }
      }
    }
    initialise(board, random);
  }

  /**
   * Scans {@code board} for all horizontal and vertical runs of three or more matchable tiles of
   * the same colour, marking each involved cell in {@code mark}.
   *
   * @param board  the board to scan
   * @param mark   a {@code WIDTH × HEIGHT} boolean array; cells to be removed are set to
   *               {@code true}
   * @return the number of unique cells marked for removal
   */
  private static int findAllMatches(CascadeBoard board, boolean[][] mark) {
    int count = 0;
    // Horizontal runs
    for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
      int x = 0;
      while (x < CascadeBoard.WIDTH) {
        CascadeCell cell = board.getCell(x, y);
        if (!cell.isMatchable()) {
          x++;
          continue;
        }
        TileColour colour = cell.getColour();
        int end = x + 1;
        while (end < CascadeBoard.WIDTH
            && board.getCell(end, y).isMatchable()
            && board.getCell(end, y).getColour() == colour) {
          end++;
        }
        if (end - x >= 3) {
          for (int i = x; i < end; i++) {
            if (!mark[i][y]) {
              mark[i][y] = true;
              count++;
            }
          }
        }
        x = end;
      }
    }
    // Vertical runs
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      int y = 0;
      while (y < CascadeBoard.HEIGHT) {
        CascadeCell cell = board.getCell(x, y);
        if (!cell.isMatchable()) {
          y++;
          continue;
        }
        TileColour colour = cell.getColour();
        int end = y + 1;
        while (end < CascadeBoard.HEIGHT
            && board.getCell(x, end).isMatchable()
            && board.getCell(x, end).getColour() == colour) {
          end++;
        }
        if (end - y >= 3) {
          for (int j = y; j < end; j++) {
            if (!mark[x][j]) {
              mark[x][j] = true;
              count++;
            }
          }
        }
        y = end;
      }
    }
    return count;
  }

  /**
   * Applies gravity to every column: fallable tiles sink to fill empty cells below them.
   *
   * <p>{@link TileType#STONE} and {@link TileType#ICE} cells are fixed obstacles; they do not
   * move and split a column into independent segments. Within each segment, fallable tiles compact
   * to the bottom while empty cells float to the top.
   */
  private static void applyGravity(CascadeBoard board) {
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      int segBottom = CascadeBoard.HEIGHT - 1;
      while (segBottom >= 0) {
        TileType bt = board.getCell(x, segBottom).getType();
        if (bt == TileType.STONE || bt == TileType.ICE) {
          segBottom--;
          continue;
        }
        // Find the top of this segment
        int segTop = segBottom;
        while (segTop > 0) {
          TileType above = board.getCell(x, segTop - 1).getType();
          if (above == TileType.STONE || above == TileType.ICE) {
            break;
          }
          segTop--;
        }
        // Compact fallable tiles to the bottom of [segTop..segBottom]
        int writeY = segBottom;
        for (int y = segBottom; y >= segTop; y--) {
          if (board.getCell(x, y).isFallable()) {
            if (writeY != y) {
              board.setCell(x, writeY, board.getCell(x, y));
              board.setCell(x, y, CascadeCell.empty());
            }
            writeY--;
          }
        }
        segBottom = segTop - 1;
      }
    }
  }

  /**
   * Fills every empty cell on the board with a new random standard tile.
   *
   * <p>Called after {@link #applyGravity(CascadeBoard)} to restore the board to a fully occupied
   * state. Colours are chosen uniformly at random; a subsequent cascade check will resolve any
   * matches that happen to form.
   *
   * @param board  the board to refill (modified in place)
   * @param random the source of randomness
   */
  private static void refill(CascadeBoard board, Random random) {
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        if (!board.getCell(x, y).isOccupied()) {
          board.setCell(x, y, CascadeCell.standard(COLOURS[random.nextInt(COLOURS.length)]));
        }
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Shared helpers for explosions and prism effects
  // ---------------------------------------------------------------------------

  /**
   * Triggers the prism colour effect: destroys all STANDARD tiles of {@code colour}, activates
   * all BOMB/ROCKET tiles of that colour, and unfreezes all ICE tiles of that colour to STANDARD.
   *
   * <p>The PRISM cell itself must be handled by the caller before invoking this method.
   *
   * @param board  the board to modify
   * @param colour the colour to target
   * @return the number of STANDARD tiles destroyed (for scoring by the caller)
   */
  private static int triggerPrismEffect(CascadeBoard board, TileColour colour) {
    int destroyed = 0;
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        CascadeCell cell = board.getCell(x, y);
        if (cell.getType() == TileType.STANDARD && cell.getColour() == colour) {
          board.setCell(x, y, CascadeCell.empty());
          destroyed++;
        } else if (isSpecial(cell.getType()) && cell.getColour() == colour) {
          cell.setActivated(true);
        } else if (cell.getType() == TileType.ICE && cell.getColour() == colour) {
          board.setCell(x, y, CascadeCell.standard(colour));
        }
      }
    }
    return destroyed;
  }

  /**
   * Fires a rocket-style beam from ({@code startX},{@code startY}) in the direction
   * ({@code dx},{@code dy}), processing one cell at a time. The starting cell itself is
   * not processed — the caller must handle it.
   *
   * @param board       the board to modify
   * @param startX      column of the origin (not processed)
   * @param startY      row of the origin (not processed)
   * @param dx          horizontal step (-1, 0, or 1)
   * @param dy          vertical step (-1, 0, or 1)
   * @param triggerColour colour used when hitting a prism
   * @return the number of tiles destroyed (replaced with EMPTY)
   */
  private static int fireInDirection(CascadeBoard board, int startX, int startY,
      int dx, int dy, TileColour triggerColour) {
    int destroyed = 0;
    int x = startX + dx;
    int y = startY + dy;
    while (board.inBounds(x, y)) {
      CascadeCell cell = board.getCell(x, y);
      if (cell.getType() == TileType.EMPTY) {
        x += dx;
        y += dy;
        continue;
      }
      if (isSpecial(cell.getType())) {
        cell.setActivated(true);
        x += dx;
        y += dy;
        continue;
      }
      if (cell.getType() == TileType.PRISM) {
        board.setCell(x, y, CascadeCell.empty());
        destroyed++;
        destroyed += triggerPrismEffect(board, triggerColour);
        x += dx;
        y += dy;
        continue;
      }
      if (cell.getType() == TileType.STONE) {
        board.setCell(x, y, CascadeCell.empty());
        destroyed++;
        break;
      }
      // ICE, STANDARD, or anything else: destroy and continue
      board.setCell(x, y, CascadeCell.empty());
      destroyed++;
      x += dx;
      y += dy;
    }
    return destroyed;
  }

  /**
   * Returns {@code true} if the tile type is a special that can be activated (BOMB or ROCKET).
   */
  private static boolean isSpecial(TileType type) {
    return type == TileType.BOMB || type == TileType.ROCKET_H || type == TileType.ROCKET_V;
  }

  /**
   * Returns {@code true} if the tile type is any rocket variant.
   */
  private static boolean isRocket(TileType type) {
    return type == TileType.ROCKET_H || type == TileType.ROCKET_V;
  }

  /**
   * Processes a single cell during a bomb/explosion area effect. Handles activation of specials,
   * prism triggers, and destruction of other tile types.
   *
   * @param board        the board to modify
   * @param x            column of the cell to process
   * @param y            row of the cell to process
   * @param triggerColour colour used when hitting a prism
   * @return the number of tiles destroyed (replaced with EMPTY)
   */
  private static int processExplosionCell(CascadeBoard board, int x, int y,
      TileColour triggerColour) {
    CascadeCell cell = board.getCell(x, y);
    if (cell.getType() == TileType.EMPTY) {
      return 0;
    }
    if (isSpecial(cell.getType())) {
      cell.setActivated(true);
      return 0;
    }
    if (cell.getType() == TileType.PRISM) {
      board.setCell(x, y, CascadeCell.empty());
      int destroyed = 1 + triggerPrismEffect(board, triggerColour);
      return destroyed;
    }
    // STANDARD, ICE, STONE: destroy
    board.setCell(x, y, CascadeCell.empty());
    return 1;
  }

  // ---------------------------------------------------------------------------
  // Make Move
  // ---------------------------------------------------------------------------

  /**
   * Performs a move by swapping the two selected tiles and applying any immediate effects
   * (prism interactions, special+special combos). Full logic is described in runtime.md.
   *
   * @param board the current board (not modified for normal swaps; may be modified for
   *              prism/special interactions)
   * @param x1    column of the first cell
   * @param y1    row of the first cell
   * @param x2    column of the second cell
   * @param y2    row of the second cell
   * @return the board after the swap and any immediate effects have been applied
   * @throws InvalidCascadeAction if the move is not valid
   */
  public static CascadeBoard makeMove(CascadeBoard board, int x1, int y1, int x2, int y2)
      throws InvalidCascadeAction {
    // Step 1: adjacency check
    if (Math.abs(x2 - x1) + Math.abs(y2 - y1) != 1) {
      throw new InvalidCascadeAction("Cells are not adjacent");
    }
    CascadeCell c1 = board.getCell(x1, y1);
    CascadeCell c2 = board.getCell(x2, y2);
    // Step 2: swappability check
    if (!c1.isSwappable() || !c2.isSwappable()) {
      throw new InvalidCascadeAction("Cell is not swappable");
    }

    // Step 3: Prism + Prism
    if (c1.getType() == TileType.PRISM && c2.getType() == TileType.PRISM) {
      int count = 0;
      for (int x = 0; x < CascadeBoard.WIDTH; x++) {
        for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
          CascadeCell cell = board.getCell(x, y);
          if (cell.getType() != TileType.EMPTY && cell.getType() != TileType.STONE) {
            board.setCell(x, y, CascadeCell.empty());
            count++;
          }
        }
      }
      board.addScore(count * CascadeBoard.TILE_SCORE * board.getMultiplier());
      return board;
    }

    // Step 4: Prism + Special (BOMB/ROCKET)
    if ((c1.getType() == TileType.PRISM && isSpecial(c2.getType()))
        || (c2.getType() == TileType.PRISM && isSpecial(c1.getType()))) {
      int prismX;
      int prismY;
      int specialX;
      int specialY;
      if (c1.getType() == TileType.PRISM) {
        prismX = x1;
        prismY = y1;
        specialX = x2;
        specialY = y2;
      } else {
        prismX = x2;
        prismY = y2;
        specialX = x1;
        specialY = y1;
      }
      CascadeCell special = board.getCell(specialX, specialY);
      TileType specialType = special.getType();
      TileColour specialColour = special.getColour();
      // Replace prism with activated copy of the special
      CascadeCell prismReplacement = buildCell(specialType, specialColour);
      prismReplacement.setActivated(true);
      board.setCell(prismX, prismY, prismReplacement);
      // Replace original special with empty
      board.setCell(specialX, specialY, CascadeCell.empty());
      // Convert STANDARD of that colour, activate existing specials, unfreeze ice
      int converted = 0;
      for (int x = 0; x < CascadeBoard.WIDTH; x++) {
        for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
          if (x == prismX && y == prismY) {
            continue;
          }
          CascadeCell cell = board.getCell(x, y);
          if (cell.getType() == TileType.STANDARD && cell.getColour() == specialColour) {
            CascadeCell replacement = buildCell(specialType, specialColour);
            replacement.setActivated(true);
            board.setCell(x, y, replacement);
            converted++;
          } else if (isSpecial(cell.getType()) && cell.getColour() == specialColour) {
            cell.setActivated(true);
          } else if (cell.getType() == TileType.ICE && cell.getColour() == specialColour) {
            board.setCell(x, y, CascadeCell.standard(specialColour));
          }
        }
      }
      board.addScore((converted + 1) * CascadeBoard.TILE_SCORE);
      return board;
    }

    // Step 5: Prism + Standard
    if ((c1.getType() == TileType.PRISM && c2.getType() == TileType.STANDARD)
        || (c2.getType() == TileType.PRISM && c1.getType() == TileType.STANDARD)) {
      int prismX;
      int prismY;
      CascadeCell standardCell;
      if (c1.getType() == TileType.PRISM) {
        prismX = x1;
        prismY = y1;
        standardCell = c2;
      } else {
        prismX = x2;
        prismY = y2;
        standardCell = c1;
      }
      TileColour colour = standardCell.getColour();
      board.setCell(prismX, prismY, CascadeCell.empty());
      int destroyed = triggerPrismEffect(board, colour);
      board.addScore(destroyed * CascadeBoard.TILE_SCORE * board.getMultiplier());
      return board;
    }

    // Step 6: Bomb + Bomb
    if (c1.getType() == TileType.BOMB && c2.getType() == TileType.BOMB) {
      TileColour bombColour = c1.getColour();
      board.setCell(x1, y1, CascadeCell.empty());
      board.setCell(x2, y2, CascadeCell.empty());
      int destroyed = 2; // count the two bombs themselves
      // Process 5x5 area centred on each bomb
      for (int bx : new int[]{x1, x2}) {
        for (int by : new int[]{y1, y2}) {
          for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
              int nx = bx + dx;
              int ny = by + dy;
              if (board.inBounds(nx, ny)) {
                destroyed += processExplosionCell(board, nx, ny, bombColour);
              }
            }
          }
        }
      }
      board.addScore(destroyed * CascadeBoard.TILE_SCORE * board.getMultiplier());
      return board;
    }

    // Step 7: Bomb + Rocket
    if ((c1.getType() == TileType.BOMB && isRocket(c2.getType()))
        || (c2.getType() == TileType.BOMB && isRocket(c1.getType()))) {
      int bombX;
      int bombY;
      TileColour bombColour;
      if (c1.getType() == TileType.BOMB) {
        bombX = x1;
        bombY = y1;
        bombColour = c1.getColour();
      } else {
        bombX = x2;
        bombY = y2;
        bombColour = c2.getColour();
      }
      board.setCell(x1, y1, CascadeCell.empty());
      board.setCell(x2, y2, CascadeCell.empty());
      int destroyed = 2; // count the bomb and rocket themselves
      // Destroy 4 diagonal neighbours of the bomb's position
      int[][] diags = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
      for (int[] d : diags) {
        int nx = bombX + d[0];
        int ny = bombY + d[1];
        if (board.inBounds(nx, ny)) {
          destroyed += processExplosionCell(board, nx, ny, bombColour);
        }
      }
      // Fire in 4 cardinal directions from the bomb's position
      destroyed += fireInDirection(board, bombX, bombY, 0, -1, bombColour);
      destroyed += fireInDirection(board, bombX, bombY, 0, 1, bombColour);
      destroyed += fireInDirection(board, bombX, bombY, -1, 0, bombColour);
      destroyed += fireInDirection(board, bombX, bombY, 1, 0, bombColour);
      board.addScore(destroyed * CascadeBoard.TILE_SCORE * board.getMultiplier());
      return board;
    }

    // Step 8: Rocket + Rocket
    if (isRocket(c1.getType()) && isRocket(c2.getType())) {
      TileColour colour1 = c1.getColour();
      TileColour colour2 = c2.getColour();
      board.setCell(x1, y1, CascadeCell.empty());
      board.setCell(x2, y2, CascadeCell.empty());
      int destroyed = 2; // count both rockets themselves
      // Rocket 1 fires along its row and column
      destroyed += fireInDirection(board, x1, y1, -1, 0, colour1);
      destroyed += fireInDirection(board, x1, y1, 1, 0, colour1);
      destroyed += fireInDirection(board, x1, y1, 0, -1, colour1);
      destroyed += fireInDirection(board, x1, y1, 0, 1, colour1);
      // Rocket 2 fires along its row and column
      destroyed += fireInDirection(board, x2, y2, -1, 0, colour2);
      destroyed += fireInDirection(board, x2, y2, 1, 0, colour2);
      destroyed += fireInDirection(board, x2, y2, 0, -1, colour2);
      destroyed += fireInDirection(board, x2, y2, 0, 1, colour2);
      board.addScore(destroyed * CascadeBoard.TILE_SCORE * board.getMultiplier());
      return board;
    }

    // Steps 9-11: Normal swap
    CascadeBoard copy = board.copy();
    copy.swap(x1, y1, x2, y2);
    if (!hasMatchAt(copy, x1, y1) && !hasMatchAt(copy, x2, y2)) {
      throw new InvalidCascadeAction("Swap creates no match");
    }
    return copy;
  }

  // ---------------------------------------------------------------------------
  // Update Board
  // ---------------------------------------------------------------------------

  /**
   * Advances an unstable board by one step: applies gravity and refill (priority 1),
   * resolves activated specials (priority 2), or resolves matches and spawns specials
   * (priority 3). Only the single highest-priority applicable action is performed per call.
   * Full logic is described in runtime.md.
   *
   * <p>Call this method repeatedly until {@link #isStable(CascadeBoard)} returns {@code true}.
   *
   * @param board  the board to advance (modified in place)
   * @param random the source of randomness for tile refill
   * @return the updated board
   */
  public static CascadeBoard updateBoard(CascadeBoard board, Random random) {
    // Priority 1: Gravity and refill
    if (applyGravityAndSmartRefill(board, random)) {
      board.setMultiplier(board.getMultiplier() * 2);
      return board;
    }

    // Priority 2: Resolve activated specials
    if (resolveActivatedSpecials(board)) {
      return board;
    }

    // Priority 3: Resolve matches and spawn specials
    resolveMatchesAndSpawn(board);
    return board;
  }

  /**
   * Applies gravity and refills open column segments. Returns {@code true} if any tile moved
   * or was created.
   */
  private static boolean applyGravityAndSmartRefill(CascadeBoard board, Random random) {
    boolean changed = false;
    // Apply gravity: compact fallable tiles downward
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      int segBottom = CascadeBoard.HEIGHT - 1;
      while (segBottom >= 0) {
        TileType bt = board.getCell(x, segBottom).getType();
        if (bt == TileType.STONE || bt == TileType.ICE) {
          segBottom--;
          continue;
        }
        int segTop = segBottom;
        while (segTop > 0) {
          TileType above = board.getCell(x, segTop - 1).getType();
          if (above == TileType.STONE || above == TileType.ICE) {
            break;
          }
          segTop--;
        }
        int writeY = segBottom;
        for (int y = segBottom; y >= segTop; y--) {
          if (board.getCell(x, y).isFallable()) {
            if (writeY != y) {
              board.setCell(x, writeY, board.getCell(x, y));
              board.setCell(x, y, CascadeCell.empty());
              changed = true;
            }
            writeY--;
          }
        }
        segBottom = segTop - 1;
      }
    }
    // Smart refill: only fill empty cells that are open to the top (not sealed by STONE/ICE)
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        if (board.getCell(x, y).getType() == TileType.EMPTY) {
          boolean sealed = false;
          for (int above = y - 1; above >= 0; above--) {
            TileType aboveType = board.getCell(x, above).getType();
            if (aboveType == TileType.STONE || aboveType == TileType.ICE) {
              sealed = true;
              break;
            }
          }
          if (!sealed) {
            board.setCell(x, y, CascadeCell.standard(COLOURS[random.nextInt(COLOURS.length)]));
            changed = true;
          }
        }
      }
    }
    return changed;
  }

  /**
   * Resolves all activated specials (bombs and rockets) with chain reactions.
   * Returns {@code true} if any activated tiles were processed.
   */
  private static boolean resolveActivatedSpecials(CascadeBoard board) {
    boolean anyProcessed = false;
    boolean hasActivated = true;
    while (hasActivated) {
      hasActivated = false;
      // Collect activated tiles
      List<int[]> activated = new ArrayList<>();
      for (int x = 0; x < CascadeBoard.WIDTH; x++) {
        for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
          if (board.getCell(x, y).isActivated()) {
            activated.add(new int[]{x, y,
                board.getCell(x, y).getType().ordinal(),
                board.getCell(x, y).getColour().ordinal()});
            board.getCell(x, y).setActivated(false);
          }
        }
      }
      if (activated.isEmpty()) {
        break;
      }
      anyProcessed = true;
      // Process each activated tile
      for (int[] info : activated) {
        int ax = info[0];
        int ay = info[1];
        TileType aType = board.getCell(ax, ay).getType();
        TileColour aColour = TileColour.values()[info[3]];
        // The tile might have been destroyed by a previous chain reaction in this pass
        if (board.getCell(ax, ay).getType() == TileType.EMPTY) {
          continue;
        }
        if (aType == TileType.BOMB) {
          board.setCell(ax, ay, CascadeCell.empty());
          board.addScore(CascadeBoard.TILE_SCORE * board.getMultiplier());
          int destroyed = 0;
          for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
              int nx = ax + dx;
              int ny = ay + dy;
              if (board.inBounds(nx, ny)) {
                destroyed += processExplosionCell(board, nx, ny, aColour);
              }
            }
          }
          board.addScore((long) destroyed * CascadeBoard.TILE_SCORE * board.getMultiplier());
        } else if (aType == TileType.ROCKET_H) {
          board.setCell(ax, ay, CascadeCell.empty());
          board.addScore(CascadeBoard.TILE_SCORE * board.getMultiplier());
          int destroyed = 0;
          destroyed += fireInDirection(board, ax, ay, -1, 0, aColour);
          destroyed += fireInDirection(board, ax, ay, 1, 0, aColour);
          board.addScore((long) destroyed * CascadeBoard.TILE_SCORE * board.getMultiplier());
        } else if (aType == TileType.ROCKET_V) {
          board.setCell(ax, ay, CascadeCell.empty());
          board.addScore(CascadeBoard.TILE_SCORE * board.getMultiplier());
          int destroyed = 0;
          destroyed += fireInDirection(board, ax, ay, 0, -1, aColour);
          destroyed += fireInDirection(board, ax, ay, 0, 1, aColour);
          board.addScore((long) destroyed * CascadeBoard.TILE_SCORE * board.getMultiplier());
        }
      }
      // Check for new activations (chain reaction)
      for (int x = 0; x < CascadeBoard.WIDTH; x++) {
        for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
          if (board.getCell(x, y).isActivated()) {
            hasActivated = true;
            break;
          }
        }
        if (hasActivated) {
          break;
        }
      }
    }
    return anyProcessed;
  }

  /**
   * Unmarks ICE tiles that sit at the boundary of a run of 4+ and whose removal still leaves
   * a valid run of 3+. These tiles will be unfrozen via adjacency (step 5) instead of being
   * destroyed as part of the match.
   */
  private static void pruneNonEssentialIce(CascadeBoard board, boolean[][] mark) {
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        if (!mark[x][y] || board.getCell(x, y).getType() != TileType.ICE) {
          continue;
        }
        TileColour colour = board.getCell(x, y).getColour();
        boolean essential = false;
        // Check horizontal run containing this cell
        int hLeft = x;
        while (hLeft > 0 && mark[hLeft - 1][y]
            && board.getCell(hLeft - 1, y).isMatchable()
            && board.getCell(hLeft - 1, y).getColour() == colour) {
          hLeft--;
        }
        int hRight = x;
        while (hRight < CascadeBoard.WIDTH - 1 && mark[hRight + 1][y]
            && board.getCell(hRight + 1, y).isMatchable()
            && board.getCell(hRight + 1, y).getColour() == colour) {
          hRight++;
        }
        int hLen = hRight - hLeft + 1;
        if (hLen >= 3) {
          if (hLen <= 3 || (x != hLeft && x != hRight)) {
            essential = true;
          }
        }
        // Check vertical run containing this cell
        int vTop = y;
        while (vTop > 0 && mark[x][vTop - 1]
            && board.getCell(x, vTop - 1).isMatchable()
            && board.getCell(x, vTop - 1).getColour() == colour) {
          vTop--;
        }
        int vBottom = y;
        while (vBottom < CascadeBoard.HEIGHT - 1 && mark[x][vBottom + 1]
            && board.getCell(x, vBottom + 1).isMatchable()
            && board.getCell(x, vBottom + 1).getColour() == colour) {
          vBottom++;
        }
        int vLen = vBottom - vTop + 1;
        if (vLen >= 3) {
          if (vLen <= 3 || (y != vTop && y != vBottom)) {
            essential = true;
          }
        }
        if (!essential) {
          mark[x][y] = false;
        }
      }
    }
  }

  /**
   * Resolves all matches on the board, spawns specials from match geometry, unfreezes adjacent
   * ice, and scores. Does nothing if no matches exist.
   */
  private static void resolveMatchesAndSpawn(CascadeBoard board) {
    boolean[][] mark = new boolean[CascadeBoard.WIDTH][CascadeBoard.HEIGHT];
    int totalMarked = findAllMatches(board, mark);
    if (totalMarked == 0) {
      return;
    }

    // Prune non-essential ICE from match boundaries: ICE at the start/end of a run of 4+
    // can be unfrozen via adjacency (step 5) instead of being destroyed as part of the match.
    pruneNonEssentialIce(board, mark);

    // Determine special spawns from match geometry
    // Track what special (if any) should spawn at each marked position
    TileType[][] spawnType = new TileType[CascadeBoard.WIDTH][CascadeBoard.HEIGHT];
    TileColour[][] spawnColour = new TileColour[CascadeBoard.WIDTH][CascadeBoard.HEIGHT];

    // Find horizontal runs and assign bomb/prism spawns
    for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
      int x = 0;
      while (x < CascadeBoard.WIDTH) {
        CascadeCell cell = board.getCell(x, y);
        if (!cell.isMatchable()) {
          x++;
          continue;
        }
        TileColour colour = cell.getColour();
        int end = x + 1;
        while (end < CascadeBoard.WIDTH
            && board.getCell(end, y).isMatchable()
            && board.getCell(end, y).getColour() == colour) {
          end++;
        }
        int len = end - x;
        if (len >= 5) {
          int centre = x + (len - 1) / 2;
          TileType candidate = len >= 6 ? TileType.PRISM : TileType.BOMB;
          if (spawnType[centre][y] == null || spawnTier(candidate) > spawnTier(spawnType[centre][y])) {
            spawnType[centre][y] = candidate;
            spawnColour[centre][y] = colour;
          }
        }
        x = end;
      }
    }

    // Find vertical runs and assign bomb/prism spawns
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      int y = 0;
      while (y < CascadeBoard.HEIGHT) {
        CascadeCell cell = board.getCell(x, y);
        if (!cell.isMatchable()) {
          y++;
          continue;
        }
        TileColour colour = cell.getColour();
        int end = y + 1;
        while (end < CascadeBoard.HEIGHT
            && board.getCell(x, end).isMatchable()
            && board.getCell(x, end).getColour() == colour) {
          end++;
        }
        int len = end - y;
        if (len >= 5) {
          int centre = y + (len - 1) / 2;
          TileType candidate = len >= 6 ? TileType.PRISM : TileType.BOMB;
          if (spawnType[x][centre] == null
              || spawnTier(candidate) > spawnTier(spawnType[x][centre])) {
            spawnType[x][centre] = candidate;
            spawnColour[x][centre] = colour;
          }
        }
        y = end;
      }
    }

    // Find L/T shapes: cells at intersection of horizontal and vertical runs (each >= 3)
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        if (!mark[x][y]) {
          continue;
        }
        CascadeCell cell = board.getCell(x, y);
        if (!cell.isMatchable()) {
          continue;
        }
        TileColour colour = cell.getColour();
        int hLen = 1
            + countRun(board, x, y, -1, 0, colour)
            + countRun(board, x, y, 1, 0, colour);
        int vLen = 1
            + countRun(board, x, y, 0, -1, colour)
            + countRun(board, x, y, 0, 1, colour);
        if (hLen >= 3 && vLen >= 3) {
          TileType candidate = hLen >= vLen ? TileType.ROCKET_H : TileType.ROCKET_V;
          if (spawnType[x][y] == null || spawnTier(candidate) > spawnTier(spawnType[x][y])) {
            spawnType[x][y] = candidate;
            spawnColour[x][y] = colour;
          }
        }
      }
    }

    // Process marked tiles and track which cells were replaced with EMPTY
    boolean[][] removed = new boolean[CascadeBoard.WIDTH][CascadeBoard.HEIGHT];
    int removeCount = 0;
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        if (!mark[x][y]) {
          continue;
        }
        CascadeCell cell = board.getCell(x, y);
        if (isSpecial(cell.getType())) {
          // Activate specials (bomb/rocket) — don't remove
          cell.setActivated(true);
        } else if (spawnType[x][y] != null) {
          // Spawn position: place the new special
          CascadeCell spawned;
          if (spawnType[x][y] == TileType.PRISM) {
            spawned = CascadeCell.prism();
          } else {
            spawned = buildCell(spawnType[x][y], spawnColour[x][y]);
          }
          board.setCell(x, y, spawned);
          // The original tile is "removed" but replaced by a special — still counts as removed
          // Wait, per spec: "Count only tiles replaced with EMPTY in step 4b"
          // Spawn position does NOT count toward removed
        } else {
          // Replace with EMPTY
          board.setCell(x, y, CascadeCell.empty());
          removed[x][y] = true;
          removeCount++;
        }
      }
    }

    // Unfreeze adjacent ice
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        if (!removed[x][y]) {
          continue;
        }
        int[][] neighbours = {{x - 1, y}, {x + 1, y}, {x, y - 1}, {x, y + 1}};
        for (int[] n : neighbours) {
          if (board.inBounds(n[0], n[1])
              && board.getCell(n[0], n[1]).getType() == TileType.ICE) {
            TileColour iceColour = board.getCell(n[0], n[1]).getColour();
            board.setCell(n[0], n[1], CascadeCell.standard(iceColour));
          }
        }
      }
    }

    // Score
    board.addScore((long) removeCount * CascadeBoard.TILE_SCORE * board.getMultiplier());
  }

  /**
   * Returns a priority tier for spawn type selection. Higher values win.
   */
  private static int spawnTier(TileType type) {
    return switch (type) {
      case PRISM -> 3;
      case BOMB -> 2;
      case ROCKET_H, ROCKET_V -> 1;
      default -> 0;
    };
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
