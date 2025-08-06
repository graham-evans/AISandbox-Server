/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.widget.axis.AxisScale;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * A foundational class for creating graphs and charts within simulations.
 * <p>BaseGraph provides the infrastructure for drawing data visualization charts including axes,
 * gridlines, titles, and data plotting methods. It handles the complex calculations for mapping
 * data coordinates to screen coordinates and provides a clean API for adding graphical elements.
 * </p>
 * <p>The graph automatically scales data to fit within the available drawing area and includes
 * proper margins for axes labels and titles. Both X and Y axes support configurable scaling through
 * the AxisScale interface.
 * </p>
 * <p>Common usage pattern:
 * </p>
 * <pre>
 * // Create a graph with specific dimensions and axis scaling
 * BaseGraph graph = new BaseGraph(400, 300, "Score Over Time",
 *                                "Episode", "Score", theme,
 *                                xAxisScale, yAxisScale);
 *
 * // Add data lines
 * graph.addLine(0, 10, 100, 85, Color.BLUE);
 *
 * // Add axes and title
 * graph.addAxisAndTitle();
 *
 * // Use the rendered image
 * graphics.drawImage(graph.getImage(), x, y, null);
 * </pre>
 */
@Slf4j
public class BaseGraph {

  // Layout and styling constants
  /**
   * Pixel spacing around the outside of the graph
   */
  private final static int PADDING = 16;
  /**
   * Pixel spacing between graph elements
   */
  private final static int MARGIN = 3;
  /**
   * Font size for the main graph title
   */
  private final static int TITLE_FONT_SIZE = 18;
  /**
   * Font used for the main graph title
   */
  private final static Font TITLE_FONT = new Font("Arial", Font.BOLD, TITLE_FONT_SIZE);
  /**
   * Font size for axis labels
   */
  private final static int AXIS_FONT_SIZE = 12;
  /**
   * Font used for axis labels
   */
  private final static Font AXIS_FONT = new Font("Arial", Font.PLAIN, AXIS_FONT_SIZE);
  /**
   * Font size for tick mark labels
   */
  private final static int TICK_FONT_SIZE = 10;
  /**
   * Font used for tick mark labels
   */
  private final static Font TICK_FONT = new Font("Arial", Font.PLAIN, TICK_FONT_SIZE);
  /**
   * Dash pattern for gridlines
   */
  private final static float[] dash1 = {10.0f};
  /**
   * Dashed stroke style for gridlines
   */
  private final static BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
      BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

  // Graph configuration
  /**
   * Total width of the graph widget
   */
  private final int width;
  /**
   * Total height of the graph widget
   */
  private final int height;
  /**
   * Main title displayed at the top of the graph
   */
  private final String title;
  /**
   * Label for the X-axis
   */
  private final String xAxisTitle;
  /**
   * Label for the Y-axis
   */
  private final String yAxisTitle;
  /**
   * Visual theme for colors and styling
   */
  private final Theme theme;
  /**
   * Scaling configuration for the X-axis
   */
  private final AxisScale xAxisScale;
  /**
   * Scaling configuration for the Y-axis
   */
  private final AxisScale yAxisScale;

  // Rendered image and drawing context
  /**
   * The pre-rendered graph image
   */
  @Getter
  private final BufferedImage image;
  /**
   * Graphics context for drawing on the image
   */
  private final Graphics2D graphics;
  /**
   * X coordinate where the graph plotting area begins
   */
  private final int xBoxStart;
  /**
   * Width of the graph plotting area
   */
  private final int boxWidth;
  /**
   * Y coordinate where the graph plotting area begins
   */
  private final int yBoxStart;
  /**
   * Height of the graph plotting area
   */
  private final int boxHeight;


  /**
   * Creates a new BaseGraph with the specified dimensions, labels, and scaling.
   * <p>
   * This constructor initializes the graph with a background, calculates the plotting area
   * dimensions, and draws the gridlines. The resulting graph is ready for adding data through the
   * various add methods.
   * </p>
   * <p>
   * The constructor automatically:
   * </p>
   * <ul>
   *   <li>Calculates optimal margins for labels and titles</li>
   *   <li>Creates a background with the theme's widget background color</li>
   *   <li>Draws gridlines based on the axis scale tick marks</li>
   *   <li>Sets up the graphics context with proper rendering hints</li>
   * </ul>
   *
   * @param width      total width of the graph widget in pixels
   * @param height     total height of the graph widget in pixels
   * @param title      main title to display at the top of the graph
   * @param xAxisTitle label for the X-axis
   * @param yAxisTitle label for the Y-axis (will be rotated vertically)
   * @param theme      visual theme for colors and styling
   * @param xAxisScale scaling configuration for the X-axis
   * @param yAxisScale scaling configuration for the Y-axis
   */
  public BaseGraph(int width, int height, String title, String xAxisTitle, String yAxisTitle,
      Theme theme, AxisScale xAxisScale, AxisScale yAxisScale) {
    this.width = width;
    this.height = height;
    this.title = title;
    this.xAxisTitle = xAxisTitle;
    this.yAxisTitle = yAxisTitle;
    this.theme = theme;
    this.xAxisScale = xAxisScale;
    this.yAxisScale = yAxisScale;
    this.image = GraphicsUtils.createBlankImage(width, height, theme.getWidgetBackground());
    this.graphics = image.createGraphics();
    GraphicsUtils.setupRenderingHints(graphics);
    // calculate graph space
    xBoxStart = PADDING + AXIS_FONT_SIZE + TICK_FONT_SIZE + MARGIN * 4;
    boxWidth = width - xBoxStart - PADDING;
    yBoxStart = PADDING + TITLE_FONT_SIZE + MARGIN;
    boxHeight = height - yBoxStart - PADDING - AXIS_FONT_SIZE - MARGIN * 3 - TICK_FONT_SIZE;
    // draw graph background
    graphics.setColor(theme.getGraphBackground());
    graphics.fillRect(xBoxStart, yBoxStart, boxWidth, boxHeight);
    // draw x axis gridlines
    graphics.setColor(theme.getGraphOutlineColor());
    Stroke line = graphics.getStroke();
    graphics.setStroke(dashed);
    for (double x : xAxisScale.getTicks()) {
      int dx = (int) (boxWidth * xAxisScale.getScaledValue(x));
      graphics.drawLine(xBoxStart + dx, yBoxStart, xBoxStart + dx, yBoxStart + boxHeight);
    }
    // draw y axis gridlines
    for (double y : yAxisScale.getTicks()) {
      int dy = (int) (boxHeight * (1.0 - yAxisScale.getScaledValue(y)));
      graphics.drawLine(xBoxStart, yBoxStart + dy, xBoxStart + boxWidth, yBoxStart + dy);
    }
    graphics.setStroke(line);
  }

