/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.widget;

import static dev.aisandbox.server.engine.output.OutputConstants.STATISTICS_FONT;
import static dev.aisandbox.server.engine.output.OutputConstants.STATISTICS_HEIGHT;

import dev.aisandbox.server.engine.Theme;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.statistics.descriptive.DoubleStatistics;
import org.apache.commons.statistics.descriptive.Statistic;

/**
 * Widget that displays statistics for a rolling window of success/failure events.
 * <p>
 * This widget maintains a rolling window of success/failure results along with associated values
 * for successful events. It renders statistical information including success rate and, for
 * successful events, minimum, maximum, mean, standard deviation, and variance of their values.
 * <p>
 * The widget supports custom sizing, padding, window size, and theming through its builder. It
 * implements caching to avoid unnecessary re-rendering of unchanged statistics.
 */
@SuppressWarnings("PMD.NullAssignment") // null is used to invalidate a cached object - this is ok.
public class RollingSuccessStatisticsWidget {

  /**
   * Format string for displaying double values with 2 decimal places
   */
  private static final String DOUBLE_FORMAT = "%.2f";

  /**
   * Width of the widget in pixels
   */
  private final int width;

  /**
   * Height of the widget in pixels
   */
  private final int height;

  /**
   * Padding from the edges in pixels
   */
  private final int padding;

  /**
   * Maximum number of values to keep in the rolling window
   */
  private final int windowSize;

  /**
   * Visual theme (colors, etc.) for the widget
   */
  private final Theme theme;

  /**
   * Whether the widget background should be opaque
   */
  private final boolean opaque;

  /**
   * Collection of success/failure results in the rolling window
   */
  private final List<SuccessResult> values = new ArrayList<>();

  /**
   * Unicode symbol for standard deviation (sigma)
   */
  private final String STD = "\u03C3";

  /**
   * Unicode symbol for squared (²)
   */
  private final String SQR = "\u00B2";

  /**
   * Cached rendered image to avoid unnecessary re-rendering
   */
  private BufferedImage cachedImage = null;

  /**
   * Constructs a new RollingSuccessStatisticsWidget with the specified parameters.
   *
   * @param width      Width of the widget in pixels
   * @param height     Height of the widget in pixels
   * @param padding    Padding from the edges in pixels
   * @param windowSize Maximum number of values to keep in the rolling window
   * @param theme      Visual theme for the widget
   * @param opaque     Whether the widget background should be opaque
   */
  public RollingSuccessStatisticsWidget(int width, int height, int padding, int windowSize,
      Theme theme, boolean opaque) {
    this.width = width;
    this.height = height;
    this.padding = padding;
    this.windowSize = windowSize;
    this.theme = theme;
    this.opaque = opaque;
  }

  /**
   * Creates and returns a new builder for this widget.
   *
   * @return A new RollingSuccessStatisticsWidgetBuilder instance
   */
  public static RollingSuccessStatisticsWidgetBuilder builder() {
    return new RollingSuccessStatisticsWidgetBuilder();
  }

  /**
   * Adds a failure result to the rolling window of statistics.
   * <p>
   * A failure is represented with a value of 0.0.
   */
  public void addFailure() {
    addResult(false, 0.0);
  }

  /**
   * Adds a result to the rolling window of statistics.
   * <p>
   * If the window is full (i.e., the number of values equals windowSize), the oldest result is
   * removed. Adding a new result invalidates the cached image.
   *
   * @param success Whether the result represents a success
   * @param value   The value associated with the result (typically only meaningful for successes)
   */
  public void addResult(boolean success, double value) {
    // Add the new result to the window
    values.add(new SuccessResult(success, value));
    // Remove old results if window is full
    while (values.size() > windowSize) {
      values.removeFirst();
    }
    // Invalidate cached image
    cachedImage = null;
  }

  /**
   * Adds a success result with a specific value to the rolling window of statistics.
   *
   * @param value The value associated with the success
   */
  public void addSuccess(double value) {
    addResult(true, value);
  }

  /**
   * Gets the current image representation of the widget.
   * <p>
   * Uses a cached image if available, otherwise renders a new one.
   *
   * @return A BufferedImage containing the rendered statistics widget
   */
  public BufferedImage getImage() {
    if (cachedImage == null) {
      cachedImage = renderStatistics();
    }
    return cachedImage;
  }

