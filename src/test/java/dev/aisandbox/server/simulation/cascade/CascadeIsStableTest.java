/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

import static dev.aisandbox.server.simulation.cascade.CascadeBoardAssert.board;
import static org.junit.jupiter.api.Assertions.fail;

import dev.aisandbox.server.simulation.cascade.model.CascadeBoard;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
class CascadeIsStableTest {

  @BeforeAll
  static void setup() {
    CascadeTestReport.reset();
  }

  @AfterAll
  static void report() throws IOException {
    CascadeTestReport.writeReport("isStable.html", "Cascade isStable Tests");
  }

  private static void check(String name, CascadeBoard b, boolean expectStable) {
    boolean actual = CascadeBoardUtils.isStable(b);
    boolean passed = actual == expectStable;
    String expected = expectStable ? "STABLE" : "UNSTABLE";
    String error = passed ? null
        : "Expected " + expected + " but was " + (actual ? "STABLE" : "UNSTABLE");
    CascadeTestReport.recordException(name, passed, b, expected, error);
    if (!passed) {
      fail(error);
    }
  }

  // ── Category A: confirmed stable ───────────────────────────────────────

  @Test
  void stableFullBoardNoRuns() {
    // Full 8×8 board using a rotating r/g/b pattern — no run of 3 anywhere
    check("stableFullBoardNoRuns", board(
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    ), true);
  }

  @Test
  void stableTwoInARow() {
    // Max run is exactly 2 — not enough to trigger a match
    check("stableTwoInARow", board(
        "ro ro go go bo bo ro ro",
        "go go bo bo ro ro go go",
        "bo bo ro ro go go bo bo",
        "ro ro go go bo bo ro ro",
        "go go bo bo ro ro go go",
        "bo bo ro ro go go bo bo",
        "ro ro go go bo bo ro ro",
        "go go bo bo ro ro go go"
    ), true);
  }

