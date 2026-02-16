/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade.model;

/**
 * Classifies what kind of object occupies a cell on the Cascade game board.
 *
 * <p>Each value determines both how the cell participates in match resolution and what happens when
 * it is destroyed or triggered.
 *
 * <ul>
 *   <li>{@link #EMPTY} — the cell is unoccupied; gravity will cause tiles above to fall into it.
 *   <li>{@link #STANDARD} — a plain coloured tile; removed by matching three or more in a line.
 *   <li>{@link #BOMB} — spawned by a 5-in-a-row match; detonates a 3×3 area when triggered.
 *   <li>{@link #ROCKET_H} — a horizontal rocket; clears its entire row when triggered.
 *   <li>{@link #ROCKET_V} — a vertical rocket; clears its entire column when triggered.
 *   <li>{@link #PRISM} — spawned by a 6+-in-a-row match; removes every tile of the paired colour.
 *   <li>{@link #ICE} — a level-placed object encasing a tile; must be freed by an adjacent match.
 *   <li>{@link #STONE} — an inert obstacle; can only be removed by a Bomb or Rocket explosion.
 * </ul>
 */
public enum TileType {

  /**
   * An empty cell.
   *
   * <p>Empty cells are transient — they exist only between the match-removal step and the gravity
   * step. After gravity, all empty cells should be at the top of their column (waiting for the
   * refill step to replace them with new tiles).
   */
  EMPTY,

  /**
   * A standard coloured tile.
   *
   * <p>Participates directly in colour-matching. Three or more adjacent standard tiles of the same
   * colour form a match and are removed simultaneously.
   */
  STANDARD,

  /**
   * A bomb special object.
   *
   * <p>Created automatically when a straight match of exactly five tiles occurs. When triggered
   * (either by being part of a match or caught in a nearby explosion), it removes all tiles in a
   * 3×3 area centred on its position and recursively triggers any specials within that area.
   */
  BOMB,

  /**
   * A horizontal rocket special object.
   *
   * <p>Created by an L-shaped or T-shaped match where the dominant axis is horizontal. When
   * triggered, fires across its entire row, removing every tile and triggering any specials in the
   * path.
   */
  ROCKET_H,

  /**
   * A vertical rocket special object.
   *
   * <p>Created by an L-shaped or T-shaped match where the dominant axis is vertical. When
   * triggered, fires down its entire column, removing every tile and triggering any specials in
   * the path.
   */
  ROCKET_V,

  /**
   * A prism (rainbow) special object.
   *
   * <p>Created by a straight match of six or more tiles. Has no fixed colour. When swapped with a
   * coloured tile, removes every tile of that colour from the board. When swapped with another
   * special object, removes all tiles of that special's colour and copies the special's effect at
   * each removed position.
   */
  PRISM,

  /**
   * An ice-block obstacle encasing a tile.
   *
   * <p>Placed by level configuration before the game starts. The trapped tile's colour is visible
   * but cannot participate in matches directly. Freed by making a match adjacent to the ice block.
   * A single freed ice tile becomes an ordinary standard tile of its original colour. Bombs and
   * Rockets destroy ice blocks instantly.
   */
  ICE,

  /**
   * A stone obstacle.
   *
   * <p>Inert — does not match, does not fall under gravity, and blocks cascade propagation. Can
   * only be removed by a direct Bomb or Rocket explosion.
   */
  STONE

}