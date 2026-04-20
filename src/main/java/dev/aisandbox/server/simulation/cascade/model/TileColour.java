/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade.model;

/**
 * The five tile colours used on the Cascade game board, plus a sentinel value for tiles that carry
 * no inherent colour (Stones, Prisms, and empty cells).
 *
 * <p>Colour is the primary attribute used to determine whether three or more adjacent tiles form a
 * match. Special tiles such as {@link TileType#PRISM} adopt the colour of whatever tile they are
 * swapped with at the moment of activation, rather than holding a fixed colour of their own.
 */
public enum TileColour {

  /** No colour — used for empty cells, Stones, and Prisms (which are colour-agnostic). */
  NONE,

  /** Red. */
  RED,

  /** Blue. */
  BLUE,

  /** Green. */
  GREEN,

  /** Yellow. */
  YELLOW,

  /** Purple. */
  PURPLE;

  /**
   * Returns an array containing only the five playable colours (excluding {@link #NONE}).
   *
   * <p>This is a convenience method for use during board initialisation and random tile generation,
   * where selecting {@code NONE} would be meaningless.
   *
   * @return array of the five coloured values: RED, BLUE, GREEN, YELLOW, PURPLE
   */
  public static TileColour[] playableValues() {
    return new TileColour[]{RED, BLUE, GREEN, YELLOW, PURPLE};
  }
}