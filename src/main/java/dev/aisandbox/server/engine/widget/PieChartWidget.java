/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Widget for rendering pie charts as BufferedImages.
 *
 * <p>This widget creates a pie chart visualization with segments representing data values,
 * and renders it as an image that can be displayed or saved.
 */
@Slf4j
public class PieChartWidget {

  private final static int PADDING = 16; // pixel spacing around the outside
  private final static int TITLE_FONT_SIZE = 18;
  private final static Font TITLE_FONT = new Font("Arial", Font.BOLD, TITLE_FONT_SIZE);
  private final int width;
  private final int height;
  private final String title;
  private final Theme theme;
  @Getter
  private BufferedImage image;
  private List<Slice> segments = List.of();

  /**
   * Creates a new pie chart widget.
   *
   * @param width the width of the widget in pixels
   * @param height the height of the widget in pixels
   * @param title the title of the pie chart
   * @param theme the theme for colors and styling
   */
  public PieChartWidget(int width, int height, String title, Theme theme) {
    this.width = width;
    this.height = height;
    this.title = title;
    this.theme = theme;
    image = drawGraph();
  }

  private BufferedImage drawGraph() {
    BufferedImage image = GraphicsUtils.createBlankImage(width, height, theme.getBackground());
    Graphics2D g = image.createGraphics();
    g.setColor(theme.getBorder());
    // g.drawRect(PADDING, PADDING, width - PADDING * 2, height - PADDING * 2);
    g.drawRect(0, 0, width - 1, height - 1);
    // add title
    GraphicsUtils.drawCenteredText(g, PADDING, PADDING, width - PADDING * 2, TITLE_FONT_SIZE, title,
        TITLE_FONT, theme.getText());
    // find center point
    int pieDiameter = Math.min(width - PADDING * 4, height - PADDING * 4 - TITLE_FONT_SIZE);
    int startX = (width - pieDiameter) / 2;
    int startY =
        (height - PADDING * 2 - TITLE_FONT_SIZE - pieDiameter) / 2 + PADDING + TITLE_FONT_SIZE;

    // draw pie chart
    // Calculate total value of all segments
    double totalValue = 0;
    for (Slice slice : segments) {
      totalValue += slice.value;
    }
    // draw each slice using drawArc and fillArc (degrees with 90' = north)
    double startAngle = 90;
    for (int i = 0; i < segments.size(); i++) {
      Slice slice = segments.get(i);
      // Draw slice
      double value = slice.value / totalValue;
      double angle = value * 360;

      Shape arc = new Arc2D.Double(startX, startY, pieDiameter, pieDiameter, startAngle, -angle,
          Arc2D.PIE);
      Rectangle bounds = arc.getBounds();
      g.setColor(i % 2 == 0 ? theme.getPrimary() : theme.getSecondary());
      g.fill(arc);
      g.setColor(theme.getBorder());
      g.draw(arc);
      startAngle -= angle;
    }
    // draw each title using sin/cos (radians with 0 = east)
    startAngle = -Math.PI / 2.0;
    g.setColor(theme.getText());
    for (Slice slice : segments) {
      double value = slice.value / totalValue;
      double angle = value * 360.0 * Math.PI / 180.0;
      // Draw the value in the middle of the arc
      double midAngle = startAngle + (angle / 2);
      int textX = (int) (startX + pieDiameter / 2 + Math.cos(midAngle) * pieDiameter / 3);
      int textY = (int) (startY + pieDiameter / 2 + Math.sin(midAngle) * pieDiameter / 3);

      //     g.fillOval(textX-3,textY-3,6,6);
      GraphicsUtils.drawCenteredText(g, textX - 20, textY - 10, 40, 20, slice.title,
          OutputConstants.LOG_FONT, Color.WHITE);
      startAngle += angle;
    }
    return image;
  }

  /**
   * Creates a builder for constructing PieChartWidget instances.
   *
   * @return a new PieChartWidgetBuilder
   */
  public static PieChartWidgetBuilder builder() {
    return new PieChartWidgetBuilder();
  }

  /**
   * Updates the pie chart with new segments and refreshes the visualization.
   *
   * @param segments the list of segments to display
   */
  public void setPie(List<Slice> segments) {
    this.segments = segments;
    image = drawGraph();
  }

  /**
   * Builder for creating PieChartWidget instances with fluent API.
   */
  @Setter
  @Accessors(chain = true, fluent = true)
  public static class PieChartWidgetBuilder {

    private int width = 200;
    private int height = 200;
    private String title = "Pie Chart";
    private Theme theme = Theme.LIGHT;

    /**
     * Builds and returns a new PieChartWidget instance.
     *
     * @return a new PieChartWidget with the configured parameters
     */
    public PieChartWidget build() {
      return new PieChartWidget(width, height, title, theme);
    }

  }

  /**
   * Represents a single slice in the pie chart.
   */
  public record Slice(String title, double value, Color baseColor) {

  }
}
