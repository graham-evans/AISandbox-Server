/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade.model;

/**
 * Represents a single cell on the 8×8 Cascade game board.
 *
 * <p>A cell is an immutable value composed of a {@link TileType} and a {@link TileColour}. The
 * board mutates state by replacing cell references in the grid rather than modifying cells in
 * place, so immutability is a natural fit.
 *
 * <p>Use the static factories {@link #empty()} and {@link #standard(TileColour)} for the two most
 * common cases, or the canonical constructor for special objects.
 */
public record CascadeCell(TileType type, TileColour colour) {

  /**
   * Returns {@code true} if this cell is occupied by any tile or object.
   *
   * <p>An empty cell ({@link TileType#EMPTY}) contributes nothing to matches and will be filled by
   * gravity or refill as appropriate.
   *
   * @return {@code true} when {@code type != EMPTY}
   */
  public boolean isOccupied() {
    return type != TileType.EMPTY;
  }

  /**
   * Returns {@code true} if this cell contains a standard coloured tile that participates directly
   * in three-in-a-row checks.
   *
   * <p>Bombs, Rockets, and Prisms are triggered by matches but are not themselves colour-matched.
   *
   * @return {@code true} when the type is {@link TileType#STANDARD}
   */
  public boolean isMatchable() {
    return type == TileType.STANDARD;
  }

  /**
   * Returns {@code true} if this cell is subject to gravity.
   *
   * <p>Stones are fixed obstacles that do not fall. All other occupied tile types obey gravity and
   * will fall into empty cells below them.
   *
   * @return {@code true} when the cell is occupied and its type is not {@link TileType#STONE}
   */
  public boolean isFallable() {
    return isOccupied() && type != TileType.STONE;
  }

  /**
   * Factory: creates a standard tile with the given colour.
   *
   * @param colour the tile colour (must not be {@link TileColour#NONE})
   * @return a new {@link CascadeCell} with {@code type=STANDARD}
   */
  public static CascadeCell standard(TileColour colour) {
    return new CascadeCell(TileType.STANDARD, colour);
  }

  /**
   * Factory: creates an empty cell.
   *
   * @return a new {@link CascadeCell} with {@code type=EMPTY} and {@code colour=NONE}
   */
  public static CascadeCell empty() {
    return new CascadeCell(TileType.EMPTY, TileColour.NONE);
  }
}