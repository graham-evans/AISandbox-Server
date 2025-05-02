package dev.aisandbox.server.simulation.mine;

import lombok.Data;

/**
 * Represents a single cell in a Minesweeper game board.
 * Each cell can contain a mine, be covered/uncovered, be flagged by the player,
 * and have a count of neighboring mines.
 */
@Data
public class Cell {

  /** Indicates whether this cell contains a mine */
  private boolean mine;

  /** 
   * Indicates whether this cell is still covered (not revealed).
   * Default value is true (covered).
   */
  private boolean covered = true;

  /** Indicates whether this cell has been flagged by the player */
  private boolean flagged;

  /** Stores the number of neighboring cells that contain mines */
  private int neighbours;

  /**
   * Returns a character representation of the cell's current state for display to the player.
   * 
   * The returned characters are:
   * - 'F': A correctly flagged mine
   * - 'f': An incorrectly flagged cell (no mine)
   * - '#': A covered cell (not revealed yet)
   * - 'X': An uncovered mine
   * - '.': An uncovered cell with no neighboring mines
   * - '1'-'8': An uncovered cell with 1-8 neighboring mines
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

  public int getPlayerViewSprite() {
    return switch (getPlayerView()) {
      case 'F' -> 12; // Correctly flagged mine
      case 'f' -> 13; // Incorrectly flagged (no mine)
      case '#' -> 11; // Covered cell (not revealed)
      case 'X' -> 10; // Uncovered mine
      case '.' -> 0; // No neighboring mines
      case '1' -> 1; // Number of neighboring mines (1-8)
      case '2' -> 2;
      case '3' -> 3;
      case '4' -> 4;
      case '5' -> 5;
      case '6' -> 6;
      case '7' -> 7;  
      case '8' -> 8; // Number of neighboring mines (1-8)
      default -> 0; // Default case (should not happen)
    };
  }
}
