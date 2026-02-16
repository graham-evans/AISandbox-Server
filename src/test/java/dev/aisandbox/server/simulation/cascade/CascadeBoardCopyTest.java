/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import dev.aisandbox.server.simulation.cascade.model.CascadeBoard;
import dev.aisandbox.server.simulation.cascade.model.CascadeCell;
import dev.aisandbox.server.simulation.cascade.model.TileColour;
import dev.aisandbox.server.simulation.cascade.model.TileType;
import org.junit.jupiter.api.Test;

/** Tests for {@link CascadeBoard#copy()}. */
public class CascadeBoardCopyTest {

  private static final String[] MIXED_BOARD = {
      "RRGBYPBG",
      "BGYPRRGB",
      "GPYBGPYR",
      "YRBPGYBR",
      "PBGYRPBG",
      "GRPBYRGY",
      "YBGRPBYP",
      "RGPYBGRP"
  };

  /** The copy is a different object from the original. */
  @Test
  void copyIsDistinctObject() {
    CascadeBoard original = CascadeBoardBuilder.parse(MIXED_BOARD);
    assertNotSame(original, original.copy());
  }

  /** Every cell in the copy matches the original. */
  @Test
  void copyHasSameCells() {
    CascadeBoard original = CascadeBoardBuilder.parse(MIXED_BOARD);
    CascadeBoard copy = original.copy();

    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        assertEquals(original.getCell(x, y), copy.getCell(x, y),
            "cell mismatch at (" + x + "," + y + ")");
      }
    }
  }

  /** The copy has the same score and movesRemaining as the original. */
  @Test
  void copyPreservesGameState() {
    CascadeBoard original = CascadeBoardBuilder.parse(MIXED_BOARD);
    original.addScore(500);
    original.consumeMove();

    CascadeBoard copy = original.copy();

    assertEquals(original.getScore(), copy.getScore());
    assertEquals(original.getMovesRemaining(), copy.getMovesRemaining());
  }

  /** Mutating the copy's cells does not affect the original. */
  @Test
  void mutateCopyDoesNotAffectOriginal() {
    CascadeBoard original = CascadeBoardBuilder.parse(MIXED_BOARD);
    CascadeBoard copy = original.copy();

    copy.setCell(0, 0, new CascadeCell(TileType.STONE, TileColour.NONE));

    assertEquals(TileType.STANDARD, original.getCell(0, 0).type(),
        "original cell (0,0) should be unchanged after mutating copy");
  }

  /** Mutating the original's cells does not affect the copy. */
  @Test
  void mutateOriginalDoesNotAffectCopy() {
    CascadeBoard original = CascadeBoardBuilder.parse(MIXED_BOARD);
    CascadeBoard copy = original.copy();

    original.setCell(0, 0, new CascadeCell(TileType.STONE, TileColour.NONE));

    assertEquals(TileType.STANDARD, copy.getCell(0, 0).type(),
        "copy cell (0,0) should be unchanged after mutating original");
  }
}