/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mine;

/**
 * Enum representing the possible states of a Mine Hunter game.
 *
 * <p>This enum tracks the lifecycle of a game from initialization through gameplay to its
 * eventual conclusion (either victory or defeat).
 */
public enum GameState {
  /**
   * The initial state when a game board is created but play hasn't started yet. In this state, the
   * board is set up with mines but no cells have been uncovered.
   */
  INIT,

  /**
   * The active gameplay state where the player is uncovering cells and placing flags. This state
   * continues until the player either wins by finding all mines or loses by triggering a mine.
   */
  PLAYING,

  /**
   * The victory state, reached when all non-mine cells have been uncovered or all mines have been
   * correctly flagged.
   */
  WON,

  /**
   * The defeat state, reached when the player uncovers a cell containing a mine. When this state is
   * reached, the game is over and a new episode begins.
   */
  LOST
}