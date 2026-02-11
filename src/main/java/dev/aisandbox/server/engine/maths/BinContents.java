/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.maths;

/**
 * Record representing the contents and statistics of a single bin in a histogram.
 *
 * <p>This record encapsulates all the information about a bin used in data distribution analysis,
 * including its boundaries, the number of data points it contains, and its calculated density. It
 * is typically used in conjunction with {@link StatisticsUtils} for creating histograms and
 * analyzing data distributions.
 *
 * <p>Example usage:
 * <pre>
 * BinContents bin = new BinContents(0.0, 1.0, 15, 0.75);
 * System.out.println("Bin [" + bin.binStart() + "-" + bin.binEnd() + "] contains " +
 *                    bin.quantity() + " values");
 * </pre>
 *
 * @param binStart the start boundary of the bin (inclusive)
 * @param binEnd   the end boundary of the bin (inclusive)
 * @param quantity the number of data points that fall within this bin
 * @param density  the calculated density of this bin (typically quantity divided by bin width)
 */
public record BinContents(double binStart, double binEnd, int quantity, double density) {

}
