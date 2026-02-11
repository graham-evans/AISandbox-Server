/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.maze;

import lombok.Getter;

/**
 * Enum representing predefined maze dimensions for the Maze simulation.
 *
 * <p>This enum defines standard maze configurations with different dimensions and zoom levels,
 * allowing users to select from small, medium, or large maze sizes. Each configuration specifies a
 * maze width, height, and zoom level for rendering.
 */
public enum MazeSize {
  /**
   * Small maze configuration: 8×6 grid with highest zoom level (5). This is suitable for quick
   * experiments or simple pathfinding tasks.
   */
  SMALL(8, 6, 5),

  /**
   * Medium maze configuration: 20×15 grid with medium zoom level (2). This provides a balance
   * between complexity and visibility.
   */
  MEDIUM(20, 15, 2),

  /**
   * Large maze configuration: 40×30 grid with lowest zoom level (1). This offers a challenging
   * environment for testing advanced pathfinding algorithms.
   */
  LARGE(40, 30, 1);

  /**
   * The width of the maze grid in cells.
   */
  @Getter
  private final int width;

  /**
   * The height of the maze grid in cells.
   */
  @Getter
  private final int height;

  /**
   * The zoom level for rendering the maze.
   *
   * <p>Higher values make the maze appear larger on screen. Lower values allow fitting larger mazes in
   * the same display area.
   */
  @Getter
  private final int zoomLevel;

  /**
   * Constructs a maze size configuration with the specified dimensions and zoom level.
   *
   * @param width     The width of the maze in cells
   * @param height    The height of the maze in cells
   * @param zoomLevel The zoom factor for rendering (higher values = larger appearance)
   */
  MazeSize(int width, int height, int zoomLevel) {
    this.width = width;
    this.height = height;
    this.zoomLevel = zoomLevel;
  }
}
