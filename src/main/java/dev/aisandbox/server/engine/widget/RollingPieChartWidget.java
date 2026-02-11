/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.widget.PieChartWidget.Slice;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Widget for displaying a rolling pie chart that updates based on incoming values.
 *
 * <p>This widget maintains a sliding window of values and displays them as a pie chart,
 * automatically updating as new values are added and old values are removed.
 */
@SuppressWarnings("PMD.NullAssignment") // null is used to invalidate a cached object - this is ok.
@RequiredArgsConstructor
public class RollingPieChartWidget {

  private final int width;
  private final int height;
  private final int window;
  private final String title;
  private final Theme theme;
  private final List<String> values = new ArrayList<>();
  private final Map<String, Color> sliceColours = new HashMap<>();
  private BufferedImage image = null;

  /**
   * Creates a builder for constructing RollingPieChartWidget instances.
   *
   * @return a new RollingPieChartWidgetBuilder
   */
  public static RollingPieChartWidgetBuilder builder() {
    return new RollingPieChartWidgetBuilder();
  }

  /**
   * Adds a value to the rolling window.
   *
   * @param value the value to add
   * @param valueColour the color to use for this value in the pie chart
   */
  public void addValue(String value, Color valueColour) {
    values.add(value);
    sliceColours.put(value, valueColour);
    while (values.size() > window) {
      values.removeFirst();
    }
    image = null;
  }

  public BufferedImage getImage() {
    if (image == null) {
      PieChartWidget pie = new PieChartWidget(width, height, title, theme);
      // calculate slices
      TreeMap<String, Double> counts = new TreeMap<>();
      values.forEach(
          value -> counts.put(value, counts.compute(value, (k, v) -> (v == null) ? 1.0 : v + 1.0)));
      List<PieChartWidget.Slice> slices = new ArrayList<>();
      for (Entry<String, Double> entry : counts.entrySet()) {
        slices.add(new Slice(entry.getKey(), entry.getValue(), sliceColours.get(entry.getKey())));
      }
      pie.setPie(slices);
      image = pie.getImage();
    }
    return image;
  }

  /**
   * Builder for creating RollingPieChartWidget instances with fluent API.
   */
  @Setter
  @Accessors(chain = true, fluent = true)
  public static class RollingPieChartWidgetBuilder {

    private int width = 200;
    private int height = 200;
    private String title = "Pie Chart";
    private int window = 200;
    private Theme theme = Theme.LIGHT;

    /**
     * Builds and returns a new RollingPieChartWidget instance.
     *
     * @return a new RollingPieChartWidget with the configured parameters
     */
    public RollingPieChartWidget build() {
      return new RollingPieChartWidget(width, height, window, title, theme);
    }

  }
}
