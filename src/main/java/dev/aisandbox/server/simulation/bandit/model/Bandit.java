/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.bandit.model;

import java.util.Random;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a single bandit (slot machine) in the multi-armed bandit problem.
 * <p>
 * Each bandit has an associated reward distribution defined by a mean and standard deviation.
 * When "pulled" by an agent, the bandit generates a reward value sampled from a normal
 * (Gaussian) distribution with these parameters.
 * </p>
 * <p>
 * The bandit's true reward parameters are typically unknown to the agent, who must learn
 * through experience which bandits provide the best rewards on average.
 * </p>
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * // Create a bandit with mean reward of 2.5 and standard deviation of 1.0
 * Bandit bandit = new Bandit(2.5, 1.0);
 * 
 * // Pull the bandit to get a reward
 * Random random = new Random();
 * double reward = bandit.pull(random);
 * </pre>
 */
public class Bandit {

  /** Mean value of the reward distribution */
  @Getter
  @Setter
  private double mean = 0.0;
  
  /** Standard deviation of the reward distribution */
  @Getter
  @Setter
  private double std = 1.0;

  /**
   * Creates a new bandit with specified reward distribution parameters.
   *
   * @param mean the mean (average) reward value
   * @param std  the standard deviation of reward values
   */
  public Bandit(double mean, double std) {
    this.mean = mean;
    this.std = std;
  }

  /**
   * Pull this bandit to receive a reward.
   * <p>
   * Generates a reward value sampled from a normal (Gaussian) distribution
   * with this bandit's mean and standard deviation parameters.
   * </p>
   *
   * @param rand the random number generator to use for sampling
   * @return a reward value sampled from the bandit's distribution
   */
  public double pull(Random rand) {
    return rand.nextGaussian() * std + mean;
  }

  /**
   * Generates a hash code for this bandit.
   * <p>
   * The hash code is based on both the mean and standard deviation values,
   * ensuring that bandits with different distributions have different hash codes.
   * </p>
   *
   * @return the hash code for this bandit
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(mean).append(std).toHashCode();
  }

  /**
   * Determines equality with another object.
   * <p>
   * Two bandits are considered equal if they have the same mean and standard deviation
   * values. This allows for comparing bandit configurations and detecting duplicates.
   * </p>
   *
   * @param o the object (Bandit) to compare with
   * @return true if o is a bandit with the same mean and standard deviation
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Bandit that = (Bandit) o;

    return new EqualsBuilder().append(mean, that.mean).append(std, that.std).isEquals();
  }


}
