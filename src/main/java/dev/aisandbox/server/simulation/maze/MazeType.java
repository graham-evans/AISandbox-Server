package dev.aisandbox.server.simulation.maze;

/**
 * Enumerates all supported maze generation algorithms.
 *
 * @author [Your Name]
 */
public enum MazeType {
  /**
   * A biased binary tree algorithm.
   */
  BINARYTREE("Binary Tree (Biased)"),
  /**
   * Sidewinder maze generation algorithm.
   */
  SIDEWINDER("Sidewinder"),
  /**
   * Recursive backtracker maze generation algorithm.
   */
  RECURSIVEBACKTRACKER("Recursive Backtracker"),
  /**
   * Braided maze generation algorithm, which includes loops.
   */
  BRAIDED("Braided (includes loops)");

  private final String name;

  /**
   * Private constructor for enum values.
   *
   * @param name the human-readable name of this maze type
   */
  MazeType(final String name) {
    this.name = name;
  }

}
