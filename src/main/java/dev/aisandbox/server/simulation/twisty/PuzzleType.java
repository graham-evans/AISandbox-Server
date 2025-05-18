/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.twisty;

import dev.aisandbox.server.simulation.twisty.model.TwistyPuzzle;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of all available twisty puzzle types supported by the system.
 *
 * <p>This enum provides a centraliz" vPluginsed registry of all twisty puzzle configurations with
 * factory methods to instantiate the appropriate puzzle models. Each enum value represents a
 * specific puzzle configuration including standard NxNxN cubes and non-cubic cuboids.
 *
 * <p>The puzzle types use OBTM (Outer Block Turn Metric) notation for move descriptions.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PuzzleType {
  /**
   * Standard 3x3x3 Rubik's Cube.
   */
  CUBE3("Cube 3x3x3", "Cube 3x3x3 (OBTM)"),
  /**
   * 2x2x2 Pocket Cube.
   */
  CUBE2("Cube 2x2x2", "Cube 2x2x2 (OBTM)"),
  /**
   * 4x4x4 Rubik's Revenge.
   */
  CUBE4("Cube 4x4x4", "Cube 4x4x4 (OBTM)"),
  /**
   * 5x5x5 Professor's Cube.
   */
  CUBE5("Cube 5x5x5", "Cube 5x5x5 (OBTM)"),
  /**
   * 6x6x6 Cube.
   */
  CUBE6("Cube 6x6x6", "Cube 6x6x6 (OBTM)"),
  /**
   * 7x7x7 Cube.
   */
  CUBE7("Cube 7x7x7", "Cube 7x7x7 (OBTM)"),
  /**
   * 8x8x8 Cube.
   */
  CUBE8("Cube 8x8x8", "Cube 8x8x8 (OBTM)"),
  /**
   * 9x9x9 Cube.
   */
  CUBE9("Cube 9x9x9", "Cube 9x9x9 (OBTM)"),
  /**
   * 2x2x3 Cuboid.
   */
  CUBE223("Cuboid 2x2x3", "Cuboid 2x2x3 (OBTM)"),
  /**
   * 2x2x4 Cuboid.
   */
  CUBE224("Cuboid 2x2x4", "Cuboid 2x2x4 (OBTM)"),
  /**
   * 2x2x5 Cuboid.
   */
  CUBE225("Cuboid 2x2x5", "Cuboid 2x2x5 (OBTM)"),
  /**
   * 2x2x6 Cuboid.
   */
  CUBE226("Cuboid 2x2x6", "Cuboid 2x2x6 (OBTM)"),
  /**
   * 3x3x2 Cuboid (Floppy cube variant).
   */
  CUBE332("Cuboid 3x3x2", "Cuboid 3x3x2 (OBTM)"),
  /**
   * 3x3x4 Cuboid.
   */
  CUBE334("Cuboid 3x3x4", "Cuboid 3x3x4 (OBTM)"),
  /**
   * 3x3x5 Cuboid.
   */
  CUBE335("Cuboid 3x3x5", "Cuboid 3x3x5 (OBTM)");

  /**
   * Display name for the puzzle type.
   */
  private final String name;

  /**
   * Unique identifier for the puzzle type.
   */
  private final String id;

  /**
   * Returns the display name of the puzzle type.
   *
   * @return the display name of the puzzle
   */
  public String toString() {
    return name;
  }

  /**
   * Returns the unique identifier for the puzzle type.
   *
   * @return the unique identifier including OBTM notation information
   */
  public String getID() {
    return id;
  }

  /**
   * Creates and returns a new instance of the appropriate puzzle model based on the enum type.
   *
   * <p>This factory method delegates to the CuboidBuilder to construct the appropriate twisty
   * puzzle with the dimensions specified by the enum type.
   *
   * @return a new instance of the TwistyPuzzle corresponding to this type
   * @throws IOException if there is an error during puzzle construction
   */
  public TwistyPuzzle getTwistyPuzzle() throws IOException {
    return switch (this) {
      case CUBE3 -> CuboidBuilder.buildCuboid(3, 3, 3);
      case CUBE2 -> CuboidBuilder.buildCuboid(2, 2, 2);
      case CUBE4 -> CuboidBuilder.buildCuboid(4, 4, 4);
      case CUBE5 -> CuboidBuilder.buildCuboid(5, 5, 5);
      case CUBE6 -> CuboidBuilder.buildCuboid(6, 6, 6);
      case CUBE7 -> CuboidBuilder.buildCuboid(7, 7, 7);
      case CUBE8 -> CuboidBuilder.buildCuboid(8, 8, 8);
      case CUBE9 -> CuboidBuilder.buildCuboid(9, 9, 9);
      case CUBE223 -> CuboidBuilder.buildCuboid(2, 2, 3);
      case CUBE224 -> CuboidBuilder.buildCuboid(2, 2, 4);
      case CUBE225 -> CuboidBuilder.buildCuboid(2, 2, 5);
      case CUBE226 -> CuboidBuilder.buildCuboid(2, 2, 6);
      case CUBE332 -> CuboidBuilder.buildCuboid(3, 3, 2);
      case CUBE334 -> CuboidBuilder.buildCuboid(3, 3, 4);
      case CUBE335 -> CuboidBuilder.buildCuboid(3, 3, 5);
    };
  }
}
