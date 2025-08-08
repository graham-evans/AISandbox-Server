/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.maze;

/**
 * Enumeration representing the four cardinal directions in a 2D coordinate system.
 * <p>
 * This enum is used throughout the maze simulation to represent movement directions,
 * path connections, and agent orientations. It provides utility methods for direction
 * conversion and manipulation.
 * </p>
 * <p>
 * The directions follow standard compass conventions:
 * </p>
 * <ul>
 *   <li>NORTH - toward decreasing Y coordinates (up)</li>
 *   <li>SOUTH - toward increasing Y coordinates (down)</li>
 *   <li>EAST - toward increasing X coordinates (right)</li>
 *   <li>WEST - toward decreasing X coordinates (left)</li>
 * </ul>
 *
 * @see dev.aisandbox.server.simulation.maze.proto.Direction
 */
public enum Direction {
  /** Direction toward decreasing Y coordinates (up on screen) */
  NORTH, 
  
  /** Direction toward increasing Y coordinates (down on screen) */
  SOUTH, 
  
  /** Direction toward increasing X coordinates (right on screen) */
  EAST, 
  
  /** Direction toward decreasing X coordinates (left on screen) */
  WEST;

  /**
   * Converts a Protocol Buffer Direction enum to this Direction enum.
   * <p>
   * This method provides interoperability between the generated Protocol Buffer
   * direction enum and this internal representation. It handles the UNRECOGNIZED
   * value by defaulting to NORTH for safety.
   * </p>
   *
   * @param direction the Protocol Buffer direction enum value to convert
   * @return the corresponding Direction enum value, defaulting to NORTH for unrecognized values
   */
  public static Direction fromProto(
      dev.aisandbox.server.simulation.maze.proto.Direction direction) {
    return switch (direction) {
      case NORTH, UNRECOGNIZED -> NORTH;
      case SOUTH -> SOUTH;
      case EAST -> EAST;
      case WEST -> WEST;
    };
  }

  /**
   * Returns the direction that is opposite to this direction.
   * <p>
   * This method is useful for maze generation algorithms and pathfinding logic
   * where you need to determine the reverse direction. The mappings are:
   * </p>
   * <ul>
   *   <li>NORTH ↔ SOUTH</li>
   *   <li>EAST ↔ WEST</li>
   * </ul>
   * <p>
   * This method is commonly used when creating bidirectional paths in mazes
   * or when an agent needs to backtrack.
   * </p>
   *
   * @return the Direction that is 180 degrees opposite to this direction
   */
  public Direction opposite() {
    return switch (this) {
      case NORTH -> SOUTH;
      case EAST -> WEST;
      case SOUTH -> NORTH;
      case WEST -> EAST;
    };
  }
}
