/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.bandit.model;

import lombok.Getter;

/**
 * Enumeration of available bandit counts for the multi-armed bandit simulation.
 */
public enum BanditCountEnumeration {
  FIVE(5), TEN(10), TWENTY(20), FIFTY(50);

  @Getter
  private final int number;

  BanditCountEnumeration(int number) {
    this.number = number;
  }

  @Override
  public String toString() {
    return Integer.toString(number);
  }
}
