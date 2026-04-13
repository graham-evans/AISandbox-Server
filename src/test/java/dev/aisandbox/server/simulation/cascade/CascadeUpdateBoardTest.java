/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

import static dev.aisandbox.server.simulation.cascade.CascadeBoardAssert.assertMatches;
import static dev.aisandbox.server.simulation.cascade.CascadeBoardAssert.board;
import static org.junit.jupiter.api.Assertions.fail;

import dev.aisandbox.server.simulation.cascade.model.CascadeBoard;
import java.io.IOException;
import java.util.Random;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CascadeBoardUtils#updateBoard(CascadeBoard, Random)}.
 *
 * <p>Each test sets up a board in a specific unstable state and calls {@code updateBoard} exactly
 * once, verifying that only the single highest-priority action was performed. Tests are grouped
 * by the three update priorities defined in {@code runtime.md}.
 */
public class CascadeUpdateBoardTest {

  private static final String S = "## ## ## ## ## ## ## ##";
  private static final Random FIXED_RANDOM = new Random(42);

  @BeforeAll
  static void setup() {
    CascadeTestReport.reset();
  }

  @AfterAll
  static void report() throws IOException {
    CascadeTestReport.writeReport("updateBoard.html", "Cascade updateBoard Tests");
  }

  // Helper to reset random seed for deterministic refill tests
  private static Random seededRandom() {
    return new Random(42);
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

  // ══════════════════════════════════════════════════════════════════════════
  // Priority 1 — Gravity and refill
  // ══════════════════════════════════════════════════════════════════════════

  @Test
  void gravityDropsSingleTile() {
    // Red tile at (0,0), empty at (0,1), stones below. Tile should drop to (0,1).
    CascadeBoard b = board(
        "ro ## ## ## ## ## ## ##",
        ".. ## ## ## ## ## ## ##",
        S, S, S, S, S, S
    );
    CascadeBoard snapshot = b.copy();
    // After gravity: tile moves down. Top cell needs refill (open to top).
    String[] expected = {
        "?o ## ## ## ## ## ## ##",
        "ro ## ## ## ## ## ## ##",
        S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 0L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("gravityDropsSingleTile", passed, snapshot, 0L, 1L, expected, result, error);
  }

  @Test
  void gravityDropsMultipleTiles() {
    // Column 0: ro at y=0, empty at y=1, go at y=2, empty at y=3. Stones at y=4+.
    // Tiles compact downward within the segment.
    CascadeBoard b = board(
        "ro ## ## ## ## ## ## ##",
        ".. ## ## ## ## ## ## ##",
        "go ## ## ## ## ## ## ##",
        ".. ## ## ## ## ## ## ##",
        S, S, S, S
    );
    CascadeBoard snapshot = b.copy();
    // After gravity: ro and go sink to y=2 and y=3. Top two cells refilled.
    String[] expected = {
        "?o ## ## ## ## ## ## ##",
        "?o ## ## ## ## ## ## ##",
        "ro ## ## ## ## ## ## ##",
        "go ## ## ## ## ## ## ##",
        S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 0L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("gravityDropsMultipleTiles", passed, snapshot, 0L, 1L, expected, result,
        error);
  }

  @Test
  void gravityStopsAtStone() {
    // Column 0: empty at y=0, stone at y=1, ro at y=2, empty at y=3. Stones at y=4+.
    // The empty at y=0 is sealed by the stone at y=1. ro at y=2 drops to y=3.
    CascadeBoard b = board(
        ".. ## ## ## ## ## ## ##",
        "## ## ## ## ## ## ## ##",
        "ro ## ## ## ## ## ## ##",
        ".. ## ## ## ## ## ## ##",
        S, S, S, S
    );
    CascadeBoard snapshot = b.copy();
    // Empty at y=0 is sealed above by stone at y=1. ro drops from y=2 to y=3.
    // y=2 becomes empty but is open to top? No — stone at y=1 seals it. So y=2 stays empty.
    // Wait — stone at y=1 seals the segment. So segment is y=2..y=3 (between stone at y=1 and
    // stone at y=4). ro drops to y=3, y=2 is sealed by stone above -> no refill.
    // But y=0 is its own segment (y=0..y=0, sealed below by stone at y=1). It's open to top.
    // Actually y=0 is empty and open to top (nothing above it), so it gets refilled.
    String[] expected = {
        "?o ## ## ## ## ## ## ##",
        "## ## ## ## ## ## ## ##",
        ".. ## ## ## ## ## ## ##",
        "ro ## ## ## ## ## ## ##",
        S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 0L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("gravityStopsAtStone", passed, snapshot, 0L, 1L, expected, result, error);
  }

  @Test
  void gravityStopsAtIce() {
    // Column 0: empty at y=0, ice at y=1, ro at y=2, empty at y=3. Stones at y=4+.
    // Ice at y=1 seals the segment. ro drops from y=2 to y=3.
    CascadeBoard b = board(
        ".. ## ## ## ## ## ## ##",
        "ri ## ## ## ## ## ## ##",
        "ro ## ## ## ## ## ## ##",
        ".. ## ## ## ## ## ## ##",
        S, S, S, S
    );
    CascadeBoard snapshot = b.copy();
    // y=0 is open (above ice at y=1). Gets refilled.
    // Segment y=2..y=3: ro drops to y=3. y=2 sealed by ice at y=1 -> no refill.
    String[] expected = {
        "?o ## ## ## ## ## ## ##",
        "ri ## ## ## ## ## ## ##",
        ".. ## ## ## ## ## ## ##",
        "ro ## ## ## ## ## ## ##",
        S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 0L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("gravityStopsAtIce", passed, snapshot, 0L, 1L, expected, result, error);
  }

  @Test
  void refillsTopOfOpenColumn() {
    // Column 0: empty at y=0 and y=1, stones at y=2+. Both empties are open to top.
    CascadeBoard b = board(
        ".. ## ## ## ## ## ## ##",
        ".. ## ## ## ## ## ## ##",
        S, S, S, S, S, S
    );
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "?o ## ## ## ## ## ## ##",
        "?o ## ## ## ## ## ## ##",
        S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 0L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("refillsTopOfOpenColumn", passed, snapshot, 0L, 1L, expected, result, error);
  }

  @Test
  void noRefillBelowSeal() {
    // Column 0: stone at y=0, empty at y=1, stones at y=2+.
    // Empty at y=1 is sealed above by stone at y=0 — should NOT be refilled.
    // This board IS stable (sealed empty), so updateBoard should return unchanged.
    CascadeBoard b = board(
        "## ## ## ## ## ## ## ##",
        ".. ## ## ## ## ## ## ##",
        S, S, S, S, S, S
    );
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "## ## ## ## ## ## ## ##",
        ".. ## ## ## ## ## ## ##",
        S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 0L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("noRefillBelowSeal", passed, snapshot, 0L, 1L, expected, result, error);
  }

  @Test
  void gravityNoScoring() {
    // Gravity/refill should not change the score.
    CascadeBoard b = board(
        "ro ## ## ## ## ## ## ##",
        ".. ## ## ## ## ## ## ##",
        S, S, S, S, S, S
    );
    b.setScore(100);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "?o ## ## ## ## ## ## ##",
        "ro ## ## ## ## ## ## ##",
        S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 100L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("gravityNoScoring", passed, snapshot, 100L, 1L, expected, result, error);
  }

  @Test
  void gravityBeforeRefill() {
    // Tiles should drop first, then empties at top are refilled — all in a single call.
    // Column 0: empty, ro, empty. Stones at y=3+.
    // Gravity compacts: ro to y=2. Empties at y=0,y=1 refilled.
    CascadeBoard b = board(
        ".. ## ## ## ## ## ## ##",
        "ro ## ## ## ## ## ## ##",
        ".. ## ## ## ## ## ## ##",
        S, S, S, S, S
    );
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "?o ## ## ## ## ## ## ##",
        "?o ## ## ## ## ## ## ##",
        "ro ## ## ## ## ## ## ##",
        S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 0L, 1L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("gravityBeforeRefill", passed, snapshot, 0L, 1L, expected, result, error);
  }

  // ══════════════════════════════════════════════════════════════════════════
  // Priority 2 — Resolve activated specials
  // ══════════════════════════════════════════════════════════════════════════

  @Test
  void activatedBombExplodes3x3() {
    // Activated red bomb at (3,3). Surrounded by standard tiles, stones at edges.
    // 3x3 area centred on (3,3): x=2..4, y=2..4. All 8 neighbours + bomb = 9 cleared.
    // But bomb itself is replaced first, so 8 neighbours destroyed.
    // Score = 8 * 10 * 1 = 80. Multiplier doubled.
    CascadeBoard b = board(
        S, S,
        "## ## go bo go ## ## ##",
        "## ## bo RB bo ## ## ##",
        "## ## go bo go ## ## ##",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        S, S,
        "## ## .. .. .. ## ## ##",
        "## ## .. .. .. ## ## ##",
        "## ## .. .. .. ## ## ##",
        S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 80L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("activatedBombExplodes3x3", passed, snapshot, 80L, 2L, expected, result,
        error);
  }

  @Test
  void activatedBombDestroysStone() {
    // Activated red bomb at (3,3). Stone at (4,3) within 3x3 — should be destroyed.
    CascadeBoard b = board(
        S, S,
        "## ## go bo go ## ## ##",
        "## ## bo RB ## ## ## ##",
        "## ## go bo go ## ## ##",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        S, S,
        "## ## .. .. .. ## ## ##",
        "## ## .. .. .. ## ## ##",
        "## ## .. .. .. ## ## ##",
        S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 80L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("activatedBombDestroysStone", passed, snapshot, 80L, 2L, expected, result,
        error);
  }

  @Test
  void activatedBombDestroysIce() {
    // Activated red bomb at (3,3). Ice at (4,3) within 3x3 — destroyed (not unfrozen).
    CascadeBoard b = board(
        S, S,
        "## ## go bo go ## ## ##",
        "## ## bo RB gi ## ## ##",
        "## ## go bo go ## ## ##",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        S, S,
        "## ## .. .. .. ## ## ##",
        "## ## .. .. .. ## ## ##",
        "## ## .. .. .. ## ## ##",
        S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 80L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("activatedBombDestroysIce", passed, snapshot, 80L, 2L, expected, result,
        error);
  }

  @Test
  void activatedBombAtEdge() {
    // Activated red bomb at (0,0). 3x3 area is clamped to x=0..1, y=0..1.
    CascadeBoard b = board(
        "RB go ## ## ## ## ## ##",
        "bo ro ## ## ## ## ## ##",
        S, S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        ".. .. ## ## ## ## ## ##",
        ".. .. ## ## ## ## ## ##",
        S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      // 3 tiles destroyed (go, bo, ro — bomb itself doesn't count as destroyed)
      assertMatches(result, 30L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("activatedBombAtEdge", passed, snapshot, 30L, 2L, expected, result, error);
  }

  @Test
  void activatedBombChainsBomb() {
    // Activated red bomb at (3,3). Green bomb at (4,3) within 3x3 — should be activated.
    CascadeBoard b = board(
        S, S,
        "## ## go bo go ## ## ##",
        "## ## bo RB gb ## ## ##",
        "## ## go bo go ## ## ##",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Green bomb at (4,3) should be marked activated (not destroyed).
    // Other tiles in 3x3 destroyed except the green bomb.
    String[] expected = {
        S, S,
        "## ## .. .. .. ## ## ##",
        "## ## .. .. GB ## ## ##",
        "## ## .. .. .. ## ## ##",
        S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 70L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("activatedBombChainsBomb", passed, snapshot, 70L, 2L, expected, result,
        error);
  }

  @Test
  void activatedBombChainsRocket() {
    // Activated red bomb at (3,3). Green rocket at (4,3) within 3x3 — should be activated.
    CascadeBoard b = board(
        S, S,
        "## ## go bo go ## ## ##",
        "## ## bo RB gh ## ## ##",
        "## ## go bo go ## ## ##",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        S, S,
        "## ## .. .. .. ## ## ##",
        "## ## .. .. GH ## ## ##",
        "## ## .. .. .. ## ## ##",
        S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 70L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("activatedBombChainsRocket", passed, snapshot, 70L, 2L, expected, result,
        error);
  }

  @Test
  void activatedBombHitsPrism() {
    // Activated red bomb at (3,3). Prism at (4,3) within 3x3.
    // Prism triggers with bomb's colour (red). Red standard at (7,0) destroyed by prism effect.
    CascadeBoard b = board(
        "## ## ## ## ## ## ## ro",
        S,
        "## ## go bo go ## ## ##",
        "## ## bo RB xx ## ## ##",
        "## ## go bo go ## ## ##",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Prism at (4,3) destroyed by bomb, triggers red prism effect.
    // Red standard at (7,0) destroyed.
    String[] expected = {
        "## ## ## ## ## ## ## ..",
        S,
        "## ## .. .. .. ## ## ##",
        "## ## .. .. .. ## ## ##",
        "## ## .. .. .. ## ## ##",
        S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 90L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("activatedBombHitsPrism", passed, snapshot, 90L, 2L, expected, result,
        error);
  }

  @Test
  void activatedRocketHFiresHorizontally() {
    // Activated red ROCKET_H at (3,3). Fires left and right along row 3.
    CascadeBoard b = board(
        S, S, S,
        "go bo go RH go bo go bo",
        S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Entire row 3 cleared (all non-stone).
    String[] expected = {
        S, S, S,
        ".. .. .. .. .. .. .. ..",
        S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      // 7 tiles destroyed (rocket itself doesn't count)
      assertMatches(result, 70L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("activatedRocketHFiresHorizontally", passed, snapshot, 70L, 2L, expected,
        result, error);
  }

  @Test
  void activatedRocketVFiresVertically() {
    // Activated red ROCKET_V at (3,3). Fires up and down along column 3.
    CascadeBoard b = board(
        "## ## ## go ## ## ## ##",
        "## ## ## bo ## ## ## ##",
        "## ## ## go ## ## ## ##",
        "## ## ## RV ## ## ## ##",
        "## ## ## go ## ## ## ##",
        "## ## ## bo ## ## ## ##",
        "## ## ## go ## ## ## ##",
        S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Entire column 3 cleared (rows 0-6, row 7 is stone).
    String[] expected = {
        "## ## ## .. ## ## ## ##",
        "## ## ## .. ## ## ## ##",
        "## ## ## .. ## ## ## ##",
        "## ## ## .. ## ## ## ##",
        "## ## ## .. ## ## ## ##",
        "## ## ## .. ## ## ## ##",
        "## ## ## .. ## ## ## ##",
        S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      // 6 tiles destroyed (rocket itself doesn't count)
      assertMatches(result, 60L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("activatedRocketVFiresVertically", passed, snapshot, 60L, 2L, expected,
        result, error);
  }

  @Test
  void rocketStopsAtStone() {
    // Activated ROCKET_H at (3,3). Stone at (6,3) — rocket going right stops after destroying it.
    CascadeBoard b = board(
        S, S, S,
        "go bo go RH go bo ## bo",
        S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Left: (2,3),(1,3),(0,3) all destroyed.
    // Right: (4,3) destroyed, (5,3) destroyed, (6,3) stone destroyed -> STOP. (7,3) survives.
    String[] expected = {
        S, S, S,
        ".. .. .. .. .. .. .. bo",
        S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 60L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("rocketStopsAtStone", passed, snapshot, 60L, 2L, expected, result, error);
  }

  @Test
  void rocketDestroysIce() {
    // Activated ROCKET_H at (3,3). Ice at (5,3) — destroyed, rocket continues past.
    CascadeBoard b = board(
        S, S, S,
        "go bo go RH go gi go bo",
        S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Ice at (5,3) destroyed, then (6,3) and (7,3) also destroyed.
    String[] expected = {
        S, S, S,
        ".. .. .. .. .. .. .. ..",
        S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 70L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("rocketDestroysIce", passed, snapshot, 70L, 2L, expected, result, error);
  }

  @Test
  void rocketChainsSpecial() {
    // Activated ROCKET_H at (3,3). Green bomb at (5,3) in path — marked activated, not destroyed.
    CascadeBoard b = board(
        S, S, S,
        "go bo go RH go gb go bo",
        S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Rocket fires left and right. Green bomb at (5,3) activated, not destroyed.
    // Continues past green bomb: (6,3) and (7,3) destroyed.
    String[] expected = {
        S, S, S,
        ".. .. .. .. .. GB .. ..",
        S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 60L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("rocketChainsSpecial", passed, snapshot, 60L, 2L, expected, result, error);
  }

  @Test
  void rocketHitsPrism() {
    // Activated red ROCKET_H at (3,3). Prism at (5,3) in path.
    // Prism triggers with rocket's colour (red). Red standard at (0,0) destroyed.
    CascadeBoard b = board(
        "ro ## ## ## ## ## ## ##",
        S, S,
        "go bo go RH go xx go bo",
        S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Prism at (5,3) hit by rocket. Triggers red prism effect -> red at (0,0) destroyed.
    // Rocket continues past prism: (6,3) and (7,3) destroyed.
    String[] expected = {
        ".. ## ## ## ## ## ## ##",
        S, S,
        ".. .. .. .. .. .. .. ..",
        S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 80L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("rocketHitsPrism", passed, snapshot, 80L, 2L, expected, result, error);
  }

  @Test
  void chainReactionWithinSingleCall() {
    // Activated red bomb at (3,3). Green rocket at (4,3) in 3x3 — gets activated.
    // Chain reaction: green rocket fires within the same updateBoard call.
    // Row 3 should be fully cleared by the chain.
    CascadeBoard b = board(
        S, S,
        "## ## go bo go ## ## ##",
        "po bo go RB gh go bo po",
        "## ## go bo go ## ## ##",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Bomb clears 3x3. Green rocket activated. Rocket fires along row 3.
    // After chain: row 3 fully cleared, 3x3 area cleared.
    String[] expected = {
        S, S,
        "## ## .. .. .. ## ## ##",
        ".. .. .. .. .. .. .. ..",
        "## ## .. .. .. ## ## ##",
        S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      // Multiplier doubled once for the whole activated-special resolution
      assertMatches(result, 120L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("chainReactionWithinSingleCall", passed, snapshot, 120L, 2L, expected, result,
        error);
  }

  @Test
  void activatedSpecialsDoubleMultiplier() {
    // Start with multiplier=4. After activated specials resolve, multiplier should be 8.
    CascadeBoard b = board(
        S, S,
        "## ## go bo go ## ## ##",
        "## ## bo RB bo ## ## ##",
        "## ## go bo go ## ## ##",
        S, S, S
    );
    b.setMultiplier(4);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        S, S,
        "## ## .. .. .. ## ## ##",
        "## ## .. .. .. ## ## ##",
        "## ## .. .. .. ## ## ##",
        S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 320L, 8L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("activatedSpecialsDoubleMultiplier", passed, snapshot, 320L, 8L, expected,
        result, error);
  }

  @Test
  void multipleActivatedSpecials() {
    // Two activated bombs at (2,3) and (5,3). Both should fire in the same pass.
    CascadeBoard b = board(
        S, S,
        "## go bo go go bo go ##",
        "## bo RB bo bo RB bo ##",
        "## go bo go go bo go ##",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Both bombs fire their 3x3 areas.
    // Bomb at (2,3): x=1..3, y=2..4
    // Bomb at (5,3): x=4..6, y=2..4
    String[] expected = {
        S, S,
        "## .. .. .. .. .. .. ##",
        "## .. .. .. .. .. .. ##",
        "## .. .. .. .. .. .. ##",
        S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 160L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("multipleActivatedSpecials", passed, snapshot, 160L, 2L, expected, result,
        error);
  }

  // ══════════════════════════════════════════════════════════════════════════
  // Priority 3 — Match resolution and special spawning
  // ══════════════════════════════════════════════════════════════════════════

  @Test
  void horizontalRunOfThreeCleared() {
    // Three red tiles in a row at (2,0),(3,0),(4,0). Should be cleared.
    CascadeBoard b = board(
        "go bo ro ro ro bo go po",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "go bo .. .. .. bo go po",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 30L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("horizontalRunOfThreeCleared", passed, snapshot, 30L, 2L, expected, result,
        error);
  }

  @Test
  void verticalRunOfThreeCleared() {
    // Three red tiles in column 0 at y=0,1,2. Should be cleared.
    CascadeBoard b = board(
        "ro ## ## ## ## ## ## ##",
        "ro ## ## ## ## ## ## ##",
        "ro ## ## ## ## ## ## ##",
        S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        ".. ## ## ## ## ## ## ##",
        ".. ## ## ## ## ## ## ##",
        ".. ## ## ## ## ## ## ##",
        S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 30L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("verticalRunOfThreeCleared", passed, snapshot, 30L, 2L, expected, result,
        error);
  }

  @Test
  void runOfFourNoSpecial() {
    // Four red tiles in a row. All removed, no special spawned (per spec: exactly 4 -> no special).
    CascadeBoard b = board(
        "go ro ro ro ro go bo po",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "go .. .. .. .. go bo po",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 40L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("runOfFourNoSpecial", passed, snapshot, 40L, 2L, expected, result, error);
  }

  @Test
  void runOfFiveSpawnsBomb() {
    // Five red tiles in a row at (1,0)..(5,0). BOMB spawned at centre position (3,0).
    CascadeBoard b = board(
        "go ro ro ro ro ro go po",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // 4 tiles removed + 1 bomb spawned at centre (3,0).
    String[] expected = {
        "go .. .. rb .. .. go po",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 40L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("runOfFiveSpawnsBomb", passed, snapshot, 40L, 2L, expected, result, error);
  }

  @Test
  void runOfSixSpawnsPrism() {
    // Six red tiles in a row at (1,0)..(6,0). PRISM spawned at centre position (3,0).
    CascadeBoard b = board(
        "go ro ro ro ro ro ro po",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // 5 tiles removed + 1 prism spawned at centre (3,0) or (4,0).
    String[] expected = {
        "go .. .. xx .. .. .. po",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 50L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("runOfSixSpawnsPrism", passed, snapshot, 50L, 2L, expected, result, error);
  }

  @Test
  void lShapeSpawnsRocket() {
    // L-shape: 3 red tiles horizontal at (2,2),(3,2),(4,2) and 3 red vertical at (2,2),(2,3),(2,4).
    // Intersection at (2,2). Horizontal arm = 3, vertical arm = 3. Equal -> ROCKET_H.
    CascadeBoard b = board(
        S, S,
        "## ## ro ro ro ## ## ##",
        "## ## ro bo go ## ## ##",
        "## ## ro bo go ## ## ##",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Rocket spawned at intersection (2,2). Other tiles in L cleared.
    String[] expected = {
        S, S,
        "## ## rh .. .. ## ## ##",
        "## ## .. bo go ## ## ##",
        "## ## .. bo go ## ## ##",
        S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      // 4 tiles removed (the 5 in the L minus the 1 where rocket spawns)
      assertMatches(result, 40L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("lShapeSpawnsRocket", passed, snapshot, 40L, 2L, expected, result, error);
  }

  @Test
  void tShapeSpawnsRocket() {
    // T-shape: 5 red tiles horizontal at (1,2)..(5,2) and 3 red vertical at (3,2),(3,3),(3,4).
    // Intersection at (3,2). Horizontal arm = 5 > vertical arm = 3 -> ROCKET_H.
    // But also the horizontal run of 5 qualifies for a bomb. Intersection qualifies for rocket.
    // Per spec: cell qualifies for multiple rules -> highest tier: PRISM > BOMB > ROCKET.
    // The horizontal run of 5 wants a bomb at centre (3,2).
    // The L/T wants a rocket at (3,2).
    // BOMB > ROCKET, so bomb wins at (3,2).
    CascadeBoard b = board(
        S, S,
        "## ro ro ro ro ro ## ##",
        "## bo go ro go bo ## ##",
        "## bo go ro go bo ## ##",
        S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Bomb spawned at (3,2). All matched tiles removed except spawn position.
    String[] expected = {
        S, S,
        "## .. .. rb .. .. ## ##",
        "## bo go .. go bo ## ##",
        "## bo go .. go bo ## ##",
        S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      // 6 tiles removed (7 in T-shape minus 1 where bomb spawns)
      assertMatches(result, 60L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("tShapeSpawnsRocket", passed, snapshot, 60L, 2L, expected, result, error);
  }

  @Test
  void matchedBombActivated() {
    // Red bomb at (3,0) is part of a run of three reds: (2,0),(3,0),(4,0).
    // The bomb should be marked activated (not removed). Other tiles removed.
    CascadeBoard b = board(
        "go bo ro rb ro bo go po",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Bomb activated, not removed. Two standard tiles removed.
    String[] expected = {
        "go bo .. RB .. bo go po",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      // Only 2 tiles removed (the two standard reds)
      assertMatches(result, 20L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("matchedBombActivated", passed, snapshot, 20L, 2L, expected, result, error);
  }

  @Test
  void matchedRocketActivated() {
    // Red rocket_h at (3,0) is part of a run of three reds.
    // The rocket should be marked activated (not removed).
    CascadeBoard b = board(
        "go bo ro rh ro bo go po",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "go bo .. RH .. bo go po",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 20L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("matchedRocketActivated", passed, snapshot, 20L, 2L, expected, result,
        error);
  }

  @Test
  void iceAdjacentToMatchUnfreezes() {
    // Run of 3 red at (2,0),(3,0),(4,0). Red ice at (5,0) adjacent to the match.
    // Ice should unfreeze to red standard. Does not score.
    CascadeBoard b = board(
        "go bo ro ro ro ri go po",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    // Ice at (5,0) unfreezes to standard red. Three match tiles removed.
    String[] expected = {
        "go bo .. .. .. ro go po",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      // Only 3 tiles scored (not the unfrozen ice)
      assertMatches(result, 30L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("iceAdjacentToMatchUnfreezes", passed, snapshot, 30L, 2L, expected, result,
        error);
  }

  @Test
  void iceInMatchDestroyed() {
    // Run of 3: red standard, red ice, red standard at (2,0),(3,0),(4,0).
    // ICE is matchable and part of the run — destroyed (replaced with EMPTY), scores.
    CascadeBoard b = board(
        "go bo ro ri ro bo go po",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "go bo .. .. .. bo go po",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      // All 3 tiles in the match scored (including the ice)
      assertMatches(result, 30L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("iceInMatchDestroyed", passed, snapshot, 30L, 2L, expected, result, error);
  }

  @Test
  void matchScoringWithMultiplier() {
    // Start with multiplier=3. Match of 3 should score 3 * 10 * 3 = 90.
    CascadeBoard b = board(
        "go bo ro ro ro bo go po",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(3);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "go bo .. .. .. bo go po",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 90L, 6L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("matchScoringWithMultiplier", passed, snapshot, 90L, 6L, expected, result,
        error);
  }

  @Test
  void matchDoublesMultiplier() {
    // Start with multiplier=1. After match resolution, multiplier should be 2.
    CascadeBoard b = board(
        "go bo ro ro ro bo go po",
        S, S, S, S, S, S, S
    );
    b.setMultiplier(1);
    CascadeBoard snapshot = b.copy();
    String[] expected = {
        "go bo .. .. .. bo go po",
        S, S, S, S, S, S, S
    };
    boolean passed = false;
    String error = null;
    CascadeBoard result = null;
    try {
      result = CascadeBoardUtils.updateBoard(b, seededRandom());
      assertMatches(result, 30L, 2L, expected);
      passed = true;
    } catch (AssertionError | Exception e) {
      error = e.getMessage();
      result = b;
    }
    recordAndAssert("matchDoublesMultiplier", passed, snapshot, 30L, 2L, expected, result,
        error);
  }
}
