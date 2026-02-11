/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.maths;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Utility class providing mathematical and statistical operations for simulation data analysis.
 *
 * <p>This class contains static methods for common statistical calculations needed throughout the
 * AI Sandbox simulations, including data binning, min/max calculations, and distribution analysis.
 * These utilities are primarily used for generating charts, histograms, and statistical summaries
 * of agent performance.
 *
 * <p>All methods are static and the class cannot be instantiated due to the
 * {@code @UtilityClass} annotation from Lombok.
 */
@UtilityClass
public class StatisticsUtils {

  /**
   * Finds the minimum and maximum values in a list of numbers.
   *
   * <p>This method efficiently scans through the list once to determine both the minimum and
   * maximum values. The result is returned as a Pair for convenient access to both values.
   *
   * @param values the list of values to analyze (must not be empty)
   * @return a Pair containing the minimum value (left) and maximum value (right)
   * @throws AssertionError if the list is empty
   */
  public static Pair<Double, Double> getMinMax(final List<Double> values) {
    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;
    for (final double value : values) {
      min = Math.min(min, value);
      max = Math.max(max, value);
    }
    return Pair.of(min, max);
  }

  /**
   * Bins a list of values into specified ranges and calculates bin statistics.
   *
   * <p>This method takes a list of values and distributes them into bins defined by start and end
   * boundaries. Each value is assigned to the first bin that contains it. The method calculates
   * both the count and density for each bin.
   *
   * <p>This is commonly used for creating histograms and distribution visualizations of agent
   * performance data.
   *
   * @param values    the list of values to be binned
   * @param binStarts the start boundaries for each bin (inclusive)
   * @param binEnds   the end boundaries for each bin (inclusive)
   * @return a list of BinContents objects containing count and density information for each bin
   * @throws AssertionError if binStarts and binEnds have different sizes, or if any value falls
   *                        outside all bin ranges
   */
  public static List<BinContents> getBinnedValues(final List<Double> values,
      final List<Double> binStarts, final List<Double> binEnds) {
    assert binStarts.size() == binEnds.size();
    int[] bincounts = new int[binStarts.size()];
    for (double value : values) {
      boolean found = false;
      for (int i = 0; (!found) && (i < binStarts.size()); i++) {
        if ((binStarts.get(i) <= value) && (binEnds.get(i) >= value)) {
          bincounts[i]++;
          found = true;
        }
      }
      assert found : "Value " + value + " out of binning range " + binStarts.getFirst() + " to "
          + binEnds.getFirst();
    }
    // work out bin density
    double[] bindensity = new double[binStarts.size()];
    for (int i = 0; i < binStarts.size(); i++) {
      bindensity[i] = binStarts.get(i) / (binEnds.get(i) - binStarts.get(i));
    }
    // generate records
    List<BinContents> bins = new ArrayList<BinContents>();
    for (int i = 0; i < binStarts.size(); i++) {
      bins.add(new BinContents(binStarts.get(i), binEnds.get(i), bincounts[i], bindensity[i]));
    }
    return bins;
  }

}
