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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.statistics.descriptive.DoubleStatistics;
import org.apache.commons.statistics.descriptive.Statistic;

/**
 * Widget that shows statistics (mean/std/var) of a moving window of values.
 * <p>
 * This widget maintains a rolling window of numerical values and renders statistical information
 * about these values in a graphical format. Statistics displayed include minimum, maximum, mean,
 * standard deviation, and variance.
 * <p>
 * The widget supports custom sizing, padding, window size, and theming through its builder. It
 * implements caching to avoid unnecessary re-rendering of unchanged statistics.
 */
@Slf4j
@SuppressWarnings("PMD.NullAssignment") // null is used to invalidate a cached object - this is ok.
public class RollingStatisticsWidget {

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
   * Collection of values in the rolling window
   */
  private final List<Double> values = new ArrayList<>();

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
   * Constructs a new RollingStatisticsWidget with the specified parameters.
   *
   * @param width      Width of the widget in pixels
   * @param height     Height of the widget in pixels
   * @param padding    Padding from the edges in pixels
   * @param windowSize Maximum number of values to keep in the rolling window
   * @param theme      Visual theme for the widget
   * @param opaque     Whether the widget background should be opaque
   */
  public RollingStatisticsWidget(int width, int height, int padding, int windowSize, Theme theme,
      boolean opaque) {
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
   * @return A new RollingStatisticsWidgetBuilder instance
   */
  public static RollingStatisticsWidgetBuilder builder() {
    return new RollingStatisticsWidgetBuilder();
  }

  /**
   * Adds a new value to the rolling window of statistics.
   * <p>
   * If the window is full (i.e., the number of values equals windowSize), the oldest value is
   * removed. Adding a new value invalidates the cached image.
   *
   * @param score The new value to add to the rolling window
   */
  public void addScore(double score) {
    // update score list
    values.add(score);
    // remove old values if window is full.
    while (values.size() > windowSize) {
      values.removeFirst();
    }
    // invalidate cached image
    cachedImage = null;
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
   * in the rolling window, an empty image is returned.
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

      // Calculate statistical values from the current window of values
      DoubleStatistics stats = DoubleStatistics.of(
          EnumSet.of(Statistic.MIN, Statistic.MAX, Statistic.MEAN, Statistic.VARIANCE,
              Statistic.STANDARD_DEVIATION), values.stream().mapToDouble(d -> d).toArray());

      // Draw the header
      GraphicsUtils.drawCenteredText(g, 0, padding, width, STATISTICS_HEIGHT, "Statistics",
          STATISTICS_FONT, theme.getText());

      // Calculate starting Y position for statistics display
      int cursorY = STATISTICS_HEIGHT + (height - STATISTICS_HEIGHT * 5) / 2;

      // Draw minimum value
      GraphicsUtils.drawCenteredText(g, 0, cursorY, width, STATISTICS_HEIGHT,
          "Minimum: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MIN)),
          STATISTICS_FONT, theme.getText());
      cursorY += STATISTICS_HEIGHT;

      // Draw maximum value
      GraphicsUtils.drawCenteredText(g, 0, cursorY, width, STATISTICS_HEIGHT,
          "Maximum: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MAX)),
          STATISTICS_FONT, theme.getText());
      cursorY += STATISTICS_HEIGHT;

      // Draw mean value
      GraphicsUtils.drawCenteredText(g, 0, cursorY, width, STATISTICS_HEIGHT,
          "Mean: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MEAN)),
          STATISTICS_FONT, theme.getText());
      cursorY += STATISTICS_HEIGHT;

      // Draw standard deviation
      GraphicsUtils.drawCenteredText(g, 0, cursorY, width, STATISTICS_HEIGHT,
          STD + ": " + String.format(DOUBLE_FORMAT,
              stats.getAsDouble(Statistic.STANDARD_DEVIATION)), STATISTICS_FONT, theme.getText());
      cursorY += STATISTICS_HEIGHT;

      // Draw variance
      GraphicsUtils.drawCenteredText(g, 0, cursorY, width, STATISTICS_HEIGHT,
          STD + SQR + ": " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.VARIANCE)),
          STATISTICS_FONT, theme.getText());
    }
    // return image
    return image;
  }

  /**
   * Builder class for creating RollingStatisticsWidget instances with a fluent API.
   * <p>
   * Supports configuration of width, height, padding, window size, opacity, and theme.
   */
  @Setter
  @Accessors(chain = true, fluent = true)
  public static class RollingStatisticsWidgetBuilder {

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
     * Builds and returns a new RollingStatisticsWidget with the configured parameters.
     *
     * @return A new RollingStatisticsWidget instance
     */
    public RollingStatisticsWidget build() {
      return new RollingStatisticsWidget(width, height, padding, windowSize, theme, opaque);
    }
  }
}
