/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.widget.axis;

import java.util.List;

/**
 * Interface for scaling and formatting axis values in graph and chart visualizations.
 * <p>
 * This interface defines the contract for axis scaling implementations that convert raw data values
 * into appropriate display coordinates and formatted labels. Different implementations can provide
 * various scaling strategies such as linear, logarithmic, or "nice number" scaling that produces
 * human-friendly axis labels.
 * </p>
 * <p>
 * The interface supports:
 * </p>
 * <ul>
 *   <li>Value scaling for positioning data points on the visual axis</li>
 *   <li>Tick mark generation for axis graduation</li>
 *   <li>Label formatting for displaying values to users</li>
 *   <li>Minimum and maximum range determination</li>
 * </ul>
 * <p>
 * Common implementations include:
 * </p>
 * <ul>
 *   <li>{@link NiceAxisScale} - Uses "nice numbers" algorithm for human-friendly labels</li>
 *   <li>{@link TightAxisScale} - Uses exact data bounds for maximum data utilization</li>
 * </ul>
 *
 * @see NiceAxisScale
 * @see TightAxisScale
 */
public interface AxisScale {

  /**
   * Returns the minimum value represented by this axis scale.
   * <p>
   * This value represents the lower bound of the axis range and is used for positioning
   * the axis origin and scaling data points.
   * </p>
   *
   * @return the minimum value on the axis
   */
  double getMinimum();

  /**
   * Returns the maximum value represented by this axis scale.
   * <p>
   * This value represents the upper bound of the axis range and is used for positioning
   * the axis end and scaling data points.
   * </p>
   *
   * @return the maximum value on the axis
   */
  double getMaximum();

  /**
   * Scales a raw data value to a normalized position within the axis range.
   * <p>
   * This method converts a data value to a position value typically between 0.0 and 1.0,
   * where 0.0 represents the minimum axis value and 1.0 represents the maximum.
   * The scaled value can then be used to position visual elements on the axis.
   * </p>
   *
   * @param value the raw data value to scale
   * @return the scaled value (typically 0.0 to 1.0) representing the position on the axis
   */
  double getScaledValue(double value);

  /**
   * Formats a numeric value into a human-readable string for axis labels.
   * <p>
   * This method converts numeric values into appropriately formatted strings for display
   * as axis labels. The formatting should be consistent across the axis and appropriate
   * for the value range and precision.
   * </p>
   *
   * @param value the numeric value to format
   * @return a formatted string representation suitable for display as an axis label
   */
  String getValueString(double value);

  /**
   * Returns a list of tick mark positions for the axis.
   * <p>
   * Tick marks provide visual reference points along the axis and typically correspond
   * to labeled values. The returned list contains the raw data values where tick marks
   * should be placed, which can then be scaled using {@link #getScaledValue(double)}
   * for positioning.
   * </p>
   *
   * @return a list of raw data values where tick marks should be positioned
   */
  List<Double> getTicks();
}
