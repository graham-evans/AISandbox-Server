/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

import static dev.aisandbox.server.simulation.cascade.CascadeBoardAssert.assertMatches;
import static dev.aisandbox.server.simulation.cascade.CascadeBoardAssert.board;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import dev.aisandbox.server.simulation.cascade.model.CascadeBoard;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CascadeBoardUtils#makeMove(CascadeBoard, int, int, int, int)}.
 *
 * <p>All boards use stone isolation (non-test rows filled with stone) so that gravity and refill
 * do not interfere with the area under test. Each test verifies the board layout, score, and
 * multiplier after the move.
 *
 * <p>Tests are grouped by the Make Move steps defined in {@code runtime.md}.
 */
public class CascadeMakeMoveTest {

  private static final String S = "## ## ## ## ## ## ## ##";

  @BeforeAll
  static void setup() {
    CascadeTestReport.reset();
  }

  @AfterAll
  static void report() throws IOException {
    CascadeTestReport.writeReport("makeMove.html", "Cascade makeMove Tests");
  }

  private static void recordAndAssert(String name, boolean passed, CascadeBoard input,
      long expectedScore, long expectedMultiplier, String[] expected, CascadeBoard actual,
      String error) {
    CascadeTestReport.record(name, passed, input, expected, expectedScore, expectedMultiplier,
        actual, error);
    if (!passed) {
      fail(error);
    }
  }

  private static void recordExceptionAndAssert(String name, boolean passed, CascadeBoard input,
      String expectedError, String error) {
    CascadeTestReport.recordException(name, passed, input, expectedError, error);
    if (!passed) {
      fail(error);
    }
  }

  // ── Step 1: Non-adjacent rejection ──────────────────────────────────────

  @Test
  void nonAdjacentThrows() {
    CascadeBoard b = board(
        "ro go bo ro go bo ro go",
        S, S, S, S, S, S, S
    );
    CascadeBoard snapshot = b.copy();
    boolean passed = false;
    String error = null;
    try {
      assertThrows(InvalidCascadeAction.class,
          () -> CascadeBoardUtils.makeMove(b, 0, 0, 2, 0));
      passed = true;
    } catch (AssertionError e) {
      error = e.getMessage();
    }
    recordExceptionAndAssert("nonAdjacentThrows", passed, snapshot,
        "InvalidCascadeAction (Manhattan distance != 1)", error);
  }

  // ── Step 2: Unswappable tile rejection ──────────────────────────────────

  @Test
  void swapWithIceThrows() {
    CascadeBoard b = board(
        "ro ri bo go yo po ro go",
        S, S, S, S, S, S, S
    );
    CascadeBoard snapshot = b.copy();
    boolean passed = false;
    String error = null;
    try {
      assertThrows(InvalidCascadeAction.class,
          () -> CascadeBoardUtils.makeMove(b, 0, 0, 1, 0));
      passed = true;
    } catch (AssertionError e) {
      error = e.getMessage();
    }
    recordExceptionAndAssert("swapWithIceThrows", passed, snapshot,
        "InvalidCascadeAction (ICE is not swappable)", error);
  }

  @Test
  void swapWithStoneThrows() {
    CascadeBoard b = board(
        "ro ## bo go yo po ro go",
        S, S, S, S, S, S, S
    );
    CascadeBoard snapshot = b.copy();
    boolean passed = false;
    String error = null;
    try {
      assertThrows(InvalidCascadeAction.class,
          () -> CascadeBoardUtils.makeMove(b, 0, 0, 1, 0));
      passed = true;
    } catch (AssertionError e) {
      error = e.getMessage();
    }
    recordExceptionAndAssert("swapWithStoneThrows", passed, snapshot,
        "InvalidCascadeAction (STONE is not swappable)", error);
  }

  @Test
  void swapWithEmptyThrows() {
    CascadeBoard b = board(
        "ro .. bo go yo po ro go",
        S, S, S, S, S, S, S
    );
    CascadeBoard snapshot = b.copy();
    boolean passed = false;
    String error = null;
    try {
      assertThrows(InvalidCascadeAction.class,
          () -> CascadeBoardUtils.makeMove(b, 0, 0, 1, 0));
      passed = true;
    } catch (AssertionError e) {
      error = e.getMessage();
    }
    recordExceptionAndAssert("swapWithEmptyThrows", passed, snapshot,
        "InvalidCascadeAction (EMPTY is not swappable)", error);
  }

