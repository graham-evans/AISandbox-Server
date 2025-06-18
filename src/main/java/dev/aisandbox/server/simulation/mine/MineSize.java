/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing predefined board sizes for the Mine simulation.
 * <p>
 * This enum defines standard board configurations with different dimensions and mine counts,
 * allowing users to select from common difficulty levels. Each configuration specifies a board
 * width, height, and the number of mines to be placed.
 * </p>
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MineSize {
  /**
   * Small board configuration: 9x9 grid with 10 mines. This is suitable for beginners or quick
   * games.
   */
  SMALL("Small (9x9 10 Mines)", 9, 9, 10),

  /**
   * Medium board configuration: 16x16 grid with 40 mines. This is the standard difficulty level for
   * intermediate players.
   */
  MEDIUM("Medium (16x16 40 Mines)", 16, 16, 40),

  /**
   * Large board configuration: 24x24 grid with 99 mines. This offers a challenging experience for
   * advanced players.
   */
  LARGE("Large (24x24 99 Mines)", 24, 24, 99),

  /**
   * Mega board configuration: 40x40 grid with 150 mines. This is an extra large board for extended
   * gameplay sessions.
   */
  MEGA("Mega (40x40 150 Mines)", 40, 40, 150);

  /**
   * Display name of this board size configuration
   */
  private final String name;

  /**
   * Width of the board in cells
   */
  private final int width;

  /**
   * Height of the board in cells
   */
  private final int height;

  /**
   * Number of mines to be placed on the board
   */
  private final int count;

  /**
   * Returns the display name of this board size configuration.
   *
   * @return A string representation of this board size for UI display
   */
  @Override
  public String toString() {
    return name;
  }
}