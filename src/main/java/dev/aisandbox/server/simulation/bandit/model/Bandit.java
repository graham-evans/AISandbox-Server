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

public class Bandit {

  @Getter
  @Setter
  private double mean = 0.0;
  @Getter
  @Setter
  private double std = 1.0;

  public Bandit(double mean, double std) {
    this.mean = mean;
    this.std = std;
  }

  /**
   * Pull this bandit, returning a number based on a normal (gaussian) distribution with the current
   * std / mean.
   *
   * @param rand
   * @return
   */
  public double pull(Random rand) {
    return rand.nextGaussian() * std + mean;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(mean).append(std).toHashCode();
  }

  /**
   * Alternative equals definition. Bandits are concidered equal if they have the same std **and**
   * mean.
   *
   * @param o the object (Bandit) to compare with.
   * @return true if o is a bandit with the same std **and** mean.
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
