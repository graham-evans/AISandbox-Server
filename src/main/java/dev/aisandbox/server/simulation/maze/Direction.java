/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.maze;

/**
 * Enum class representing the directions in a 2D space.
 */
public enum Direction {
  NORTH, SOUTH, EAST, WEST;

  /**
   * Converts a {@link dev.aisandbox.server.simulation.maze.proto.Direction} enum from the proto
   * buffer to this {@link Direction} enum.
   *
   * @param direction the proto buffer direction
   * @return the corresponding {@link Direction}
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
   * Return the direction opposite to the current value (i.e. North -> South)
   *
   * @return a {@link Direction} object opposite to this one.
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
