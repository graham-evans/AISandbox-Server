/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mancala;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;

/**
 * Represents the Mancala (Kalah variant) board and implements all game logic.
 *
 * <p>The board uses an {@code int[14]} array for efficient counter-clockwise sowing:
 * <ul>
 *   <li>Indices 0-5: Player 1's pits (left to right from Player 1's perspective)</li>
 *   <li>Index 6: Player 1's store (mancala)</li>
 *   <li>Indices 7-12: Player 2's pits (left to right from Player 2's perspective)</li>
 *   <li>Index 13: Player 2's store (mancala)</li>
 * </ul>
 *
 * <p>Sowing proceeds counter-clockwise by incrementing the index (mod 14), skipping the
 * opponent's store.
 */
public class MancalaBoard {

  /** Number of pits per player. */
  static final int PITS_PER_PLAYER = 6;

  /** Index of Player 1's store. */
  static final int PLAYER1_STORE = 6;

  /** Index of Player 2's store. */
  static final int PLAYER2_STORE = 13;

  /** Total number of positions on the board (12 pits + 2 stores). */
  static final int BOARD_SIZE = 14;

  /** The board state: 12 pits and 2 stores. */
  @Getter
  private final int[] board;

  /**
   * Creates a new Mancala board with the specified number of seeds per pit.
   *
   * @param seedsPerPit the initial number of seeds in each pit
   */
  public MancalaBoard(int seedsPerPit) {
    board = new int[BOARD_SIZE];
    for (int i = 0; i < BOARD_SIZE; i++) {
      if (i == PLAYER1_STORE || i == PLAYER2_STORE) {
        board[i] = 0;
      } else {
        board[i] = seedsPerPit;
      }
    }
  }

  /**
   * Creates a board as a copy of the given board state.
   *
   * @param source the board state to copy
   */
  private MancalaBoard(int[] source) {
    board = Arrays.copyOf(source, source.length);
  }

  /**
   * Creates a deep copy of this board.
   *
   * @return a new MancalaBoard with identical state
   */
  public MancalaBoard copy() {
    return new MancalaBoard(board);
  }

  /**
   * Returns the store index for the given player.
   *
   * @param player the player (0 or 1)
   * @return the index of the player's store
   */
  static int storeIndex(int player) {
    return player == 0 ? PLAYER1_STORE : PLAYER2_STORE;
  }

  /**
   * Returns the store index for the opponent of the given player.
   *
   * @param player the player (0 or 1)
   * @return the index of the opponent's store
   */
  static int opponentStoreIndex(int player) {
    return player == 0 ? PLAYER2_STORE : PLAYER1_STORE;
  }

  /**
   * Converts a player-relative pit index (0-5) to an absolute board index.
   *
   * @param player the player (0 or 1)
   * @param pit    the pit index (0-5)
   * @return the absolute board index
   */
  static int pitIndex(int player, int pit) {
    return player == 0 ? pit : pit + PITS_PER_PLAYER + 1;
  }

  /**
   * Returns the seed count in the specified player's store.
   *
   * @param player the player (0 or 1)
   * @return the number of seeds in the store
   */
  public int getStore(int player) {
    return board[storeIndex(player)];
  }

  /**
   * Returns the seed counts for the specified player's pits.
   *
   * @param player the player (0 or 1)
   * @return an array of 6 seed counts
   */
  public int[] getPitsForPlayer(int player) {
    int start = player == 0 ? 0 : PITS_PER_PLAYER + 1;
    return Arrays.copyOfRange(board, start, start + PITS_PER_PLAYER);
  }

  /**
   * Returns the list of valid pit indices (0-5) that the player can choose.
   *
   * @param player the player (0 or 1)
   * @return list of pit indices with at least one seed
   */
  public List<Integer> getValidMoves(int player) {
    List<Integer> moves = new ArrayList<>();
    for (int i = 0; i < PITS_PER_PLAYER; i++) {
      if (board[pitIndex(player, i)] > 0) {
        moves.add(i);
      }
    }
    return moves;
  }

