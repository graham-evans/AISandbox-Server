/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mancala;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the MancalaBoard game logic.
 */
public class TestMancalaBoard {

  /**
   * Tests that a new board is set up correctly with the given seeds per pit.
   */
  @Test
  public void testInitialSetup() {
    MancalaBoard board = new MancalaBoard(4);
    // Check Player 1's pits
    assertArrayEquals(new int[]{4, 4, 4, 4, 4, 4}, board.getPitsForPlayer(0));
    // Check Player 2's pits
    assertArrayEquals(new int[]{4, 4, 4, 4, 4, 4}, board.getPitsForPlayer(1));
    // Check stores are empty
    assertEquals(0, board.getStore(0));
    assertEquals(0, board.getStore(1));
  }

  /**
   * Tests that valid moves only include non-empty pits.
   */
  @Test
  public void testValidMoves() {
    MancalaBoard board = new MancalaBoard(4);
    List<Integer> moves = board.getValidMoves(0);
    assertEquals(List.of(0, 1, 2, 3, 4, 5), moves);
  }

  /**
   * Tests a basic sow operation distributes seeds correctly.
   */
  @Test
  public void testBasicSow() {
    MancalaBoard board = new MancalaBoard(4);
    // Player 0 sows from pit 0 (4 seeds: goes to pits 1, 2, 3, 4)
    MancalaBoard.SowResult result = board.sow(0, 0);
    assertEquals(MancalaBoard.SowResult.NORMAL, result);
    assertEquals(0, board.getPitsForPlayer(0)[0]);
    assertEquals(5, board.getPitsForPlayer(0)[1]);
    assertEquals(5, board.getPitsForPlayer(0)[2]);
    assertEquals(5, board.getPitsForPlayer(0)[3]);
    assertEquals(5, board.getPitsForPlayer(0)[4]);
  }

  /**
   * Tests that the last seed landing in the player's store triggers an extra turn.
   */
  @Test
  public void testExtraTurn() {
    MancalaBoard board = new MancalaBoard(4);
    // Player 0 sows from pit 2 (4 seeds: goes to pits 3, 4, 5, store)
    MancalaBoard.SowResult result = board.sow(0, 2);
    assertEquals(MancalaBoard.SowResult.EXTRA_TURN, result);
    assertEquals(1, board.getStore(0));
  }

  /**
   * Tests capture: last seed lands in empty own pit with seeds opposite.
   */
  @Test
  public void testCapture() {
    MancalaBoard board = new MancalaBoard(4);
    // Manually set up a capture scenario:
    // Empty pit 0 of player 0, put 1 seed in pit 5 so it lands in empty pit 0...
    // Actually, let's set up the board directly
    int[] b = board.getBoard();
    // Clear player 0's pit 0
    b[0] = 0;
    // Put 1 seed in player 0's pit 5 (index 5) that will land in store, not helpful
    // Let's think about this more carefully
    // We need: last seed to land in an empty pit on player's side with opposite having seeds
    // Player 0 pit index 0 is empty, player 2's opposite pit is index 12
    // If we put 1 seed in pit 5, sowing from pit 5 lands in store (index 6) - that's extra turn
    // If player 1 sows from pit 0 (index 7): seeds go to 8, 9, 10, 11
    // Let's make a cleaner setup:
    // Player 0 pit 1 (index 1) = 0, player 0 pit 0 (index 0) has 1 seed
    // Sow from pit 0: seed goes to index 1 (empty) -> captures opposite (index 11)
    b[0] = 1;
    b[1] = 0;
    // Opposite of index 1 is index 11 (12 - 1 = 11)
    b[11] = 3; // Player 2's pit 4 (index 11) has 3 seeds

    MancalaBoard.SowResult result = board.sow(0, 0);
    assertEquals(MancalaBoard.SowResult.NORMAL, result);
    // Player 0's store should have captured seeds: 1 (landing seed) + 3 (opposite) = 4
    assertEquals(4, board.getStore(0));
    // Both pit 1 and pit 11 should be empty
    assertEquals(0, board.getBoard()[1]);
    assertEquals(0, board.getBoard()[11]);
  }

  /**
   * Tests that no capture occurs when opposite pit is empty.
   */
  @Test
  public void testNoCaptureWhenOppositeEmpty() {
    MancalaBoard board = new MancalaBoard(4);
    int[] b = board.getBoard();
    b[0] = 1;
    b[1] = 0;
    b[11] = 0; // Opposite is empty

    MancalaBoard.SowResult result = board.sow(0, 0);
    assertEquals(MancalaBoard.SowResult.NORMAL, result);
    // No capture - store should be empty
    assertEquals(0, board.getStore(0));
    // The seed should remain in pit 1
    assertEquals(1, board.getBoard()[1]);
  }

