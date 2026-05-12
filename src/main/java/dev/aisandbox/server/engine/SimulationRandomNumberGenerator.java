/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.ZigguratSampler;
import org.apache.commons.rng.simple.RandomSource;

public class SimulationRandomNumberGenerator {

  private final UniformRandomProvider random;
  private final ZigguratSampler.NormalizedGaussian gaussian;

  public SimulationRandomNumberGenerator(long seed) {
    random = RandomSource.MT_64.create(seed);
    gaussian = ZigguratSampler.NormalizedGaussian.of(random);
  }

  // Delegate standard methods
  public int nextInt(int bound)  { return random.nextInt(bound); }
  public double nextDouble()     { return random.nextDouble(); }
  public double nextDouble(double origin, double bound) { return random.nextDouble(origin, bound); }
  public boolean nextBoolean()   { return random.nextBoolean(); }
  public long nextLong()         { return random.nextLong(); }

  // Standard normal (mean 0, std 1), matching java.util.Random#nextGaussian()
  public double nextGaussian() {
    return gaussian.sample();
  }

  // Gaussian with mean and std
  public double nextGaussian(double mean, double std) {
    return mean + std * gaussian.sample();
  }

}
