/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

import static org.junit.jupiter.api.Assertions.fail;

import dev.aisandbox.server.simulation.cascade.model.CascadeBoard;
import dev.aisandbox.server.simulation.cascade.model.CascadeCell;
import dev.aisandbox.server.simulation.cascade.model.TileColour;
import dev.aisandbox.server.simulation.cascade.model.TileType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test helper for comparing and rendering Cascade boards.
 *
 * <p>Expected boards are expressed as row strings using the standard two-character serialisation
 * format, extended with wildcard tokens for cells whose colour is non-deterministic (e.g. filled
 * by a random refill step):
 *
 * <ul>
 *   <li>{@code ??} &mdash; any non-EMPTY cell (type, colour, and activated state unchecked)</li>
 *   <li>{@code ?o} &mdash; STANDARD tile of any colour</li>
 *   <li>{@code ?b} &mdash; BOMB of any colour</li>
 *   <li>{@code ?h} &mdash; ROCKET_H of any colour</li>
 *   <li>{@code ?v} &mdash; ROCKET_V of any colour</li>
 *   <li>{@code ?i} &mdash; ICE of any colour</li>
 * </ul>
 *
 * <p>All other tokens follow the format defined in {@link CascadeBoardUtils#serialiseBoard}:
 * {@code ..} (empty), {@code ##} (stone), {@code xx}/{@code XX} (prism / activated prism), and
 * colour+type pairs like {@code ro} (red standard) or {@code GB} (activated green bomb).
 *
 * <p>Wildcard matches do not check the activated flag. Exact tokens do.
 *
 * <p>Typical usage in a test:
 * <pre>{@code
 * CascadeBoard actual = ...;
 * CascadeBoardAssert.assertMatches(actual,
 *     "ro go bo ro go bo ro go",
 *     "go bo ?o ?o ?o ?o go bo",   // random refill positions
 *     "bo ro go bo ro go bo ro",
 *     "ro go bo ro go bo ro go",
 *     "go bo ro go bo ro go bo",
 *     "bo ro go bo ro go bo ro",
 *     "ro go bo ro go bo ro go",
 *     "go bo ro go bo ro go bo"
 * );
 * }</pre>
 */
public final class CascadeBoardAssert {

  private static final String EMPTY_ROW = ".. .. .. .. .. .. .. ..";

  private CascadeBoardAssert() {
    // utility class
  }

  // ── Board construction helper ───────────────────────────────────────────

  /**
   * Builds a {@link CascadeBoard} from row strings in the standard serialisation format.
   * Any rows not supplied are filled with empty cells.
   *
   * <p>This is a convenience method identical to the private helpers in existing test classes,
   * made public so all test classes can share it.
   *
   * @param rows up to eight row strings, each containing eight space-separated tokens
   * @return a fully populated board
   */
  public static CascadeBoard board(String... rows) {
    List<String> padded = pad(rows);
    CascadeBoard b = new CascadeBoard();
    CascadeBoardUtils.deserialiseBoard(b, padded);
    return b;
  }

  // ── Assertion ───────────────────────────────────────────────────────────

  /**
   * Asserts that {@code actual} matches the expected row pattern cell by cell.
   *
   * <p>Any rows not supplied are treated as all-empty. On failure the error message includes:
   * <ol>
   *   <li>A list of every mismatched cell with position, expected description, actual description,
   *       and reason.</li>
   *   <li>A side-by-side rendering of the expected pattern and the actual board.</li>
   * </ol>
   *
   * @param actual       the board to verify
   * @param expectedRows up to eight row strings; supports wildcard tokens
   */
  public static void assertMatches(CascadeBoard actual, String... expectedRows) {
    List<String> padded = pad(expectedRows);
    List<String> mismatches = new ArrayList<>();

    for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
      String[] tokens = padded.get(y).split(" ");
      if (tokens.length != CascadeBoard.WIDTH) {
        fail("Expected row " + y + " has " + tokens.length
            + " tokens, need " + CascadeBoard.WIDTH);
      }
      for (int x = 0; x < CascadeBoard.WIDTH; x++) {
        String reason = checkCell(tokens[x], actual.getCell(x, y));
        if (reason != null) {
          mismatches.add(String.format("  (%d,%d): expected %-24s actual %-24s %s",
              x, y, describeToken(tokens[x]), describeCell(actual.getCell(x, y)), reason));
        }
      }
    }

    if (!mismatches.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      sb.append("Board mismatch: ").append(mismatches.size()).append(" cell(s) differ\n\n");
      for (String m : mismatches) {
        sb.append(m).append('\n');
      }
      sb.append('\n');
      sb.append(renderComparison(padded, actual));
      fail(sb.toString());
    }
  }

  // ── Rendering ───────────────────────────────────────────────────────────

  /**
   * Renders a board as a labelled grid with score, moves, and multiplier.
   *
   * <p>Example output:
   * <pre>
   *    0  1  2  3  4  5  6  7
   * 0: ro go bo ro go bo ro go
   * 1: go bo ro go bo ro go bo
   * ...
   * Score: 150  Moves: 25  Multiplier: 2
   * </pre>
   *
   * @param board the board to render
   * @return a multi-line string
   */
  public static String render(CascadeBoard board) {
    List<String> rows = CascadeBoardUtils.serialiseBoard(board);
    StringBuilder sb = new StringBuilder();
    sb.append("   0  1  2  3  4  5  6  7\n");
    for (int y = 0; y < rows.size(); y++) {
      sb.append(y).append(": ").append(rows.get(y)).append('\n');
    }
    sb.append(String.format("Score: %d  Moves: %d  Multiplier: %d",
        board.getScore(), board.getMovesRemaining(), board.getMultiplier()));
    return sb.toString();
  }

  // ── Private helpers ─────────────────────────────────────────────────────

  private static List<String> pad(String... rows) {
    List<String> padded = new ArrayList<>(Arrays.asList(rows));
    while (padded.size() < CascadeBoard.HEIGHT) {
      padded.add(EMPTY_ROW);
    }
    return padded;
  }

  /**
   * Checks whether a single cell matches a token. Returns {@code null} on match, or a short
   * reason string on mismatch.
   */
  private static String checkCell(String token, CascadeCell cell) {
    // Empty
    if (token.equals("..")) {
      return cell.getType() == TileType.EMPTY ? null : "expected EMPTY";
    }
    // Stone
    if (token.equals("##")) {
      return cell.getType() == TileType.STONE ? null : "expected STONE";
    }
    // Wildcard: any occupied cell
    if (token.equals("??")) {
      return cell.getType() != TileType.EMPTY ? null : "expected any occupied tile";
    }
    // Wildcard: specific type, any colour (activation not checked)
    if (token.charAt(0) == '?') {
      TileType expected = typeFromChar(token.charAt(1));
      if (expected == null) {
        return "unrecognised wildcard type '" + token.charAt(1) + "'";
      }
      return cell.getType() == expected ? null : "expected " + expected + " (any colour)";
    }
    // Prism (exact, activation checked)
    if (token.equalsIgnoreCase("xx")) {
      if (cell.getType() != TileType.PRISM) {
        return "expected PRISM";
      }
      boolean wantActivated = token.equals("XX");
      return wantActivated == cell.isActivated() ? null
          : "activated mismatch (expected " + wantActivated + ")";
    }
    // Colour+type token — activation indicated by both characters being uppercase
    boolean wantActivated = Character.isUpperCase(token.charAt(0))
        && Character.isUpperCase(token.charAt(1));
    String lower = token.toLowerCase();
    TileColour wantColour = colourFromChar(lower.charAt(0));
    TileType wantType = typeFromChar(lower.charAt(1));
    if (wantColour == null) {
      return "unrecognised colour '" + lower.charAt(0) + "'";
    }
    if (wantType == null) {
      return "unrecognised type '" + lower.charAt(1) + "'";
    }
    if (cell.getType() != wantType) {
      return "type mismatch";
    }
    if (cell.getColour() != wantColour) {
      return "colour mismatch";
    }
    if (wantActivated != cell.isActivated()) {
      return "activated mismatch (expected " + wantActivated + ")";
    }
    return null;
  }

  private static TileType typeFromChar(char c) {
    return switch (c) {
      case 'o' -> TileType.STANDARD;
      case 'b' -> TileType.BOMB;
      case 'h' -> TileType.ROCKET_H;
      case 'v' -> TileType.ROCKET_V;
      case 'i' -> TileType.ICE;
      default -> null;
    };
  }

  private static TileColour colourFromChar(char c) {
    return switch (c) {
      case 'r' -> TileColour.RED;
      case 'b' -> TileColour.BLUE;
      case 'g' -> TileColour.GREEN;
      case 'y' -> TileColour.YELLOW;
      case 'p' -> TileColour.PURPLE;
      default -> null;
    };
  }

  private static String describeToken(String token) {
    if (token.equals("..")) {
      return "EMPTY";
    }
    if (token.equals("##")) {
      return "STONE";
    }
    if (token.equals("??")) {
      return "any occupied";
    }
    if (token.equalsIgnoreCase("xx")) {
      return token.equals("XX") ? "PRISM(activated)" : "PRISM";
    }
    if (token.charAt(0) == '?') {
      TileType type = typeFromChar(token.charAt(1));
      return type != null ? type + "(any colour)" : "unknown wildcard";
    }
    boolean activated = Character.isUpperCase(token.charAt(0))
        && Character.isUpperCase(token.charAt(1));
    String lower = token.toLowerCase();
    TileColour colour = colourFromChar(lower.charAt(0));
    TileType type = typeFromChar(lower.charAt(1));
    String desc = (colour != null ? colour.name() : "?") + " "
        + (type != null ? type.name() : "?");
    if (activated) {
      desc += "(activated)";
    }
    return desc;
  }

  private static String describeCell(CascadeCell cell) {
    if (cell.getType() == TileType.EMPTY) {
      return "EMPTY";
    }
    if (cell.getType() == TileType.STONE) {
      return "STONE";
    }
    if (cell.getType() == TileType.PRISM) {
      return cell.isActivated() ? "PRISM(activated)" : "PRISM";
    }
    String desc = cell.getColour().name() + " " + cell.getType().name();
    if (cell.isActivated()) {
      desc += "(activated)";
    }
    return desc;
  }

  /**
   * Renders the expected pattern and actual board side by side.
   */
  private static String renderComparison(List<String> expectedRows, CascadeBoard actual) {
    List<String> actualRows = CascadeBoardUtils.serialiseBoard(actual);
    String header = "   0  1  2  3  4  5  6  7";
    int colWidth = header.length();
    String gap = "     ";

    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%-" + colWidth + "s%s%s\n", "Expected:", gap, "Actual:"));
    sb.append(header).append(gap).append(header).append('\n');
    for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
      String expLine = y + ": " + expectedRows.get(y);
      String actLine = y + ": " + actualRows.get(y);
      sb.append(String.format("%-" + colWidth + "s%s%s\n", expLine, gap, actLine));
    }
    sb.append(String.format("%-" + colWidth + "s%sScore: %d  Moves: %d  Multiplier: %d\n",
        "", gap, actual.getScore(), actual.getMovesRemaining(), actual.getMultiplier()));
    return sb.toString();
  }
}