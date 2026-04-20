/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.aisandbox.server.simulation.cascade.model.CascadeBoard;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CascadeBoardUtils#isStable(CascadeBoard)} and
 * {@link CascadeBoardUtils#isValid(CascadeBoard)}.
 *
 * <p>Board layouts are written in the two-character serialisation format defined in
 * {@link CascadeBoardUtils#deserialiseBoard}. A private helper pads any missing rows with empty
 * cells so individual tests only need to describe the rows that matter.
 */
public class CascadeBoardUtilsTest {

  private static final String EMPTY_ROW = ".. .. .. .. .. .. .. ..";
  private static final String STONE_ROW = "## ## ## ## ## ## ## ##";
  private static final String ICE_ROW   = "ri ri ri ri ri ri ri ri";

  /**
   * Builds a board from up to eight row strings in the two-character token format.
   * Any rows not supplied are filled with empty cells.
   */
  private static CascadeBoard board(String... rows) {
    List<String> padded = new ArrayList<>(Arrays.asList(rows));
    while (padded.size() < CascadeBoard.HEIGHT) {
      padded.add(EMPTY_ROW);
    }
    CascadeBoard b = new CascadeBoard();
    CascadeBoardUtils.deserialiseBoard(b, padded);
    return b;
  }

  // ── isStable: should return true ─────────────────────────────────────────

  /** Empty board has empty tiles and is therefore not stable — empty cells must be filled. */
  @Test
  void emptyBoardIsNotStable() {
    assertFalse(CascadeBoardUtils.isStable(new CascadeBoard()));
  }

  /**
   * Scenario 2: a full board whose colours cycle R/G/B can never produce a run of three
   * in any row or column.
   */
  @Test
  void fullBoardWithNoRunsIsStable() {
    assertTrue(CascadeBoardUtils.isStable(board(
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    )));
  }

  /** Scenario 2: exactly two adjacent red tiles — one short of a match. */
  @Test
  void runOfTwoIsStable() {
    assertTrue(CascadeBoardUtils.isStable(board(
        "ro ro bo go yo po ro bo",
        STONE_ROW, STONE_ROW, STONE_ROW,
        STONE_ROW, STONE_ROW, STONE_ROW, STONE_ROW
    )));
  }

  /** Scenario 3: a stone in the middle breaks what would otherwise be a run of three reds. */
  @Test
  void stoneBreaksRunIsStable() {
    // ro ro ## ro — stone is not matchable, so the run resets
    assertTrue(CascadeBoardUtils.isStable(board(
        "ro ro ## ro go yo po bo",
        STONE_ROW, STONE_ROW, STONE_ROW,
        STONE_ROW, STONE_ROW, STONE_ROW, STONE_ROW
    )));
  }

  /**
   * Scenario 4: an empty cell sealed above by an ice tile is stable — the ice blocks both
   * fallable tiles and new tiles from above, so the gap can never be filled.
   *
   * <p>Column 0: ice (y=0), empty (y=1), then stones. The empty at y=1 has ice directly above it;
   * nothing can enter its segment from above.
   */
  @Test
  void emptyCellSealedByIceAboveIsStable() {
    assertTrue(CascadeBoardUtils.isStable(board(
        "ri ## ## ## ## ## ## ##",
        ".. ## ## ## ## ## ## ##",
        "## ## ## ## ## ## ## ##",
        "## ## ## ## ## ## ## ##",
        "## ## ## ## ## ## ## ##",
        "## ## ## ## ## ## ## ##",
        "## ## ## ## ## ## ## ##",
        "## ## ## ## ## ## ## ##"
    )));
  }

  /**
   * Scenario 5: a prism sitting between two red tiles does not extend the run
   * because prisms are not matchable.
   */
  @Test
  void prismBreaksRunIsStable() {
    // ro xx ro — prism breaks the colour run
    assertTrue(CascadeBoardUtils.isStable(board(
        "ro xx ro bo go yo po bo",
        STONE_ROW, STONE_ROW, STONE_ROW,
        STONE_ROW, STONE_ROW, STONE_ROW, STONE_ROW
    )));
  }

  /** Scenario 7: a board containing only stones has no matchable tiles. */
  @Test
  void allStonesIsStable() {
    assertTrue(CascadeBoardUtils.isStable(board(
        STONE_ROW, STONE_ROW, STONE_ROW, STONE_ROW,
        STONE_ROW, STONE_ROW, STONE_ROW, STONE_ROW
    )));
  }

  // ── isStable: should return false ────────────────────────────────────────

  /** Scenario 1 (false): horizontal run of exactly three standard tiles. */
  @Test
  void horizontalRunOfThreeIsNotStable() {
    assertFalse(CascadeBoardUtils.isStable(board(
        "ro ro ro bo go yo po bo"
    )));
  }

  /** Scenario 2 (false): horizontal run of four or more same-colour tiles. */
  @Test
  void horizontalRunOfFourIsNotStable() {
    assertFalse(CascadeBoardUtils.isStable(board(
        "ro ro ro ro go yo po bo"
    )));
  }

  /** Scenario 4 (false): vertical run of exactly three standard tiles in a column. */
  @Test
  void verticalRunOfThreeIsNotStable() {
    // Column 0 has red tiles at y=0, y=1, y=2
    assertFalse(CascadeBoardUtils.isStable(board(
        "ro bo go yo po ro bo go",
        "ro go bo yo po ro go bo",
        "ro yo go bo po ro yo go",
        "bo go yo ro po bo go yo"
    )));
  }

  /** Scenario 5 (false): vertical run of four or more same-colour tiles in a column. */
  @Test
  void verticalRunOfFourIsNotStable() {
    // Column 0 has red tiles at y=0, y=1, y=2, y=3
    assertFalse(CascadeBoardUtils.isStable(board(
        "ro bo go yo po ro bo go",
        "ro go bo yo po ro go bo",
        "ro yo go bo po ro yo go",
        "ro go yo bo po ro go yo",
        "bo go yo ro po bo go yo"
    )));
  }

  /** Scenario 6 (false): run of three at the far right edge of the board (columns 5–7). */
  @Test
  void runAtFarEdgeIsNotStable() {
    assertFalse(CascadeBoardUtils.isStable(board(
        "bo go yo po bo ro ro ro"
    )));
  }

  /**
   * Scenario 7 (false): run of three made up of different matchable types —
   * standard, bomb, and rocket — all of the same colour.
   */
  @Test
  void mixedMatchableTypesRunIsNotStable() {
    // ro rb rv — red standard, red bomb, red rocket-V
    assertFalse(CascadeBoardUtils.isStable(board(
        "ro rb rv bo go yo po go"
    )));
  }

  /** Scenario 8 (false): a bomb flanked by two standard tiles of the same colour. */
  @Test
  void bombInRunIsNotStable() {
    // ro rb ro — red standard, red bomb, red standard
    assertFalse(CascadeBoardUtils.isStable(board(
        "ro rb ro bo go yo po go"
    )));
  }

  /** Scenario 8 (false): an ice tile flanked by two standard tiles of the same colour. */
  @Test
  void iceInRunIsNotStable() {
    // ro ri ro — red standard, red ice, red standard
    assertFalse(CascadeBoardUtils.isStable(board(
        "ro ri ro bo go yo po go"
    )));
  }

  /**
   * Scenario 9 (false): a board containing any activated tile is not stable — activated tiles
   * are pending resolution and must be processed before the board is considered settled.
   * The activated red bomb tile at (0,0) does not form a run of three, but the board is
   * still unstable because of the activated state.
   */
  @Test
  void activatedTileIsNotStable() {
    // RB = activated red bomb; no run of three present, but board is not stable
    assertFalse(CascadeBoardUtils.isStable(board(
        "RB go bo yo po ro bo go"
    )));
  }

  /**
   * Scenario 10 (false): an empty cell at y=0 (top of the board) is not stable — the column
   * segment is open to the top so a new tile will drop in to fill it.
   *
   * <p>Column 2 is empty at y=0 with no blocker above it (it is the topmost row).
   */
  @Test
  void emptyCellAtTopIsNotStable() {
    assertFalse(CascadeBoardUtils.isStable(board(
        "ro ro .. ro go yo po bo",
        STONE_ROW, STONE_ROW, STONE_ROW,
        STONE_ROW, STONE_ROW, STONE_ROW, STONE_ROW
    )));
  }

  /**
   * Scenario 11 (false): an empty cell with a fallable tile above it is not stable — gravity
   * will move the tile down to fill the gap.
   *
   * <p>Column 0: standard red tile at y=0, empty at y=1, then stones. The red tile should fall.
   */
  @Test
  void emptyCellWithFallableTileAboveIsNotStable() {
    assertFalse(CascadeBoardUtils.isStable(board(
        "ro ## ## ## ## ## ## ##",
        ".. ## ## ## ## ## ## ##",
        "## ## ## ## ## ## ## ##",
        "## ## ## ## ## ## ## ##",
        "## ## ## ## ## ## ## ##",
        "## ## ## ## ## ## ## ##",
        "## ## ## ## ## ## ## ##",
        "## ## ## ## ## ## ## ##"
    )));
  }

  // ── isValid: should return true ──────────────────────────────────────────

  /**
   * Scenario 1: a prism adjacent to a normal swappable tile constitutes a valid move
   * because the prism can be swapped with any non-stone, non-ice tile.
   */
  @Test
  void prismAdjacentToSwappableTileIsValid() {
    // xx at (0,0) is adjacent to ro at (1,0)
    assertTrue(CascadeBoardUtils.isValid(board(
        "xx ro bo go yo po ro bo"
    )));
  }

  /**
   * Scenario 2: two prisms sitting next to each other — each is a swappable neighbour
   * of the other.
   */
  @Test
  void twoPrismsAdjacentAreValid() {
    assertTrue(CascadeBoardUtils.isValid(board(
        "xx xx ## go yo po ro bo"
    )));
  }

  /**
   * Scenario 3: one swap away from a horizontal match of three.
   * Row: ro bo ro ro — swapping (0,0) and (1,0) places three reds at positions 1, 2, 3.
   */
  @Test
  void oneSwapFromHorizontalMatchIsValid() {
    assertTrue(CascadeBoardUtils.isValid(board(
        "ro bo ro ro go yo po bo"
    )));
  }

  /**
   * Scenario 4: one swap away from a vertical match of three.
   * Column 0: ro, bo, ro, ro — swapping (0,0) and (0,1) creates three reds at y=1, 2, 3.
   */
  @Test
  void oneSwapFromVerticalMatchIsValid() {
    assertTrue(CascadeBoardUtils.isValid(board(
        "ro bo go yo po ro bo go",
        "bo go yo po ro bo go yo",
        "ro yo go po ro yo go po",
        "ro po go yo ro po go yo"
    )));
  }

  /**
   * Scenario 5: swap creates a match of three that includes a bomb.
   * Row: ro ro go rb — swapping (2,0) and (3,0) aligns the red bomb with two red standard tiles.
   */
  @Test
  void oneSwapFromMatchContainingBombIsValid() {
    assertTrue(CascadeBoardUtils.isValid(board(
        "ro ro go rb bo yo po go"
    )));
  }

  /**
   * Scenario 6: swap creates a match of three that includes a rocket.
   * Row: ro ro go rv — swapping (2,0) and (3,0) aligns the red rocket with two red standard tiles.
   */
  @Test
  void oneSwapFromMatchContainingRocketIsValid() {
    assertTrue(CascadeBoardUtils.isValid(board(
        "ro ro go rv bo yo po go"
    )));
  }

  /**
   * Scenario 7: swap creates a match of three consisting entirely of bombs and rockets.
   * Row: rb rb go rv — swapping (2,0) and (3,0) creates a run of three red specials.
   */
  @Test
  void oneSwapFromMatchOfBombsAndRocketsIsValid() {
    assertTrue(CascadeBoardUtils.isValid(board(
        "rb rb go rv bo yo po go"
    )));
  }

  /**
   * Scenario 8: valid swap exists at the far edge of the board (last row, y=7).
   * Same horizontal match setup as scenario 3, placed in row 7.
   */
  @Test
  void validSwapAtFarEdgeIsValid() {
    assertTrue(CascadeBoardUtils.isValid(board(
        EMPTY_ROW, EMPTY_ROW, EMPTY_ROW, EMPTY_ROW,
        EMPTY_ROW, EMPTY_ROW, EMPTY_ROW,
        "ro bo ro ro go yo po bo"
    )));
  }

  /**
   * Scenario 9: a standard tile is swapped into a position adjacent to a bomb of the same colour,
   * creating a run of three.
   * Row: ro rb go ro — swapping (2,0) and (3,0) places a red standard next to the red bomb,
   * giving a run of three reds at positions 0, 1, 2.
   */
  @Test
  void standardSwappedNextToBombCreatesMatchIsValid() {
    assertTrue(CascadeBoardUtils.isValid(board(
        "ro rb go ro bo yo po go"
    )));
  }

  /**
   * Scenario 10: board where exactly one swap is valid; all other adjacent pairs produce no match.
   * Row: ro go ro ro — only swapping (0,0) and (1,0) creates a run of three reds at positions 1–3.
   */
  @Test
  void singleValidSwapBoardIsValid() {
    assertTrue(CascadeBoardUtils.isValid(board(
        "ro go ro ro bo yo po go"
    )));
  }

  // ── isValid: should return false ─────────────────────────────────────────

  /** Scenario 1: the empty board has no tiles to swap. */
  @Test
  void emptyBoardIsNotValid() {
    assertFalse(CascadeBoardUtils.isValid(new CascadeBoard()));
  }

  /** Scenario 2: a board full of stones has no swappable tiles. */
  @Test
  void allStonesIsNotValid() {
    assertFalse(CascadeBoardUtils.isValid(board(
        STONE_ROW, STONE_ROW, STONE_ROW, STONE_ROW,
        STONE_ROW, STONE_ROW, STONE_ROW, STONE_ROW
    )));
  }

  /**
   * Scenario 3: a board full of ice has no swappable tiles, even though ice is matchable.
   * Ice tiles participate in matches but cannot be directly swapped.
   */
  @Test
  void allIceIsNotValid() {
    assertFalse(CascadeBoardUtils.isValid(board(
        ICE_ROW, ICE_ROW, ICE_ROW, ICE_ROW,
        ICE_ROW, ICE_ROW, ICE_ROW, ICE_ROW
    )));
  }

  /**
   * Scenario 4: cycling R/G/B pattern — no adjacent swap produces a run of three,
   * and no PRISM is present.
   */
  @Test
  void noMatchPossibleNoPrismIsNotValid() {
    assertFalse(CascadeBoardUtils.isValid(board(
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    )));
  }

  /**
   * Scenario 5: pairs of same-colour tiles exist, but no swap extends any pair into a run of three.
   * Row: ro ro bo bo go go yo yo — each colour only appears in an isolated pair.
   */
  @Test
  void adjacentPairsButNoTripleIsNotValid() {
    assertFalse(CascadeBoardUtils.isValid(board(
        "ro ro bo bo go go yo yo"
    )));
  }

  /**
   * Scenario 6: a stone blocks the only swap that would produce a match.
   * Row: ro ## ro ro — without the stone, swapping (0,0) and (1,0) would create a run of three
   * reds at positions 1, 2, 3. The stone makes that swap illegal.
   */
  @Test
  void stoneBlocksOnlyValidSwapIsNotValid() {
    assertFalse(CascadeBoardUtils.isValid(board(
        "ro ## ro ro go yo po bo"
    )));
  }

  /**
   * Scenario 7: a prism completely surrounded by stones has no swappable neighbour and
   * therefore cannot be activated.
   *
   * <p>The current implementation incorrectly returns {@code true} for any board that contains
   * a prism, regardless of whether it can actually be swapped.
   */
  @Test
  void prismSurroundedByStonesIsNotValid() {
    // Prism at (0,0); its only neighbours — (1,0) and (0,1) — are stones.
    assertFalse(CascadeBoardUtils.isValid(board(
        "xx ## ## ## ## ## ## ##",
        STONE_ROW, STONE_ROW, STONE_ROW,
        STONE_ROW, STONE_ROW, STONE_ROW, STONE_ROW
    )));
  }

  /**
   * Scenario 8: a bomb is present but no swap on the board creates a match of three.
   *
   * <p>The current implementation incorrectly returns {@code true} for any board that contains
   * a bomb, regardless of whether a valid swap exists.
   */
  @Test
  void bombPresentButNoValidSwapIsNotValid() {
    // Red bomb at (0,0) in a cycling R/G/B board — no adjacent swap produces a match.
    assertFalse(CascadeBoardUtils.isValid(board(
        "rb go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    )));
  }

  /**
   * Scenario 9: a rocket is present but no swap on the board creates a match of three.
   *
   * <p>The current implementation incorrectly returns {@code true} for any board that contains
   * a rocket, regardless of whether a valid swap exists.
   */
  @Test
  void rocketPresentButNoValidSwapIsNotValid() {
    // Red horizontal rocket at (0,0) in a cycling R/G/B board — no adjacent swap produces a match.
    assertFalse(CascadeBoardUtils.isValid(board(
        "rh go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo",
        "bo ro go bo ro go bo ro",
        "ro go bo ro go bo ro go",
        "go bo ro go bo ro go bo"
    )));
  }
}
