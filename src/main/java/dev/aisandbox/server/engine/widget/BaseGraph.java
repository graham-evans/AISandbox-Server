package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.widget.axis.AxisScale;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseGraph {

  // positional constants
  private final static int PADDING = 16; // pixel spacing around the outside
  private final static int MARGIN = 3; // pixel spacing between objects
  private final static int TITLE_FONT_SIZE = 18;
  private final static Font TITLE_FONT = new Font("Arial", Font.BOLD, TITLE_FONT_SIZE);
  private final static int AXIS_FONT_SIZE = 12;
  private final static Font AXIS_FONT = new Font("Arial", Font.PLAIN, AXIS_FONT_SIZE);
  private final static int TICK_FONT_SIZE = 10;
  private final static Font TICK_FONT = new Font("Arial", Font.PLAIN, TICK_FONT_SIZE);
  private final static float[] dash1 = {10.0f};
  private final static BasicStroke dashed =
      new BasicStroke(1.0f,
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_MITER,
          10.0f, dash1, 0.0f);
  // graph inputs
  private final int width;
  private final int height;
  private final String title;
  private final String xAxisTitle;
  private final String yAxisTitle;
  private final Theme theme;
  private final AxisScale xAxisScale;
  private final AxisScale yAxisScale;
  // image fields
  @Getter
  private final BufferedImage image;
  private final Graphics2D graphics;
  private final int xBoxStart;
  private final int boxWidth;
  private final int yBoxStart;
  private final int boxHeight;


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
    // draw titles
    GraphicsUtils.drawCenteredText(graphics, PADDING, PADDING, width - PADDING * 2, TITLE_FONT_SIZE,
        title, TITLE_FONT, theme.getText());
    GraphicsUtils.drawCenteredText(graphics, PADDING, height - PADDING - AXIS_FONT_SIZE,
        width - PADDING * 2, AXIS_FONT_SIZE, xAxisTitle, AXIS_FONT, theme.getText());
    GraphicsUtils.drawCenteredText(graphics, PADDING, height - PADDING, height - PADDING * 2,
        AXIS_FONT_SIZE, yAxisTitle, AXIS_FONT, theme.getText());
    // draw graph border
    graphics.setColor(theme.getGraphOutlineColor());
    graphics.drawRect(xBoxStart, yBoxStart, boxWidth, boxHeight);
    // draw x axis
    graphics.drawLine(xBoxStart, yBoxStart + boxHeight + MARGIN, xBoxStart + boxWidth,
        yBoxStart + boxHeight + MARGIN);
    for (double x : xAxisScale.getTicks()) {
      int dx = (int) (boxWidth * xAxisScale.getScaledValue(x));
      graphics.drawLine(xBoxStart + dx, yBoxStart + boxHeight + MARGIN, xBoxStart + dx,
          yBoxStart + boxHeight + MARGIN * 2);
      GraphicsUtils.drawCenteredText(graphics, xBoxStart + dx - 20,
          yBoxStart + boxHeight + MARGIN * 2, 40, TICK_FONT_SIZE, xAxisScale.getValueString(x),
          TICK_FONT, theme.getText());
    }
    // draw y axis
    graphics.drawLine(xBoxStart - MARGIN, yBoxStart, xBoxStart - MARGIN, yBoxStart + boxHeight);
    for (double y : yAxisScale.getTicks()) {
      int dy = (int) (boxHeight * (1.0 - yAxisScale.getScaledValue(y)));
      graphics.drawLine(xBoxStart - MARGIN * 2, yBoxStart + dy, xBoxStart - MARGIN, yBoxStart + dy);
      drawVirticalCenteredTest(xBoxStart - MARGIN * 3 - TICK_FONT_SIZE, yBoxStart + dy + 20, 40,
          TICK_FONT_SIZE, yAxisScale.getValueString(y), TICK_FONT, Color.green);
    }
  }

  private void drawVirticalCenteredTest(int x, int y, int width, int height, String title,
      Font font, Color debugColour) {

    AffineTransform origTransform = graphics.getTransform();

    graphics.rotate(Math.toRadians(-90));
    graphics.translate(-y, x);

    if (debugColour != null) {
      graphics.setColor(debugColour);
      //        graphics.fillRect(0, 0, width, height);
    }

    graphics.setFont(font);
    graphics.setColor(theme.getText());

    FontMetrics metrics = graphics.getFontMetrics(font);
    int dx = (width - metrics.stringWidth(title)) / 2;

    graphics.drawString(title, dx, height);

    graphics.setTransform(origTransform);
  }
}
