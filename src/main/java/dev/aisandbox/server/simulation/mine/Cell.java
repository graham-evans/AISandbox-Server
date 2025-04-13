package dev.aisandbox.server.simulation.mine;

import lombok.Data;

@Data
public class Cell {

  private boolean mine;

  // is this cell still covered
  private boolean covered = true;

  // has this cell been flagged
  private boolean flagged;

  // the number of neighbours that are mines
  private int neighbours;

  /**
   * getPlayerView.
   *
   * @return a char.
   */
  public char getPlayerView() {
    if (covered) {
      if (flagged) {
        if (mine) {
          return 'F';
        } else {
          return 'f';
        }
      } else {
        return '#';
      }
    } else {
      if (mine) {
        return 'X';
      } else if (neighbours == 0) {
        return '.';
      } else {
        return Integer.toString(neighbours).charAt(0);
      }
    }
  }
}