  /**
   * Tests that the opponent's store is skipped during sowing.
   */
  @Test
  public void testSkipOpponentStore() {
    MancalaBoard board = new MancalaBoard(4);
    int[] b = board.getBoard();
    // Give Player 0 pit 5 (index 5) many seeds to wrap around
    b[5] = 10;
    board.sow(0, 5);
    // Seeds should go: 6(store), 7, 8, 9, 10, 11, 12, [skip 13], 0, 1
    // Player 2's store (index 13) should remain 0
    assertEquals(0, board.getStore(1));
    // Player 0's store should have 1 seed
    assertEquals(1, board.getStore(0));
  }

  /**
   * Tests game over detection when one player's pits are all empty.
   */
  @Test
  public void testGameOver() {
    MancalaBoard board = new MancalaBoard(4);
    int[] b = board.getBoard();
    // Empty all of player 0's pits except pit 0
    for (int i = 1; i < 6; i++) {
      b[i] = 0;
    }
    b[0] = 1;
    // Sow pit 0 - it will go to pit 1, which doesn't end the game
    // Let's set up so that after sowing, player 0 has no seeds
    b[0] = 0;
    // Now player 0's pits are all empty -> game over
    assertTrue(board.isGameOver());
  }

  /**
   * Tests that the game is not over when pits still have seeds.
   */
  @Test
  public void testNotGameOver() {
    MancalaBoard board = new MancalaBoard(4);
    assertFalse(board.isGameOver());
  }

  /**
   * Tests remaining seed collection at game end.
   */
  @Test
  public void testCollectRemaining() {
    MancalaBoard board = new MancalaBoard(4);
    int[] b = board.getBoard();
    // Set up: Player 0 has no pits, Player 1 has some seeds
    for (int i = 0; i < 6; i++) {
      b[i] = 0;
    }
    b[6] = 20; // Player 0's store
    // Player 1 has 7+8+9+0+0+0 = 24 seeds in pits
    b[7] = 7;
    b[8] = 8;
    b[9] = 9;
    b[10] = 0;
    b[11] = 0;
    b[12] = 0;
    b[13] = 4; // Player 1's store

    board.collectRemaining();
    assertEquals(20, board.getStore(0));
    assertEquals(28, board.getStore(1)); // 4 + 24
    // All pits should be empty
    assertArrayEquals(new int[]{0, 0, 0, 0, 0, 0}, board.getPitsForPlayer(0));
    assertArrayEquals(new int[]{0, 0, 0, 0, 0, 0}, board.getPitsForPlayer(1));
  }

  /**
   * Tests winner determination.
   */
  @Test
  public void testGetWinner() {
    MancalaBoard board = new MancalaBoard(4);
    int[] b = board.getBoard();
    // Clear all pits
    for (int i = 0; i < 14; i++) {
      b[i] = 0;
    }
    b[6] = 30;  // Player 0's store
    b[13] = 18; // Player 1's store
    assertEquals(0, board.getWinner());

    b[6] = 18;
    b[13] = 30;
    assertEquals(1, board.getWinner());

    b[6] = 24;
    b[13] = 24;
    assertEquals(-1, board.getWinner()); // draw
  }

  /**
   * Tests the deep copy functionality.
   */
  @Test
  public void testCopy() {
    MancalaBoard original = new MancalaBoard(4);
    MancalaBoard copy = original.copy();
    // Modify original
    original.sow(0, 0);
    // Copy should be unchanged
    assertArrayEquals(new int[]{4, 4, 4, 4, 4, 4}, copy.getPitsForPlayer(0));
  }

  /**
   * Tests sowing that wraps around the board past opponent's store.
   */
  @Test
  public void testWrapAroundSow() {
    MancalaBoard board = new MancalaBoard(0);
    int[] b = board.getBoard();
    // Put 9 seeds in pit 5 (index 5). Sowing wraps around, skipping opponent's store (13).
    // Seeds go to: 6(store), 7, 8, 9, 10, 11, 12, [skip 13], 0, 1
    // Last seed lands in pit 1 (index 1), which is empty -> but opposite (index 11) = 1
    // so capture triggers: store += 1 + 1 = 2, total store = 1 + 2 = 3
    // To avoid capture complexity, put existing seeds in the landing pit.
    b[5] = 9;
    b[1] = 1; // make landing pit non-empty so capture does NOT trigger
    board.sow(0, 5);
    // Seeds: 6(store), 7-12, [skip 13], 0, 1
    assertEquals(1, board.getStore(0));      // one seed landed in store
    assertEquals(0, board.getStore(1));      // opponent's store was skipped
    assertEquals(1, board.getBoard()[7]);    // player 2's first pit got a seed
    assertEquals(1, board.getBoard()[12]);   // player 2's last pit got a seed
    assertEquals(1, board.getBoard()[0]);    // player 0's pit 0 got a seed
    assertEquals(2, board.getBoard()[1]);    // landing pit: was 1, +1 = 2 (no capture)
    assertEquals(0, board.getBoard()[5]);    // original pit emptied
  }
}