  @Test
  void stableUnactivatedBomb() {
    // An unactivated bomb does not trigger instability
    check("stableUnactivatedBomb", board(
        "rb go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    ), true);
  }

  @Test
  void stableUnactivatedRocket() {
    // An unactivated rocket does not trigger instability
    check("stableUnactivatedRocket", board(
        "rh go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    ), true);
  }

  @Test
  void stableEmptySealedByStone() {
    // Empty row sealed above by a stone row — gravity cannot reach these cells
    check("stableEmptySealedByStone", board(
        "## ## ## ## ## ## ## ##",
        ".. .. .. .. .. .. .. ..",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro"
    ), true);
  }

  @Test
  void stableEmptySealedByIce() {
    // Empty row sealed above by an ice row — ice acts as a barrier
    check("stableEmptySealedByIce", board(
        "ri gi bi ri gi bi ri gi",
        ".. .. .. .. .. .. .. ..",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro"
    ), true);
  }

  @Test
  void stableMultipleEmptiesSealedByStone() {
    // Two empty rows below a stone row — all sealed; no fallable tiles between empty and stone
    check("stableMultipleEmptiesSealedByStone", board(
        "## ## ## ## ## ## ## ##",
        ".. .. .. .. .. .. .. ..",
        ".. .. .. .. .. .. .. ..",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    ), true);
  }

  // ── Category B: unstable — activated tiles ─────────────────────────────

  @Test
  void unstableActivatedBomb() {
    check("unstableActivatedBomb", board(
        "RB go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    ), false);
  }

  @Test
  void unstableActivatedRocketH() {
    check("unstableActivatedRocketH", board(
        "RH go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    ), false);
  }

  @Test
  void unstableActivatedRocketV() {
    check("unstableActivatedRocketV", board(
        "RV go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    ), false);
  }

  // ── Category C: unstable — horizontal runs ─────────────────────────────

  @Test
  void unstableHorizontalRunAtLeftEdge() {
    // 3-run of red starting at column 0
    check("unstableHorizontalRunAtLeftEdge", board(
        "ro ro ro go bo ro go bo",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    ), false);
  }

  @Test
  void unstableHorizontalRunOf3() {
    // 3-run of red in the middle of a row
    check("unstableHorizontalRunOf3", board(
        "go bo ro ro ro go bo ro",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    ), false);
  }

  @Test
  void unstableHorizontalRunOf5() {
    // 5-run of red across a row
    check("unstableHorizontalRunOf5", board(
        "ro ro ro ro ro go bo ro",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    ), false);
  }

  @Test
  void unstableHorizontalRunSplitByStone() {
    // Stone at col 3 breaks the full run, but both halves still have a run of 3
    check("unstableHorizontalRunSplitByStone", board(
        "ro ro ro ## ro ro ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    ), false);
  }

  // ── Category D: unstable — vertical runs ──────────────────────────────

  @Test
  void unstableVerticalRunAtTopEdge() {
    // 3-run of red starting at row 0 in column 0
    check("unstableVerticalRunAtTopEdge", board(
        "ro go bo ro go bo ro go",
        "ro bo ro go bo ro go bo",
        "ro ro go bo ro go bo ro",
        "go go bo ro go bo ro go",
        "bo bo ro go bo ro go bo",
        "ro ro go bo ro go bo ro",
        "go go bo ro go bo ro go",
        "bo bo ro go bo ro go bo"
    ), false);
  }

  @Test
  void unstableVerticalRunOf3() {
    // 3-run of red in the middle of column 0 (rows 3-5)
    check("unstableVerticalRunOf3", board(
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "ro bo ro go bo ro go bo",
        "ro ro go bo ro go bo ro",
        "go go bo ro go bo ro go",
        "bo bo ro go bo ro go bo"
    ), false);
  }

  @Test
  void unstableVerticalRunOf5() {
    // 5-run of red in column 0 (rows 0-4)
    check("unstableVerticalRunOf5", board(
        "ro go bo ro go bo ro go",
        "ro bo ro go bo ro go bo",
        "ro ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "ro bo ro go bo ro go bo",
        "go ro go bo ro go bo ro",
        "bo go bo ro go bo ro go",
        "ro bo ro go bo ro go bo"
    ), false);
  }

  @Test
  void unstableVerticalAndHorizontalRun() {
    // Column 4 has a vertical run of 3 (rows 2-4); row 4 has a horizontal run of 3 (cols 2-4)
    check("unstableVerticalAndHorizontalRun", board(
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro ro go bo go",
        "go bo ro ro ro go bo ro",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    ), false);
  }

  // ── Category E: unstable — unsettled empty cells ──────────────────────

  @Test
  void unstableEmptyBoardOpenToTop() {
    // Fully empty board — every cell at row 0 is open to the top
    check("unstableEmptyBoardOpenToTop", board(), false);
  }

  @Test
  void unstableEmptyAtTopOfColumn() {
    // Single empty cell at y=0 — directly open to the top
    check("unstableEmptyAtTopOfColumn", board(
        ".. go bo ro go bo ro go",
        "ro bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    ), false);
  }

  @Test
  void unstableEmptyWithFallableTileAbove() {
    // Col 0: stone at y=0, fallable tile at y=1, empty at y=2
    // Scanning up from y=2: fallable tile found at y=1 → unstable
    check("unstableEmptyWithFallableTileAbove", board(
        "## go bo ro go bo ro go",
        "ro bo ro go bo ro go bo",
        ".. ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    ), false);
  }

  @Test
  void unstableEmptyFallableBeforeStone() {
    // Col 0: stone at y=0, empty at y=1 (sealed), fallable at y=2, empty at y=3
    // Scanning up from y=3: y=2 is fallable — found before any stone → unstable
    // (stone at y=0 would seal, but fallable at y=2 is encountered first)
    check("unstableEmptyFallableBeforeStone", board(
        "## go bo ro go bo ro go",
        ".. bo ro go bo ro go bo",
        "ro ro go bo ro go bo ro",
        ".. go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    ), false);
  }
}
