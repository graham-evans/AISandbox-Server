/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.bandit.model;

import lombok.Getter;

/**
 * Enumeration of standard deviation values for bandits in the multi-armed bandit simulation.
 */
@Getter
public enum BanditStdEnumeration {
  ONE(1.0), FIVE(5.0), TENTH(1.0 / 10);

  private final double value;

  BanditStdEnumeration(double value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return Double.toString(value);
  }
}
