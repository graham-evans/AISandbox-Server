/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MineSize {
  SMALL("Small (8x6 10 Mines)", 9, 9, 10), MEDIUM("Medium (16x16 40 Mines)", 16, 16, 40), LARGE(
      "Large (24x24 99 Mines)", 24, 24, 99), MEGA("Mega (40x40 150 Mines)", 40, 40, 150);

  private final String name;
  private final int width;
  private final int height;
  private final int count;

  @Override
  public String toString() {
    return name;
  }
}