  /**
   * Renders the statistics to a new BufferedImage.
   * <p>
   * This method creates a new image and draws the statistical data onto it. If there are no values
   * in the rolling window, an empty image is returned. For success results, additional statistics
   * about their values are displayed.
   *
   * @return A newly rendered BufferedImage of the statistics widget
   */
  public BufferedImage renderStatistics() {
    // Create either an opaque image with background color or a transparent image
    BufferedImage image =
        opaque ? GraphicsUtils.createBlankImage(width, height, theme.getWidgetBackground())
            : GraphicsUtils.createClearImage(width, height);

    // Only render statistics if we have values
    if (!values.isEmpty()) {
      Graphics2D g = image.createGraphics();
      GraphicsUtils.setupRenderingHints(g);

      // Draw the header
      GraphicsUtils.drawCenteredText(g, 0, padding, width, STATISTICS_HEIGHT, "Statistics",
          STATISTICS_FONT, theme.getText());

      // Prepare the lines of text to display
      List<String> lines = new ArrayList<>();

      // Calculate and add success rate
      double successRate =
          (100.0 * values.stream().filter(SuccessResult::success).count()) / values.size();
      lines.add("Success rate: " + successRate + "%");

      // If there are any successful results, calculate and add their statistics
      if (successRate > 0.0) { // NOPMD - AvoidLiteralsInIfCondition: clear in context
        // Generate statistics from successful values only
        DoubleStatistics stats = DoubleStatistics.of(
            EnumSet.of(Statistic.MIN, Statistic.MAX, Statistic.MEAN, Statistic.VARIANCE,
                Statistic.STANDARD_DEVIATION),
            values.stream().filter(SuccessResult::success).mapToDouble(SuccessResult::value)
                .toArray());

        // Add each statistic as a line
        lines.add("Minimum: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MIN)));
        lines.add("Maximum: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MAX)));
        lines.add("Mean: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MEAN)));
        lines.add(STD + ": " + String.format(DOUBLE_FORMAT,
            stats.getAsDouble(Statistic.STANDARD_DEVIATION)));
        lines.add(
            STD + SQR + ": " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.VARIANCE)));
      }

      // Calculate starting Y position for statistics display
      int cursorY = STATISTICS_HEIGHT + (height - STATISTICS_HEIGHT * lines.size()) / 2;

      // Draw each line of text
      for (String line : lines) {
        GraphicsUtils.drawCenteredText(g, 0, cursorY, width, STATISTICS_HEIGHT, line,
            STATISTICS_FONT, theme.getText());
        cursorY += STATISTICS_HEIGHT;
      }
    }

    // Return the rendered image
    return image;
  }

  /**
   * Builder class for creating RollingSuccessStatisticsWidget instances with a fluent API.
   * <p>
   * Supports configuration of width, height, padding, window size, opacity, and theme.
   */
  @Setter
  @Accessors(chain = true, fluent = true)
  public static class RollingSuccessStatisticsWidgetBuilder {

    /**
     * Default width of 200 pixels
     */
    private int width = 200;

    /**
     * Default height of 200 pixels
     */
    private int height = 200;

    /**
     * Default padding of 40 pixels
     */
    private int padding = 40;

    /**
     * Default window size of 200 values
     */
    private int windowSize = 200;

    /**
     * Default to opaque background
     */
    private boolean opaque = true;

    /**
     * Default to light theme
     */
    private Theme theme = Theme.LIGHT;

    /**
     * Builds and returns a new RollingSuccessStatisticsWidget with the configured parameters.
     *
     * @return A new RollingSuccessStatisticsWidget instance
     */
    public RollingSuccessStatisticsWidget build() {
      return new RollingSuccessStatisticsWidget(width, height, padding, windowSize, theme, opaque);
    }
  }

  /**
   * Record representing a success or failure result with an associated value.
   * <p>
   * For success results, the value is meaningful. For failures, the value is typically 0.0.
   *
   * @param success Whether the result represents a success
   * @param value   The value associated with the result
   */
  private record SuccessResult(boolean success, double value) {

  }
}
