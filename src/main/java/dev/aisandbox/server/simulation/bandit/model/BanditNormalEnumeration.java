/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.bandit.model;

import java.util.Random;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of different probability distributions for bandit mean values.
 * <p>
 * This enumeration defines various probability distributions that can be used to generate the mean
 * reward values for bandits in the multi-armed bandit simulation. Each distribution provides a
 * different challenge and learning scenario for the agent.
 * </p>
 * <p>
 * Available distributions:
 * </p>
 * <ul>
 *   <li><strong>NORMAL_0_1:</strong> Normal distribution with mean=0, std=1</li>
 *   <li><strong>NORMAL_0_5:</strong> Normal distribution with mean=0, std=5</li>
 *   <li><strong>UNIFORM_1_1:</strong> Uniform distribution from -1 to 1</li>
 *   <li><strong>UNIFORM_0_5:</strong> Uniform distribution from 0 to 5</li>
 * </ul>
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BanditNormalEnumeration {
  /**
   * Normal distribution centered at 0 with standard deviation 1
   */
  NORMAL_0_1("Normal(0,1)"),
  /**
   * Normal distribution centered at 0 with standard deviation 5
   */
  NORMAL_0_5("Normal(0,5)"),
  /**
   * Uniform distribution between -1 and 1
   */
  UNIFORM_1_1("Uniform -1:1"),
  /**
   * Uniform distribution between 0 and 5
   */
  UNIFORM_0_5("Uniform 0:5");

  /**
   * Human-readable name for this distribution
   */
  private final String name;

  /**
   * Generates a random value from this distribution.
   * <p>
   * Samples a value from the probability distribution represented by this enumeration value. This
   * value is typically used as the mean reward for a bandit in the simulation.
   * </p>
   *
   * @param random the random number generator to use for sampling
   * @return a value sampled from this distribution
   */
  public double getNormalValue(Random random) {
    return switch (this) {
      case NORMAL_0_1 -> random.nextGaussian(0.0, 1.0);
      case NORMAL_0_5 -> random.nextGaussian(0.0, 5.0);
      case UNIFORM_1_1 -> random.nextDouble(-1.0, 1.0);
      case UNIFORM_0_5 -> random.nextDouble(0.0, 5.0);
    };
  }

  @Override
  public String toString() {
    return name;
  }
}