  /**
   * Sows seeds from the specified pit for the given player.
   *
   * <p>Seeds are distributed one at a time counter-clockwise, skipping the opponent's store.
   * After sowing, capture and extra-turn rules are applied.
   *
   * @param player the player making the move (0 or 1)
   * @param pit    the pit index (0-5) to sow from
   * @return the result of the sow operation
   */
  public SowResult sow(int player, int pit) {
    int startIndex = pitIndex(player, pit);
    int seeds = board[startIndex];
    board[startIndex] = 0;

    int opponentStore = opponentStoreIndex(player);
    int playerStore = storeIndex(player);

    int currentIndex = startIndex;
    for (int i = 0; i < seeds; i++) {
      currentIndex = (currentIndex + 1) % BOARD_SIZE;
      // skip opponent's store
      if (currentIndex == opponentStore) {
        currentIndex = (currentIndex + 1) % BOARD_SIZE;
      }
      board[currentIndex]++;
    }

    // Check for extra turn: last seed landed in player's store
    if (currentIndex == playerStore) {
      if (isGameOver()) {
        collectRemaining();
        return SowResult.GAME_OVER;
      }
      return SowResult.EXTRA_TURN;
    }

    // Check for capture: last seed landed in an empty pit on player's side
    if (board[currentIndex] == 1 && isPlayerPit(player, currentIndex)) {
      int oppositeIndex = getOppositePit(currentIndex);
      if (board[oppositeIndex] > 0) {
        board[playerStore] += board[currentIndex] + board[oppositeIndex];
        board[currentIndex] = 0;
        board[oppositeIndex] = 0;
      }
    }

    if (isGameOver()) {
      collectRemaining();
      return SowResult.GAME_OVER;
    }

    return SowResult.NORMAL;
  }

  /**
   * Checks if the given board index is one of the specified player's pits.
   *
   * @param player the player (0 or 1)
   * @param index  the board index to check
   * @return true if the index is one of the player's pits
   */
  private boolean isPlayerPit(int player, int index) {
    int start = player == 0 ? 0 : PITS_PER_PLAYER + 1;
    int end = start + PITS_PER_PLAYER;
    return index >= start && index < end;
  }

  /**
   * Returns the index of the pit opposite to the given pit index.
   *
   * @param index the board index of a pit
   * @return the index of the opposite pit
   */
  private int getOppositePit(int index) {
    return 12 - index;
  }

  /**
   * Checks if the game is over (either player has all pits empty).
   *
   * @return true if the game is over
   */
  public boolean isGameOver() {
    return allPitsEmpty(0) || allPitsEmpty(1);
  }

  /**
   * Checks if all pits for the given player are empty.
   *
   * @param player the player (0 or 1)
   * @return true if all six pits are empty
   */
  private boolean allPitsEmpty(int player) {
    for (int i = 0; i < PITS_PER_PLAYER; i++) {
      if (board[pitIndex(player, i)] > 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * Collects all remaining seeds into the respective player's stores.
   *
   * <p>Called when the game ends: each player collects all seeds remaining on their side.
   */
  void collectRemaining() {
    for (int player = 0; player < 2; player++) {
      for (int i = 0; i < PITS_PER_PLAYER; i++) {
        int idx = pitIndex(player, i);
        board[storeIndex(player)] += board[idx];
        board[idx] = 0;
      }
    }
  }

  /**
   * Returns the winner of the game, or -1 for a draw.
   *
   * <p>Should only be called after the game is over and remaining seeds have been collected.
   *
   * @return 0 if Player 1 wins, 1 if Player 2 wins, -1 for a draw
   */
  public int getWinner() {
    int store0 = getStore(0);
    int store1 = getStore(1);
    if (store0 > store1) {
      return 0;
    } else if (store1 > store0) {
      return 1;
    }
    return -1;
  }

  /**
   * Enumeration of possible sow outcomes.
   */
  public enum SowResult {
    /** The last seed landed in the player's store, granting an extra turn. */
    EXTRA_TURN,
    /** A normal move with no special outcome. */
    NORMAL,
    /** The game has ended (one side's pits are all empty). */
    GAME_OVER
  }
}
