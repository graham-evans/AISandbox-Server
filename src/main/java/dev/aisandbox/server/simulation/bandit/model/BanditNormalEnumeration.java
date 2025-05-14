/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.bandit.model;

import java.util.Random;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BanditNormalEnumeration {
  NORMAL_0_1("Normal(0,1)"), NORMAL_0_5("Normal(0,5)"), UNIFORM_1_1("Uniform -1:1"), UNIFORM_0_5(
      "Uniform 0:5");

  private final String name;

  public double getNormalValue(Random random) {
    return switch (this) {
      case NORMAL_0_1 -> random.nextGaussian(0.0, 1.0);
      case NORMAL_0_5 -> random.nextGaussian(0.0, 5.0);
      case UNIFORM_1_1 -> random.nextDouble(-1.0, 1.0);
      case UNIFORM_0_5 -> random.nextDouble(0.0, 5.0);
    };
  }

}
