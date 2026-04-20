/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.aisandbox.server.simulation.cascade.model.CascadeBoard;
import dev.aisandbox.server.simulation.cascade.model.CascadeCell;
import dev.aisandbox.server.simulation.cascade.model.TileColour;
import dev.aisandbox.server.simulation.cascade.model.TileType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link CascadeBoardUtils#serialiseBoard(CascadeBoard)} and
 * {@link CascadeBoardUtils#deserialiseBoard(CascadeBoard, List)}.
 */
public class CascadeSerialisationTest {

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /** Serialises a single cell placed at (0,0) and returns its token string. */
  private static String tokenFor(CascadeCell cell) {
    CascadeBoard board = new CascadeBoard();
    board.setCell(0, 0, cell);
    return CascadeBoardUtils.serialiseBoard(board).get(0).split(" ")[0];
  }

  /** Builds a full set of rows with {@code token} at (0,0) and {@code ..} everywhere else. */
  private static List<String> rowsWithToken(String token) {
    String emptyRow = ".. .. .. .. .. .. .. ..";
    StringBuilder first = new StringBuilder(token);
    for (int i = 1; i < CascadeBoard.WIDTH; i++) {
      first.append(" ..");
    }
    List<String> rows = new ArrayList<>();
    rows.add(first.toString());
    for (int y = 1; y < CascadeBoard.HEIGHT; y++) {
      rows.add(emptyRow);
    }
    return rows;
  }

  /** Deserialises a single token at (0,0) and returns the resulting cell. */
  private static CascadeCell cellFrom(String token) {
    CascadeBoard board = new CascadeBoard();
    CascadeBoardUtils.deserialiseBoard(board, rowsWithToken(token));
    return board.getCell(0, 0);
  }

  // ---------------------------------------------------------------------------
  // Serialise: cell → token
  // ---------------------------------------------------------------------------

  static Stream<Arguments> cellToToken() {
    CascadeCell activatedStandard = CascadeCell.standard(TileColour.RED);
    activatedStandard.setActivated(true);
    CascadeCell activatedBomb = CascadeCell.bomb(TileColour.GREEN);
    activatedBomb.setActivated(true);
    CascadeCell activatedRocketH = CascadeCell.rocketH(TileColour.BLUE);
    activatedRocketH.setActivated(true);
    CascadeCell activatedRocketV = CascadeCell.rocketV(TileColour.YELLOW);
    activatedRocketV.setActivated(true);
    CascadeCell activatedPrism = CascadeCell.prism();
    activatedPrism.setActivated(true);

    return Stream.of(
        // Fixed tokens
        Arguments.of(CascadeCell.empty(),                     ".."),
        Arguments.of(CascadeCell.stone(),                     "##"),
        Arguments.of(CascadeCell.prism(),                     "xx"),
        // Standard tiles — all five colours
        Arguments.of(CascadeCell.standard(TileColour.RED),    "ro"),
        Arguments.of(CascadeCell.standard(TileColour.BLUE),   "bo"),
        Arguments.of(CascadeCell.standard(TileColour.GREEN),  "go"),
        Arguments.of(CascadeCell.standard(TileColour.YELLOW), "yo"),
        Arguments.of(CascadeCell.standard(TileColour.PURPLE), "po"),
        // Specials
        Arguments.of(CascadeCell.bomb(TileColour.GREEN),      "gb"),
        Arguments.of(CascadeCell.rocketH(TileColour.BLUE),    "bh"),
        Arguments.of(CascadeCell.rocketV(TileColour.RED),     "rv"),
        Arguments.of(CascadeCell.ice(TileColour.YELLOW),      "yi"),
        // Activated cells
        Arguments.of(activatedStandard,  "RO"),
        Arguments.of(activatedBomb,      "GB"),
        Arguments.of(activatedRocketH,   "BH"),
        Arguments.of(activatedRocketV,   "YV"),
        Arguments.of(activatedPrism,     "XX")
    );
  }

  @ParameterizedTest(name = "{0} -> \"{1}\"")
  @MethodSource("cellToToken")
  void cellSerialisesToExpectedToken(CascadeCell cell, String expectedToken) {
    assertEquals(expectedToken, tokenFor(cell));
  }

  // ---------------------------------------------------------------------------
  // Deserialise: token → cell
  // ---------------------------------------------------------------------------

  static Stream<Arguments> tokenToCell() {
    return Stream.of(
        // Fixed tokens
        Arguments.of("..",  TileType.EMPTY,    TileColour.NONE,   false),
        Arguments.of("##",  TileType.STONE,    TileColour.NONE,   false),
        Arguments.of("xx",  TileType.PRISM,    TileColour.NONE,   false),
        Arguments.of("XX",  TileType.PRISM,    TileColour.NONE,   true),
        // Standard tiles — all five colours
        Arguments.of("ro",  TileType.STANDARD, TileColour.RED,    false),
        Arguments.of("bo",  TileType.STANDARD, TileColour.BLUE,   false),
        Arguments.of("go",  TileType.STANDARD, TileColour.GREEN,  false),
        Arguments.of("yo",  TileType.STANDARD, TileColour.YELLOW, false),
        Arguments.of("po",  TileType.STANDARD, TileColour.PURPLE, false),
        // Specials
        Arguments.of("gb",  TileType.BOMB,     TileColour.GREEN,  false),
        Arguments.of("bh",  TileType.ROCKET_H, TileColour.BLUE,   false),
        Arguments.of("rv",  TileType.ROCKET_V, TileColour.RED,    false),
        Arguments.of("yi",  TileType.ICE,      TileColour.YELLOW, false),
        // Activated cells
        Arguments.of("RO",  TileType.STANDARD, TileColour.RED,    true),
        Arguments.of("GB",  TileType.BOMB,     TileColour.GREEN,  true),
        Arguments.of("BH",  TileType.ROCKET_H, TileColour.BLUE,   true),
        Arguments.of("YV",  TileType.ROCKET_V, TileColour.YELLOW, true)
    );
  }

  @ParameterizedTest(name = "\"{0}\" -> {1} / {2} / activated={3}")
  @MethodSource("tokenToCell")
  void tokenDeserialisesToExpectedCell(String token, TileType expectedType,
      TileColour expectedColour, boolean expectedActivated) {
    CascadeCell cell = cellFrom(token);
    assertEquals(expectedType,      cell.getType(),      "type");
    assertEquals(expectedColour,    cell.getColour(),    "colour");
    assertEquals(expectedActivated, cell.isActivated(),  "activated");
  }

  // ---------------------------------------------------------------------------
  // Round-trip
  // ---------------------------------------------------------------------------

  @Test
  void fullBoardRoundTrip() {
    CascadeBoard original = new CascadeBoard();
    // Row 0: one of each standard colour, then bomb, rocket-H, rocket-V
    original.setCell(0, 0, CascadeCell.standard(TileColour.RED));
    original.setCell(1, 0, CascadeCell.standard(TileColour.BLUE));
    original.setCell(2, 0, CascadeCell.standard(TileColour.GREEN));
    original.setCell(3, 0, CascadeCell.standard(TileColour.YELLOW));
    original.setCell(4, 0, CascadeCell.standard(TileColour.PURPLE));
    original.setCell(5, 0, CascadeCell.bomb(TileColour.RED));
    original.setCell(6, 0, CascadeCell.rocketH(TileColour.BLUE));
    original.setCell(7, 0, CascadeCell.rocketV(TileColour.GREEN));
    // Row 1: ice, stone, prism, empty, then activated variants
    original.setCell(0, 1, CascadeCell.ice(TileColour.YELLOW));
    original.setCell(1, 1, CascadeCell.stone());
    original.setCell(2, 1, CascadeCell.prism());
    original.setCell(3, 1, CascadeCell.empty());
    CascadeCell activatedStandard = CascadeCell.standard(TileColour.PURPLE);
    activatedStandard.setActivated(true);
    original.setCell(4, 1, activatedStandard);
    CascadeCell activatedBomb = CascadeCell.bomb(TileColour.RED);
    activatedBomb.setActivated(true);
    original.setCell(5, 1, activatedBomb);
    CascadeCell activatedRocketH = CascadeCell.rocketH(TileColour.BLUE);
    activatedRocketH.setActivated(true);
    original.setCell(6, 1, activatedRocketH);
    CascadeCell activatedPrism = CascadeCell.prism();
    activatedPrism.setActivated(true);
    original.setCell(7, 1, activatedPrism);

    List<String> serialised = CascadeBoardUtils.serialiseBoard(original);
    CascadeBoard restored = new CascadeBoard();
    CascadeBoardUtils.deserialiseBoard(restored, serialised);

    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        CascadeCell orig = original.getCell(x, y);
        CascadeCell rest = restored.getCell(x, y);
        String pos = "(" + x + "," + y + ")";
        assertEquals(orig.getType(),     rest.getType(),     "type at "      + pos);
        assertEquals(orig.getColour(),   rest.getColour(),   "colour at "    + pos);
        assertEquals(orig.isActivated(), rest.isActivated(), "activated at " + pos);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Error cases
  // ---------------------------------------------------------------------------

  @Test
  void wrongRowCountThrows() {
    List<String> rows = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      rows.add(".. .. .. .. .. .. .. ..");
    }
    assertThrows(IllegalArgumentException.class,
        () -> CascadeBoardUtils.deserialiseBoard(new CascadeBoard(), rows));
  }

  @Test
  void wrongTokenCountThrows() {
    List<String> rows = new ArrayList<>();
    rows.add(".. .. .. .. .. .. ..");           // 7 tokens, not 8
    for (int y = 1; y < CascadeBoard.HEIGHT; y++) {
      rows.add(".. .. .. .. .. .. .. ..");
    }
    assertThrows(IllegalArgumentException.class,
        () -> CascadeBoardUtils.deserialiseBoard(new CascadeBoard(), rows));
  }

  @ParameterizedTest(name = "token \"{0}\" is invalid")
  @ValueSource(strings = {"zz", "rz", "r", "roo", "q5"})
  void invalidTokenThrows(String badToken) {
    assertThrows(IllegalArgumentException.class,
        () -> CascadeBoardUtils.deserialiseBoard(new CascadeBoard(), rowsWithToken(badToken)));
  }
}
