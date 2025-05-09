package dev.aisandbox.server.simulation.twisty.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * Represents a sequence of cells that form a loop in the twisty puzzle.
 * A move loop is used to track cells that move together when a specific 
 * rotation or manipulation is performed on the puzzle.
 */
public class MoveLoop {

  /**
   * The list of cells that are part of this move loop.
   * Cells within a move loop will be transformed together during puzzle manipulations.
   */
  @Getter
  List<Cell> cells = new ArrayList<>();

  /**
   * Removes a specific cell from the move loop.
   *
   * @param c The cell to be removed from the loop
   */
  public void removeCell(Cell c) {
    cells.remove(c);
  }

  /**
   * Returns a string representation of this move loop.
   * If the loop is empty, it indicates so. Otherwise, it shows the number of cells in the loop.
   *
   * @return A string describing the move loop and its size
   */
  @Override
  public String toString() {
    if (cells.isEmpty()) {
      return "Empty Loop";
    } else {
      return "Loop with " + cells.size() + " cells";
    }
  }
}
