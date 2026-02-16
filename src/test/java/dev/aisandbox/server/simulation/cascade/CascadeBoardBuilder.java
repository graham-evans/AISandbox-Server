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

/**
 * Test utility that constructs a {@link CascadeBoard} from a human-readable text representation.
 *
 * <p>Call {@link #parse(String...)} with exactly {@value CascadeBoard#HEIGHT} strings, each of
 * exactly {@value CascadeBoard#WIDTH} characters. The first string is the top row ({@code y=0});
 * within each string the leftmost character is column {@code x=0}.
 *
 * <h2>Character map</h2>
 * <pre>
 *   R  —  Standard RED
 *   B  —  Standard BLUE
 *   G  —  Standard GREEN
 *   Y  —  Standard YELLOW
 *   P  —  Standard PURPLE
 *   r  —  ICE encasing RED
 *   b  —  ICE encasing BLUE
 *   g  —  ICE encasing GREEN
 *   y  —  ICE encasing YELLOW
 *   p  —  ICE encasing PURPLE
 *   .  —  EMPTY
 *   S  —  STONE
 *   ~  —  PRISM
 *   *  —  BOMB  (colour NONE)
 *   H  —  ROCKET_H (colour NONE)
 *   V  —  ROCKET_V (colour NONE)
 * </pre>
 */
public final class CascadeBoardBuilder {

  private CascadeBoardBuilder() {
    // utility class
  }

  /**
   * Parses a text grid into a {@link CascadeBoard}.
   *
   * <p>The board's {@code movesRemaining} and {@code score} fields are left at their constructor
   * defaults (30 and 0 respectively); only the cell grid is populated.
   *
   * @param rows exactly {@value CascadeBoard#HEIGHT} strings, each of exactly
   *             {@value CascadeBoard#WIDTH} characters
   * @return a fully populated {@link CascadeBoard}
   * @throws IllegalArgumentException if the wrong number of rows or columns is supplied, or if an
   *                                  unrecognised character is encountered
   */
  public static CascadeBoard parse(String... rows) {
    if (rows.length != CascadeBoard.HEIGHT) {
      throw new IllegalArgumentException(
          "Expected " + CascadeBoard.HEIGHT + " rows, got " + rows.length);
    }
    CascadeBoard board = new CascadeBoard();
    for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
      String row = rows[y];
      if (row.length() != CascadeBoard.WIDTH) {
        throw new IllegalArgumentException(
            "Row " + y + ": expected " + CascadeBoard.WIDTH + " characters, got " + row.length());
      }
      for (int x = 0; x < CascadeBoard.WIDTH; x++) {
        board.setCell(x, y, parseChar(row.charAt(x), x, y));
      }
    }
    return board;
  }

  /**
   * Converts a single character to its corresponding {@link CascadeCell}.
   *
   * @param ch the character to convert
   * @param x  column (used only for error messages)
   * @param y  row (used only for error messages)
   * @return the matching {@link CascadeCell}
   * @throws IllegalArgumentException if {@code ch} is not a recognised board character
   */
  private static CascadeCell parseChar(char ch, int x, int y) {
    return switch (ch) {
      // Standard coloured tiles
      case 'R' -> CascadeCell.standard(TileColour.RED);
      case 'B' -> CascadeCell.standard(TileColour.BLUE);
      case 'G' -> CascadeCell.standard(TileColour.GREEN);
      case 'Y' -> CascadeCell.standard(TileColour.YELLOW);
      case 'P' -> CascadeCell.standard(TileColour.PURPLE);
      // Ice-encased coloured tiles
      case 'r' -> new CascadeCell(TileType.ICE, TileColour.RED);
      case 'b' -> new CascadeCell(TileType.ICE, TileColour.BLUE);
      case 'g' -> new CascadeCell(TileType.ICE, TileColour.GREEN);
      case 'y' -> new CascadeCell(TileType.ICE, TileColour.YELLOW);
      case 'p' -> new CascadeCell(TileType.ICE, TileColour.PURPLE);
      // Special objects
      case '*' -> new CascadeCell(TileType.BOMB, TileColour.NONE);
      case 'H' -> new CascadeCell(TileType.ROCKET_H, TileColour.NONE);
      case 'V' -> new CascadeCell(TileType.ROCKET_V, TileColour.NONE);
      case '~' -> new CascadeCell(TileType.PRISM, TileColour.NONE);
      case 'S' -> new CascadeCell(TileType.STONE, TileColour.NONE);
      // Empty cell
      case '.' -> CascadeCell.empty();
      default -> throw new IllegalArgumentException(
          "Unrecognised board character '" + ch + "' at (" + x + "," + y + ")");
    };
  }
}