  /**
   * Draw a line on the graph in the selected colour.
   * <p>
   * X & Y coordinates are scaled to fit the diagram, then drawn.
   *
   * @param startX starting X point
   * @param startY starting Y point
   * @param endX   end X point
   * @param endY   end Y point
   * @param colour colour to use.
   */
  public void addLine(double startX, double startY, double endX, double endY, Color colour) {
    int x1 = (int) (xAxisScale.getScaledValue(startX) * boxWidth);
    int y1 = (int) (yAxisScale.getScaledValue(startY) * boxHeight);
    int x2 = (int) (xAxisScale.getScaledValue(endX) * boxWidth);
    int y2 = (int) (yAxisScale.getScaledValue(endY) * boxHeight);
    graphics.setColor(colour);
    graphics.drawLine(xBoxStart + x1, yBoxStart + boxHeight - y1, xBoxStart + x2,
        yBoxStart + boxHeight - y2);
  }

  public void addBox(double startX, double startY, double endX, double endY, Color fillColour,
      Color outlineColour) {
    int x1 = (int) (xAxisScale.getScaledValue(startX) * boxWidth);
    int y1 = (int) (yAxisScale.getScaledValue(startY) * boxHeight);
    int x2 = (int) (xAxisScale.getScaledValue(endX) * boxWidth);
    int y2 = (int) (yAxisScale.getScaledValue(endY) * boxHeight);
    int x = Math.min(x1, x2);
    int width = Math.abs(x1 - x2);
    int y = Math.max(y1, y2);
    int height = Math.abs(y1 - y2);
    graphics.setColor(fillColour);
    graphics.fillRect(xBoxStart + x, yBoxStart + boxHeight - y, width, height);
    graphics.setColor(outlineColour);
    graphics.drawRect(xBoxStart + x, yBoxStart + boxHeight - y, width, height);
  }


  public void addAxisAndTitle() {
    // draw main title
    GraphicsUtils.drawCenteredText(graphics, PADDING, PADDING, width - PADDING * 2, TITLE_FONT_SIZE,
        title, TITLE_FONT, theme.getText());
    // draw X axis title
    GraphicsUtils.drawCenteredText(graphics, xBoxStart, height - PADDING - AXIS_FONT_SIZE, boxWidth,
        AXIS_FONT_SIZE, xAxisTitle, AXIS_FONT, theme.getText());
    // draw Y axis title
    GraphicsUtils.drawVerticalCenteredText(graphics, PADDING, PADDING + TITLE_FONT_SIZE + MARGIN,
        AXIS_FONT_SIZE,
        height - PADDING * 2 - TITLE_FONT_SIZE - AXIS_FONT_SIZE - MARGIN - TITLE_FONT_SIZE,
        yAxisTitle, AXIS_FONT, theme.getText());
    // draw graph border
    graphics.setColor(theme.getGraphOutlineColor());
    graphics.drawRect(xBoxStart, yBoxStart, boxWidth, boxHeight);
    // draw x axis
    graphics.drawLine(xBoxStart, yBoxStart + boxHeight + MARGIN, xBoxStart + boxWidth,
        yBoxStart + boxHeight + MARGIN);
    for (double x : xAxisScale.getTicks()) {
      int dx = (int) (boxWidth * xAxisScale.getScaledValue(x));
      graphics.setColor(theme.getGraphOutlineColor());
      graphics.drawLine(xBoxStart + dx, yBoxStart + boxHeight + MARGIN, xBoxStart + dx,
          yBoxStart + boxHeight + MARGIN * 2);
      GraphicsUtils.drawCenteredText(graphics, xBoxStart + dx - 20,
          yBoxStart + boxHeight + MARGIN * 2, 40, TICK_FONT_SIZE, xAxisScale.getValueString(x),
          TICK_FONT, theme.getText());
    }
    // draw y axis
    graphics.setColor(theme.getGraphOutlineColor());
    graphics.drawLine(xBoxStart - MARGIN, yBoxStart, xBoxStart - MARGIN, yBoxStart + boxHeight);
    for (double y : yAxisScale.getTicks()) {
      int dy = (int) (boxHeight * (1.0 - yAxisScale.getScaledValue(y)));
      graphics.setColor(theme.getGraphOutlineColor());
      graphics.drawLine(xBoxStart - MARGIN * 2, yBoxStart + dy, xBoxStart - MARGIN, yBoxStart + dy);
      GraphicsUtils.drawVerticalCenteredText(graphics, xBoxStart - MARGIN * 3 - TICK_FONT_SIZE,
          yBoxStart + dy - 20, TICK_FONT_SIZE, 40, yAxisScale.getValueString(y), TICK_FONT,
          theme.getText());
    }
  }


}
