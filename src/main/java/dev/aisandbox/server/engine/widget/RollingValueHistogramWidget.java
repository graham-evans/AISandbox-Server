/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.maths.BinContents;
import dev.aisandbox.server.engine.maths.bins.BinningEngine;
import dev.aisandbox.server.engine.maths.bins.EqualWidthBinner;
import dev.aisandbox.server.engine.widget.axis.AxisScale;
import dev.aisandbox.server.engine.widget.axis.NiceAxisScale;
import dev.aisandbox.server.engine.widget.axis.TightAxisScale;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * A widget that displays a histogram of rolling values in a configurable time window.
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.NullAssignment") // null is used to invalidate a cached object - this is ok.
public class RollingValueHistogramWidget {

  // fields from builder
  private final int width;
  private final int height;
  private final int window;
  private final Theme theme;
  private final String title;
  private final String xAxisTitle;
  private final String yAxisTitle;
  private final BinningEngine binEngine;
  // internal fields
  private final List<Double> values = new ArrayList<>();
  private BufferedImage image = null;

  /**
   * Creates a new builder for configuring a rolling histogram widget.
   *
   * @return a new builder instance
   */
  public static RollingHistogramChartBuilder builder() {
    return new RollingHistogramChartBuilder();
  }

  /**
   * Adds a new value to the histogram window.
   *
   * @param value the value to add to the histogram
   */
  public void addValue(double value) {
    // add new value
    values.add(value);
    // remove extra values
    while (values.size() > window) {
      values.removeFirst();
    }
    // invalidate the image
    image = null;
  }

  /**
   * Returns the rendered image of the histogram.
   *
   * <p>The image is cached and regenerated only when new values are added.
   *
   * @return the histogram image as a BufferedImage
   */
  public BufferedImage getImage() {
    if (image == null) {
      if (values.isEmpty()) {
        image = GraphicsUtils.createBlankImage(width, height, theme.getBackground());
      } else {
        // calculate the 'bins'
        List<BinContents> bins = binEngine.binValues(values);
        // TODO choose to use density
        double maxValue = bins.stream().mapToDouble(value -> (double) value.quantity()).max()
            .orElse(1.0);
        // create the scales
        AxisScale xAxis = new TightAxisScale(bins.getFirst().binStart(), bins.getLast().binEnd(),
            width / 40);
        AxisScale yAxis = new NiceAxisScale(0.0, maxValue, 5);
        // draw the graph
        BaseGraph graph = new BaseGraph(width, height, title, xAxisTitle, yAxisTitle, theme, xAxis,
            yAxis);
        for (BinContents bin : bins) {
          if (bin.quantity() > 0) {
            graph.addBox(bin.binStart(), 0.0, bin.binEnd(), bin.quantity(), theme.getPrimary(),
                theme.getPrimary());
          }
        }
        graph.addAxisAndTitle();
        image = graph.getImage();
      }
    }
    return image;
  }

  /**
   * Builder for configuring rolling histogram widget parameters.
   */
  @Setter
  @Accessors(chain = true, fluent = true)
  public static class RollingHistogramChartBuilder {

    /**
     * The width of the final image.
     */
    private int width = 200;
    /**
     * The height of the final image.
     */
    private int height = 200;
    /**
     * The number of values to hold in memory.
     */
    private int window = 200;
    /**
     * The theme to use whilst drawing.
     */
    private Theme theme = Theme.LIGHT;
    /**
     * The Main title, shown at the top of the widget.
     */
    private String title = "Histogram Title";
    /**
     * The horizontal title, shown at the bottom of the widget.
     */
    private String xAxisTitle = "Score";
    /**
     * The vertical title, shown at the left of the widget.
     */
    private String yAxisTitle = "Frequency";
    /**
     * The binning engine, this reflects how the blocks in the graph will be drawn.
     */
    private BinningEngine binEngine = new EqualWidthBinner();

    /**
     * Builds the rolling histogram widget with the configured parameters.
     *
     * @return a new RollingValueHistogramWidget instance
     */
    public RollingValueHistogramWidget build() {
      return new RollingValueHistogramWidget(width, height, window, theme, title, xAxisTitle,
          yAxisTitle, binEngine);
    }

  }


}
