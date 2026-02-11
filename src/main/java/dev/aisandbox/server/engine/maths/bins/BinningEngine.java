/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.maths.bins;

import dev.aisandbox.server.engine.maths.BinContents;
import java.util.List;

/**
 * Interface for algorithms that organize data values into histogram bins.
 *
 * <p>A binning engine takes a collection of numeric data values and groups them into discrete
 * bins (intervals) for histogram visualization and statistical analysis. Different binning
 * strategies can be implemented to provide various trade-offs between data visualization clarity
 * and statistical accuracy.
 *
 * <p>Common binning strategies include:
 * <ul>
 *   <li>Equal-width binning: All bins have the same width</li>
 *   <li>Equal-frequency binning: All bins contain approximately the same number of values</li>
 *   <li>Fixed-bin binning: Using predetermined bin boundaries</li>
 * </ul>
 *
 * <p>The binning process typically involves:
 * <ol>
 *   <li>Analyzing the input data range and distribution</li>
 *   <li>Determining appropriate bin boundaries</li>
 *   <li>Counting values that fall within each bin</li>
 *   <li>Calculating bin densities or frequencies</li>
 * </ol>
 *
 * @see BinContents
 * @see EqualWidthBinner
 * @see IntegerBinner
 */
public interface BinningEngine {

  /**
   * Organizes the input data values into histogram bins.
   *
   * <p>This method takes a list of numeric values and groups them into discrete bins according to
   * the specific binning strategy implemented by this engine. Each returned {@link BinContents}
   * object represents one bin with its boundaries, count of values, and calculated density.
   *
   * <p>The number of bins, their boundaries, and their sizes depend on the specific implementation
   * and the characteristics of the input data.
   *
   * @param values the list of numeric values to organize into bins (must not be null)
   * @return a list of BinContents representing the histogram bins, ordered from lowest to highest
   * @throws IllegalArgumentException if values is null or contains non-finite numbers
   */
  List<BinContents> binValues(List<Double> values);

}