  // ── Step 3: Prism + Prism ──────────────────────────────────────────────

  @Test
  void prismPlusPrism() {
    // Two prisms at (3,0) and (4,0). Non-stone, non-empty tiles should all be cleared.
    // Row 0 has 6 non-prism occupied tiles + 2 prisms = 8 tiles. All 8 become EMPTY.
    // Score = 8 * 10 * 1 = 80, multiplier doubled to 2.
    CascadeBoard b = board(
        "ro go bo xx xx bo go ro",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        ".. .. .. .. .. .. .. ..",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 3, 0, 4, 0);
      assertMatches(result, 80L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("prismPlusPrism", passed, snapshot, 80L, 2L, expected, result, error);
  }

  // ── Step 4: Prism + Special ────────────────────────────────────────────

  @Test
  void prismPlusBomb() {
    // Prism at (0,0), red bomb at (1,0). Row also has red standard tiles and a red ice.
    // Effect: prism cell (0,0) becomes activated red bomb, original bomb (1,0) becomes EMPTY.
    // All red STANDARD tiles become activated red bombs.
    // Red ice at (5,0) unfreezes to red standard.
    // Existing red rocket at (6,0) gets activated.
    // Score = (converted_standard + 1) * 10 where +1 is the prism cell.
    // Converted standard: (2,0)=ro and (4,0)=ro = 2 tiles.  Score = (2+1)*10 = 30.
    CascadeBoard b = board(
        "xx rb ro go ro ri rh bo",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "RB .. RB go RB ro RH bo",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 0, 0, 1, 0);
      assertMatches(result, 30L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("prismPlusBomb", passed, snapshot, 30L, 1L, expected, result, error);
  }

  @Test
  void prismPlusRocketH() {
    // Prism at (0,0), red ROCKET_H at (1,0). Red standard tiles at (2,0) and (4,0).
    // Effect: prism becomes activated red ROCKET_H, rocket(1,0) -> EMPTY.
    // Red standard tiles become activated red ROCKET_H.
    // Score = (2 converted + 1) * 10 = 30. Multiplier unchanged.
    CascadeBoard b = board(
        "xx rh ro go ro bo go po",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "RH .. RH go RH bo go po",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 0, 0, 1, 0);
      assertMatches(result, 30L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("prismPlusRocketH", passed, snapshot, 30L, 1L, expected, result, error);
  }

  @Test
  void prismPlusRocketV() {
    // Prism at (0,0), red ROCKET_V at (1,0). Red standard tiles at (2,0) and (4,0).
    // Score = (2 converted + 1) * 10 = 30. Multiplier unchanged.
    CascadeBoard b = board(
        "xx rv ro go ro bo go po",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "RV .. RV go RV bo go po",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 0, 0, 1, 0);
      assertMatches(result, 30L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("prismPlusRocketV", passed, snapshot, 30L, 1L, expected, result, error);
  }

  // ── Step 5: Prism + Standard ───────────────────────────────────────────

  @Test
  void prismPlusStandard() {
    // Prism at (0,0), red standard at (1,0). Other reds at (3,0) and (5,0).
    // Red bomb at (6,0) should be activated. Red ice at (7,0) should unfreeze.
    // Count of red STANDARD destroyed: (1,0), (3,0), (5,0) = 3.
    // Score = 3 * 10 * 1 = 30, multiplier stays at 1.
    CascadeBoard b = board(
        "xx ro go ro go ro rb ri",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        ".. .. go .. go .. RB ro",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 0, 0, 1, 0);
      assertMatches(result, 30L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("prismPlusStandard", passed, snapshot, 30L, 1L, expected, result, error);
  }

  @Test
  void prismPlusStandardWithIce() {
    // Verify ICE tiles of matching colour unfreeze to STANDARD (not destroyed).
    // Prism at (0,0), red standard at (1,0). Red ice at (3,0) and (5,0).
    // Only the red STANDARD at (1,0) is destroyed. Score = 1 * 10 = 10. Multiplier stays.
    CascadeBoard b = board(
        "xx ro go ri go ri bo po",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        ".. .. go ro go ro bo po",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 0, 0, 1, 0);
      assertMatches(result, 10L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("prismPlusStandardWithIce", passed, snapshot, 10L, 1L, expected, result,
        error);
  }

  // ── Step 6: Bomb + Bomb (5x5 area) ────────────────────────────────────

  @Test
  void bombPlusBomb() {
    // Red bombs at (3,2) and (4,2). 5x5 area centred on each: covers a wide area.
    // All non-empty cells in the union of both 5x5 areas are destroyed.
    // Use a board with tiles in rows 0-4 (rest stone) to contain the blast.
    CascadeBoard b = board(
        "go bo go go go go bo go",
        "bo go bo go go bo go bo",
        "go bo go rb rb go bo go",
        "bo go bo go go bo go bo",
        "go bo go go go go bo go",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // 5x5 centred on (3,2): x=1..5, y=0..4
    // 5x5 centred on (4,2): x=2..6, y=0..4
    // Union: x=1..6, y=0..4. Edges (0,*) and (7,*) survive.
    // Tiles destroyed: 6 columns x 5 rows = 30. Score = 30 * 10 * 1 = 300.
    String[] expected = {
        "go .. .. .. .. .. .. go",
        "bo .. .. .. .. .. .. bo",
        "go .. .. .. .. .. .. go",
        "bo .. .. .. .. .. .. bo",
        "go .. .. .. .. .. .. go",
        S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 3, 2, 4, 2);
      assertMatches(result, 300L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("bombPlusBomb", passed, snapshot, 300L, 1L, expected, result, error);
  }

  @Test
  void bombPlusBombHitsStone() {
    // Bombs at (3,1) and (4,1). Stone at (3,3) within 5x5 area — should be destroyed.
    // Union: x=1..6, y=0..3. 24 tiles including the stone. Score = 24 * 10 = 240.
    CascadeBoard b = board(
        "go bo go go go go bo go",
        "bo go go rb rb go go bo",
        "go bo go go go go bo go",
        "bo go go ## go go go bo",
        S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "go .. .. .. .. .. .. go",
        "bo .. .. .. .. .. .. bo",
        "go .. .. .. .. .. .. go",
        "bo .. .. .. .. .. .. bo",
        S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 3, 1, 4, 1);
      assertMatches(result, 240L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("bombPlusBombHitsStone", passed, snapshot, 240L, 1L, expected, result, error);
  }

  @Test
  void bombPlusBombHitsPrism() {
    // Bombs at (3,2) and (4,2). Prism at (2,2) within 5x5 area.
    // Prism triggers using bomb's colour (red) — removes all red STANDARD tiles on board.
    // Red standard at (0,0) should be destroyed by prism effect.
    // Union: x=1..6, y=0..4 = 30 tiles (including prism). Prism effect: +1 (0,0).
    // Score = 31 * 10 = 310.
    CascadeBoard b = board(
        "ro go bo go go bo go bo",
        "go bo go go go go bo go",
        "bo go xx rb rb go go bo",
        "go bo go go go go bo go",
        "bo go bo go go bo go bo",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // 5x5 union covers x=1..6, y=0..4. Plus prism effect kills red STANDARD at (0,0).
    String[] expected = {
        ".. .. .. .. .. .. .. bo",
        "go .. .. .. .. .. .. go",
        "bo .. .. .. .. .. .. bo",
        "go .. .. .. .. .. .. go",
        "bo .. .. .. .. .. .. bo",
        S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 3, 2, 4, 2);
      assertMatches(result, 310L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("bombPlusBombHitsPrism", passed, snapshot, 310L, 1L, expected, result, error);
  }

  @Test
  void bombPlusBombHitsPrismHitsBomb() {
    // Bombs at (3,2) and (4,2). Prism at (2,2) within 5x5 area.
    // Prism triggers using bomb's colour (red) — activates all red BOMB tiles on board.
    // Red bomb at (0,0) should be activated by prism effect.
    // Union: x=1..6, y=0..4 = 30 tiles (including prism). Prism effect: +1 (0,0).
    // Score = 31 * 10 = 310.
    CascadeBoard b = board(
            "rb go bo go go bo go bo",
            "go bo go go go go bo go",
            "bo go xx rb rb go go bo",
            "go bo go go go go bo go",
            "bo go bo go go bo go bo",
            S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // 5x5 union covers x=1..6, y=0..4. Plus prism effect kills red STANDARD at (0,0).
    String[] expected = {
            "RB .. .. .. .. .. .. bo",
            "go .. .. .. .. .. .. go",
            "bo .. .. .. .. .. .. bo",
            "go .. .. .. .. .. .. go",
            "bo .. .. .. .. .. .. bo",
            S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 3, 2, 4, 2);
      assertMatches(result, 300L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("bombPlusBombHitsPrismHitsBomb", passed, snapshot, 300L, 1L, expected, result, error);
  }


  @Test
  void bombPlusBombHitsSpecial() {
    // Bombs at (3,2) and (4,2). Green bomb at (5,2) within 5x5 area.
    // The green bomb should be marked activated (not destroyed).
    // Union 30 tiles minus green bomb (activated, not EMPTY) = 29 tiles. Score = 290.
    CascadeBoard b = board(
        "go bo go go go go bo go",
        "bo go bo go go bo go bo",
        "go bo go rb rb gb bo go",
        "bo go bo go go bo go bo",
        "go bo go go go go bo go",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Union x=1..6 y=0..4. Green bomb at (5,2) is activated not destroyed.
    String[] expected = {
        "go .. .. .. .. .. .. go",
        "bo .. .. .. .. .. .. bo",
        "go .. .. .. .. GB .. go",
        "bo .. .. .. .. .. .. bo",
        "go .. .. .. .. .. .. go",
        S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 3, 2, 4, 2);
      assertMatches(result, 290L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("bombPlusBombHitsSpecial", passed, snapshot, 290L, 1L, expected, result,
        error);
  }

  // ── Step 7: Bomb + Rocket (cross pattern) ──────────────────────────────

  @Test
  void bombPlusRocket() {
    // Red bomb at (3,3), red rocket_h at (4,3). Fire in 4 cardinal directions from bomb's pos.
    // Diagonals of bomb: (2,2),(4,2),(2,4),(4,4) also destroyed.
    // Cardinals: column 3 + row 3 cleared. Down fires into stone row at (3,7): stone destroyed.
    // Score: 2(removed) + 4(diags) + 3(up) + 4(down incl stone) + 3(left) + 3(right) = 19 -> 190.
    CascadeBoard b = board(
        "go bo go go go go bo go",
        "bo go bo go bo go go bo",
        "go bo go go go go bo go",
        "bo go bo rb rh go go bo",
        "go bo go go go go bo go",
        "bo go bo go bo go go bo",
        "go bo go go go go bo go",
        S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Cardinals from bomb (3,3): entire column 3 and row 3 cleared.
    // Diagonals of bomb (3,3): (2,2),(4,2),(2,4),(4,4) also destroyed.
    // Down cardinal hits (3,7)=stone, destroys it and stops.
    String[] expected = {
        "go bo go .. go go bo go",
        "bo go bo .. bo go go bo",
        "go bo .. .. .. go bo go",   // (2,2) and (4,2) diagonals destroyed
        ".. .. .. .. .. .. .. ..",
        "go bo .. .. .. go bo go",   // (2,4) and (4,4) diagonals destroyed
        "bo go bo .. bo go go bo",
        "go bo go .. go go bo go",
        "## ## ## .. ## ## ## ##"    // (3,7) stone destroyed by downward cardinal
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 3, 3, 4, 3);
      assertMatches(result, 190L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("bombPlusRocket", passed, snapshot, 190L, 1L, expected, result, error);
  }

  @Test
  void bombPlusRocketHitsStone() {
    // Bomb at (3,2), rocket at (4,2). Stone at (3,0) — cross going up from (3,2) hits stone,
    // destroys it, then stops.
    // Also fires down from (3,2): (3,3),(3,4) clear, then (3,5)=stone destroyed, stops.
    // Score: 2 + 4(diags) + 2(up incl stone) + 3(down incl stone) + 3(left) + 3(right) = 17 -> 170.
    CascadeBoard b = board(
        "go bo go ## go go bo go",
        "bo go bo go bo go go bo",
        "go bo go rb rh go go bo",
        "bo go bo go go go go bo",
        "go bo go go go go bo go",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Diagonals of (3,2): (2,1),(4,1),(2,3),(4,3) destroyed.
    // Up: (3,1) destroyed, (3,0)=## stone destroyed -> STOP.
    // Down: (3,3),(3,4) destroyed, (3,5)=## stone destroyed -> STOP.
    // Left: (2,2),(1,2),(0,2) destroyed. Right: (5,2),(6,2),(7,2) destroyed.
    String[] expected = {
        "go bo go .. go go bo go",
        "bo go .. .. .. go go bo",   // (2,1) diag, (3,1) up, (4,1) diag destroyed
        ".. .. .. .. .. .. .. ..",
        "bo go .. .. .. go go bo",   // (2,3) diag, (3,3) down, (4,3) diag destroyed
        "go bo go .. go go bo go",
        "## ## ## .. ## ## ## ##",   // (3,5) stone destroyed by downward cardinal
        S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 3, 2, 4, 2);
      assertMatches(result, 170L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("bombPlusRocketHitsStone", passed, snapshot, 170L, 1L, expected, result,
        error);
  }

  @Test
  void bombPlusRocketHitsPrism() {
    // Bomb at (3,3), rocket at (4,3). Prism at (3,1) in cross path.
    // Prism triggers with bomb's colour (red). Red standard at (0,0) destroyed by prism effect.
    // Down cardinal hits (3,5)=stone, destroys it and stops.
    // Score: 2 + 4(diags) + 3(up:3,2+prism+3,0) + 2(down:3,4+stone) + 3(left) + 3(right)
    //        + 1(prism effect on 0,0) = 18 -> 180.
    CascadeBoard b = board(
        "ro bo go go go go bo go",
        "bo go bo xx bo go go bo",
        "go bo go go go go bo go",
        "bo go bo rb rh go go bo",
        "go bo go go go go bo go",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Diagonals of (3,3): (2,2),(4,2),(2,4),(4,4) destroyed.
    // Up: (3,2) destroyed, (3,1)=prism destroyed + prism effect (red), continues: (3,0) destroyed.
    // Down: (3,4) destroyed, (3,5)=stone destroyed -> STOP.
    // Prism effect: red standard at (0,0) destroyed.
    String[] expected = {
        ".. bo go .. go go bo go",   // (0,0) red destroyed by prism, (3,0) destroyed by cross
        "bo go bo .. bo go go bo",   // (3,1) prism destroyed
        "go bo .. .. .. go bo go",   // (2,2) and (4,2) diagonals + (3,2) cardinal destroyed
        ".. .. .. .. .. .. .. ..",
        "go bo .. .. .. go bo go",   // (2,4) and (4,4) diagonals + (3,4) cardinal destroyed
        "## ## ## .. ## ## ## ##",   // (3,5) stone destroyed by downward cardinal
        S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 3, 3, 4, 3);
      assertMatches(result, 180L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("bombPlusRocketHitsPrism", passed, snapshot, 180L, 1L, expected, result,
        error);
  }

  // ── Step 8: Rocket + Rocket ────────────────────────────────────────────

  @Test
  void rocketHPlusRocketH() {
    // Two horizontal rockets at (3,3) and (4,3). Each fires along its row AND column.
    // Columns 3 and 4 fire down into stone row: (3,7) and (4,7) destroyed.
    // Score: 2(removed) + 6(row3) + 3(col3up) + 4(col3down incl stone)
    //        + 3(col4up) + 4(col4down incl stone) = 22 -> 220.
    CascadeBoard b = board(
        "go bo go go go go bo go",
        "bo go bo go bo go go bo",
        "go bo go go go go bo go",
        "bo go bo rh rh go go bo",
        "go bo go go go go bo go",
        "bo go bo go bo go go bo",
        "go bo go go go go bo go",
        S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Rocket at (3,3) fires row 3 + column 3. Rocket at (4,3) fires row 3 + column 4.
    // Down firing from col3 hits (3,7)=stone; down firing from col4 hits (4,7)=stone.
    String[] expected = {
        "go bo go .. .. go bo go",
        "bo go bo .. .. go go bo",
        "go bo go .. .. go bo go",
        ".. .. .. .. .. .. .. ..",
        "go bo go .. .. go bo go",
        "bo go bo .. .. go go bo",
        "go bo go .. .. go bo go",
        "## ## ## .. .. ## ## ##"    // (3,7) and (4,7) stones destroyed
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 3, 3, 4, 3);
      assertMatches(result, 220L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("rocketHPlusRocketH", passed, snapshot, 220L, 1L, expected, result, error);
  }

  @Test
  void rocketVPlusRocketV() {
    // Two vertical rockets at (3,3) and (4,3). Same cross pattern as H+H.
    // Score = 220.
    CascadeBoard b = board(
        "go bo go go go go bo go",
        "bo go bo go bo go go bo",
        "go bo go go go go bo go",
        "bo go bo rv rv go go bo",
        "go bo go go go go bo go",
        "bo go bo go bo go go bo",
        "go bo go go go go bo go",
        S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "go bo go .. .. go bo go",
        "bo go bo .. .. go go bo",
        "go bo go .. .. go bo go",
        ".. .. .. .. .. .. .. ..",
        "go bo go .. .. go bo go",
        "bo go bo .. .. go go bo",
        "go bo go .. .. go bo go",
        "## ## ## .. .. ## ## ##"    // (3,7) and (4,7) stones destroyed
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 3, 3, 4, 3);
      assertMatches(result, 220L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("rocketVPlusRocketV", passed, snapshot, 220L, 1L, expected, result, error);
  }

  @Test
  void rocketHPlusRocketV() {
    // Mixed rockets: H at (3,3), V at (4,3). Same cross pattern.
    // Score = 220.
    CascadeBoard b = board(
        "go bo go go go go bo go",
        "bo go bo go bo go go bo",
        "go bo go go go go bo go",
        "bo go bo rh rv go go bo",
        "go bo go go go go bo go",
        "bo go bo go bo go go bo",
        "go bo go go go go bo go",
        S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "go bo go .. .. go bo go",
        "bo go bo .. .. go go bo",
        "go bo go .. .. go bo go",
        ".. .. .. .. .. .. .. ..",
        "go bo go .. .. go bo go",
        "bo go bo .. .. go go bo",
        "go bo go .. .. go bo go",
        "## ## ## .. .. ## ## ##"    // (3,7) and (4,7) stones destroyed
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 3, 3, 4, 3);
      assertMatches(result, 220L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("rocketHPlusRocketV", passed, snapshot, 220L, 1L, expected, result, error);
  }

  @Test
  void doubleRocketHitsStone() {
    // Two rockets at (3,3) and (4,3). Stone at (3,1) — rocket going up from (3,3) hits stone,
    // destroys it, stops in that direction.
    // Col3 down also hits (3,5)=stone, destroys it, stops. Col4 down hits (4,5)=stone.
    // Score: 2(removed) + 6(row3) + 2(col3up) + 2(col3down) + 3(col4up) + 2(col4down) = 17 -> 170.
    CascadeBoard b = board(
        "go bo go go go go bo go",
        "bo go bo ## bo go go bo",
        "go bo go go go go bo go",
        "bo go bo rh rv go go bo",
        "go bo go go go go bo go",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Rocket at (3,3) up: (3,2) destroyed, (3,1) stone destroyed -> STOP.
    // (3,0) survives because stone stopped propagation.
    String[] expected = {
        "go bo go go .. go bo go",
        "bo go bo .. .. go go bo",
        "go bo go .. .. go bo go",
        ".. .. .. .. .. .. .. ..",
        "go bo go .. .. go bo go",
        "## ## ## .. .. ## ## ##",   // (3,5) and (4,5) stones destroyed
        S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 3, 3, 4, 3);
      assertMatches(result, 170L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("doubleRocketHitsStone", passed, snapshot, 170L, 1L, expected, result, error);
  }

  @Test
  void doubleRocketHitsPrism() {
    // Two rockets at (3,3) and (4,3). Prism at (3,1) in column path.
    // Prism triggers with rocket's colour (red). Red standard at (0,0) destroyed by prism effect.
    // Col3 down hits (3,5)=stone, destroys it. Col4 down hits (4,5)=stone, destroys it.
    // Score: 2 + 6(row3) + 3(col3up:3,2+prism+3,0) + 2(col3down) + 3(col4up) + 2(col4down)
    //        + 1(prism effect on 0,0) = 19 -> 190.
    CascadeBoard b = board(
        "ro bo go go go go bo go",
        "bo go bo xx bo go go bo",
        "go bo go go go go bo go",
        "bo go bo rh rv go go bo",
        "go bo go go go go bo go",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Prism at (3,1) hit by rocket going up. Triggers prism with red colour.
    // Red standard at (0,0) destroyed.
    String[] expected = {
        ".. bo go .. .. go bo go",
        "bo go bo .. .. go go bo",
        "go bo go .. .. go bo go",
        ".. .. .. .. .. .. .. ..",
        "go bo go .. .. go bo go",
        "## ## ## .. .. ## ## ##",   // (3,5) and (4,5) stones destroyed
        S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 3, 3, 4, 3);
      assertMatches(result, 190L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("doubleRocketHitsPrism", passed, snapshot, 190L, 1L, expected, result, error);
  }

  // ── Steps 9-11: Normal swap ────────────────────────────────────────────

  @Test
  void normalSwapCreatesHorizontalMatch() {
    // Swap (2,0) and (3,0): moves ro to (2,0), go to (3,0).
    // Result: ro ro ro at positions 0,1,2 — a match of three (resolved by updateBoard later).
    // Score = 0 (normal swap does not score immediately). Multiplier unchanged.
    CascadeBoard b = board(
        "ro ro go ro go bo ro go",
        S, S, S, S, S, S, S
    );
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "ro ro ro go go bo ro go",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 2, 0, 3, 0);
      assertMatches(result, 0L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("normalSwapCreatesHorizontalMatch", passed, snapshot, 0L, 1L, expected, result,
        error);
  }

  @Test
  void normalSwapCreatesVerticalMatch() {
    // Column 0: ro, go, ro, ro. Swap (0,0) and (0,1): go goes to (0,0), ro goes to (0,1).
    // Result: column 0 is go, ro, ro, ro — vertical match of three at y=1,2,3.
    // Score = 0. Multiplier unchanged.
    CascadeBoard b = board(
        "ro bo go yo po bo go yo",
        "go bo go yo po bo go yo",
        "ro bo go yo po bo go yo",
        "ro bo go yo po bo go yo",
        S, S, S, S
    );
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "go bo go yo po bo go yo",
        "ro bo go yo po bo go yo",
        "ro bo go yo po bo go yo",
        "ro bo go yo po bo go yo",
        S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 0, 0, 0, 1);
      assertMatches(result, 0L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("normalSwapCreatesVerticalMatch", passed, snapshot, 0L, 1L, expected, result,
        error);
  }

  @Test
  void normalSwapNoMatchThrows() {
    // Swap (0,0) and (1,0): ro <-> go. Neither position creates a run of 3.
    CascadeBoard b = board(
        "ro go bo yo po ro go bo",
        S, S, S, S, S, S, S
    );
    CascadeBoard snapshot = b.copy();
    boolean passed = false;
    String error = null;
    try {
      assertThrows(InvalidCascadeAction.class,
          () -> CascadeBoardUtils.makeMove(b, 0, 0, 1, 0));
      passed = true;
    } catch (AssertionError e) {
      error = e.getMessage();
    }
    recordExceptionAndAssert("normalSwapNoMatchThrows", passed, snapshot,
        "InvalidCascadeAction (swap creates no match)", error);
  }

  // ── Score/multiplier tests ─────────────────────────────────────────────

  @Test
  void prismPlusStandardKeepsMultiplier() {
    // Start with multiplier=2. After prism+standard, multiplier should be 2.
    // 2 red standard tiles. Score = 2 * 10 * 2 = 40. Multiplier = 2.
    CascadeBoard b = board(
        "xx ro go ro go bo go po",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(2);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        ".. .. go .. go bo go po",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.makeMove(b, 0, 0, 1, 0);
      assertMatches(result, 40L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("prismPlusStandardDoublesMultiplier", passed, snapshot, 40L, 2L, expected,
        result, error);
  }
}
