/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mine;

import lombok.Data;

/**
 * Represents a single cell in a Minesweeper game board.
 * <p>
 * Each cell can contain a mine, be covered/uncovered, be flagged by the player, and have a count of
 * neighboring mines. This class models the state and behavior of individual cells in the Mine
 * Hunter simulation.
 * </p>
 * <p>
 * The cell's state determines how it is displayed to the player and how it affects gameplay when
 * interacted with.
 * </p>
 */
@Data
public class Cell {

  /**
   * Indicates whether this cell contains a mine.
   * <p>
   * When true, uncovering this cell will end the game in defeat.
   * </p>
   */
  private boolean mine;

  /**
   * Indicates whether this cell is still covered (not revealed).
   * <p>
   * Default value is true (covered). When false, the cell has been revealed to the player, showing
   * either a mine or the number of neighboring mines.
   * </p>
   */
  private boolean covered = true;

  /**
   * Indicates whether this cell has been flagged by the player.
   * <p>
   * Flagged cells cannot be uncovered until the flag is removed. Correctly flagging all mines is
   * one way to win the game.
   * </p>
   */
  private boolean flagged;

  /**
   * Stores the number of neighboring cells that contain mines.
   * <p>
   * This value ranges from 0 to 8, representing the count of mines in the eight surrounding cells
   * (horizontally, vertically, and diagonally).
   * </p>
   */
  private int neighbours;

  /**
   * Returns the sprite index to use when rendering this cell.
   * <p>
   * This method maps the cell's logical state to a visual representation by providing an index into
   * a sprite sheet of cell images.
   * </p>
   * <p>
   * The mapping from character representation to sprite index is:
   * <ul>
   * <li>12: Correctly flagged mine ('F')</li>
   * <li>13: Incorrectly flagged cell ('f')</li>
   * <li>11: Covered cell ('#')</li>
   * <li>10: Uncovered mine ('X')</li>
   * <li>0: Empty cell with no neighboring mines ('.')</li>
   * <li>1-8: Cell showing count of neighboring mines</li>
   * </ul>
   * </p>
   *
   * @return the index of the sprite to use for rendering this cell
   */
  public int getPlayerViewSprite() {
    return switch (getPlayerView()) {
      case 'F' -> 12; // Correctly flagged mine
      case 'f' -> 13; // Incorrectly flagged (no mine)
      case '#' -> 11; // Covered cell (not revealed)
      case 'X' -> 10; // Uncovered mine
      case '.' -> 0;  // No neighboring mines
      case '1' -> 1;  // One neighboring mine
      case '2' -> 2;  // Two neighboring mines
      case '3' -> 3;  // Three neighboring mines
      case '4' -> 4;  // Four neighboring mines
      case '5' -> 5;  // Five neighboring mines
      case '6' -> 6;  // Six neighboring mines
      case '7' -> 7;  // Seven neighboring mines
      case '8' -> 8;  // Eight neighboring mines
      default -> 0;   // Default case (should not occur in normal gameplay)
    };
  }

  /**
   * Returns a character representation of the cell's current state for display to the player.
   * <p>
   * This method provides a character-based visualization of the cell's state, which is used for
   * both communication with the agent and internal logic.
   * </p>
   * <p>
   * The returned characters are:
   * <ul>
   * <li>'F': A correctly flagged mine</li>
   * <li>'f': An incorrectly flagged cell (no mine)</li>
   * <li>'#': A covered cell (not revealed yet)</li>
   * <li>'X': An uncovered mine</li>
   * <li>'.': An uncovered cell with no neighboring mines</li>
   * <li>'1'-'8': An uncovered cell with 1-8 neighboring mines</li>
   * </ul>
   *
   * @return a character representing the cell's current state
   */
  public char getPlayerView() {
    if (covered) {
      // Cell is still covered
      if (flagged) {
        if (mine) {
          return 'F'; // Correctly flagged mine
        } else {
          return 'f'; // Incorrectly flagged (no mine)
        }
      } else {
        return '#'; // Covered cell (not revealed)
      }
    } else {
      // Cell is uncovered
      if (mine) {
        return 'X'; // Uncovered mine
      } else if (neighbours == 0) {
        return '.'; // No neighboring mines
      } else {
        return Integer.toString(neighbours).charAt(0); // Number of neighboring mines
      }
    }
  }
}
