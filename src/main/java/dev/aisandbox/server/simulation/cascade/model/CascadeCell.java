/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a single cell on the 8×8 Cascade game board.
 *
 * <p>A cell holds a {@link TileType}, a {@link TileColour}, and an {@code activated} flag used
 * during match resolution to mark special objects (Bombs, Rockets, Prisms) that have been
 * triggered and are waiting to fire in the current wave.
 *
 * <p>Cells are mutable so that the board can update them in place during the match → gravity →
 * refill → cascade loop without allocating a new object on every state change.
 *
 * <p>Use the static factories for construction:
 * <ul>
 *   <li>{@link #empty()} — unoccupied cell</li>
 *   <li>{@link #standard(TileColour)} — plain coloured tile</li>
 *   <li>{@link #bomb(TileColour)} — bomb special</li>
 *   <li>{@link #rocketH(TileColour)} — horizontal rocket special</li>
 *   <li>{@link #rocketV(TileColour)} — vertical rocket special</li>
 *   <li>{@link #prism()} — prism (rainbow) special</li>
 *   <li>{@link #ice(TileColour)} — ice-encased tile</li>
 *   <li>{@link #stone()} — inert stone obstacle</li>
 * </ul>
 */
@Getter
@Setter
@EqualsAndHashCode
public class CascadeCell {

  /** What kind of object occupies this cell. */
  private TileType type;

  /** The colour of this cell's tile; {@link TileColour#NONE} for Prisms, Stones, and empty cells. */
  private TileColour colour;

  /**
   * Whether this special tile has been triggered in the current resolution wave.
   *
   * <p>Only meaningful for {@link TileType#BOMB}, {@link TileType#ROCKET_H},
   * {@link TileType#ROCKET_V}, and {@link TileType#PRISM}. The activation loop checks this flag
   * to decide which specials still need to fire before the board is re-evaluated.
   */
  private boolean activated;

  private CascadeCell(TileType type, TileColour colour) {
    this.type = type;
    this.colour = colour;
    this.activated = false;
  }

  // -------------------------------------------------------------------------
  // State queries
  // -------------------------------------------------------------------------

  /**
   * Returns {@code true} if this cell is occupied by any tile or object.
   *
   * @return {@code true} when {@code type != EMPTY}
   */
  public boolean isOccupied() {
    return type != TileType.EMPTY;
  }

  /**
   * Returns {@code true} if this cell participates in three-in-a-row colour checks.
   *
   * <p>Standard tiles, Bombs, and Rockets all carry a colour and can form or extend a match.
   * Prisms, Ice, Stones, and empty cells do not.
   *
   * @return {@code true} when the type is {@link TileType#STANDARD}, {@link TileType#BOMB},
   *     {@link TileType#ROCKET_H}, or {@link TileType#ROCKET_V}
   */
  public boolean isMatchable() {
    return type == TileType.STANDARD
        || type == TileType.BOMB
        || type == TileType.ROCKET_H
        || type == TileType.ROCKET_V
        || type == TileType.ICE;
  }

  /**
   * Returns {@code true} if this cell is subject to gravity (will fall into empty cells below it).
   *
   * <p>Stones and Ice Blocks are fixed obstacles that do not fall.
   *
   * @return {@code true} when the cell is occupied, and its type is not {@link TileType#STONE}
   *     or {@link TileType#ICE}
   */
  public boolean isFallable() {
    return isOccupied() && type != TileType.STONE && type != TileType.ICE;
  }

  /**
   * Marks this tile as activated if it is a Bomb or Rocket, ready to fire in the current
   * resolution wave.
   *
   * <p>Has no effect on any other tile type.
   */
  public void activate() {
    if (type == TileType.BOMB || type == TileType.ROCKET_H || type == TileType.ROCKET_V) {
      this.activated = true;
    }
  }

  // -------------------------------------------------------------------------
  // Copy
  // -------------------------------------------------------------------------

  /**
   * Returns a new {@link CascadeCell} that is an independent copy of this one.
   *
   * <p>Used by {@link CascadeBoard#copy()} to produce a fully independent board clone.
   *
   * @return a new cell with the same type, colour, and activated state
   */
  public CascadeCell copy() {
    CascadeCell c = new CascadeCell(this.type, this.colour);
    c.activated = this.activated;
    return c;
  }

  // -------------------------------------------------------------------------
  // Static factories
  // -------------------------------------------------------------------------

  /** Returns a new empty (unoccupied) cell. */
  public static CascadeCell empty() {
    return new CascadeCell(TileType.EMPTY, TileColour.NONE);
  }

  /**
   * Returns a new standard coloured tile.
   *
   * @param colour the tile colour (must not be {@link TileColour#NONE})
   */
  public static CascadeCell standard(TileColour colour) {
    return new CascadeCell(TileType.STANDARD, colour);
  }

  /**
   * Returns a new bomb special tile in the given colour.
   *
   * @param colour the colour of the match that spawned this bomb
   */
  public static CascadeCell bomb(TileColour colour) {
    return new CascadeCell(TileType.BOMB, colour);
  }

  /**
   * Returns a new horizontal rocket special tile in the given colour.
   *
   * @param colour the colour of the match that spawned this rocket
   */
  public static CascadeCell rocketH(TileColour colour) {
    return new CascadeCell(TileType.ROCKET_H, colour);
  }

  /**
   * Returns a new vertical rocket special tile in the given colour.
   *
   * @param colour the colour of the match that spawned this rocket
   */
  public static CascadeCell rocketV(TileColour colour) {
    return new CascadeCell(TileType.ROCKET_V, colour);
  }

  /**
   * Returns a new prism (rainbow) special tile.
   *
   * <p>Prisms have no fixed colour ({@link TileColour#NONE}); they adopt the colour of whatever
   * tile they are paired with at activation time.
   */
  public static CascadeCell prism() {
    return new CascadeCell(TileType.PRISM, TileColour.NONE);
  }

  /**
   * Returns a new ice-block cell encasing a tile of the given colour.
   *
   * @param colour the colour of the trapped tile (visible but not directly swappable)
   */
  public static CascadeCell ice(TileColour colour) {
    return new CascadeCell(TileType.ICE, colour);
  }

  /**
   * Returns a new stone obstacle cell.
   *
   * <p>Stones are colourless ({@link TileColour#NONE}) and can only be removed by explosions.
   */
  public static CascadeCell stone() {
    return new CascadeCell(TileType.STONE, TileColour.NONE);
  }
}
