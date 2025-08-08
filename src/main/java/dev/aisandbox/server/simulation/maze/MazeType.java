/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.maze;

/**
 * Enumeration of maze generation algorithms available in the AI Sandbox.
 * <p>
 * Each algorithm produces mazes with different characteristics and visual patterns:
 * </p>
 * <ul>
 *   <li>BINARYTREE - Creates mazes with a distinctive bias toward the northeast corner</li>
 *   <li>SIDEWINDER - Produces mazes with horizontal corridors and vertical connections</li>
 *   <li>RECURSIVEBACKTRACKER - Generates mazes with long winding passages and few dead ends</li>
 *   <li>BRAIDED - Creates interconnected mazes with loops, offering multiple solution paths</li>
 * </ul>
 * <p>
 * The choice of algorithm affects both the visual appearance and the difficulty characteristics
 * of the generated maze, providing variety for different AI training scenarios.
 * </p>
 *
 * @see MazeGenerator
 */
public enum MazeType {
  /**
   * A biased binary tree algorithm that creates mazes with passages trending toward
   * the northeast corner, resulting in predictable but varied maze structures.
   */
  BINARYTREE("Binary Tree (Biased)"),
  
  /**
   * Sidewinder algorithm that creates mazes with long horizontal corridors connected
   * by occasional vertical passages, producing distinctive horizontal emphasis.
   */
  SIDEWINDER("Sidewinder"),
  
  /**
   * Recursive backtracker algorithm that generates mazes with long, winding passages
   * and relatively few dead ends, creating engaging exploration challenges.
   */
  RECURSIVEBACKTRACKER("Recursive Backtracker"),
  
  /**
   * Braided maze algorithm that includes loops and multiple solution paths,
   * creating more complex navigation scenarios with interconnected routes.
   */
  BRAIDED("Braided (includes loops)");

  /** The human-readable name of this maze generation algorithm */
  private final String name;

  /**
   * Creates a maze type with the specified display name.
   *
   * @param name the human-readable name of this maze generation algorithm
   */
  MazeType(final String name) {
    this.name = name;
  }

}
