package dev.aisandbox.server.engine.widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GraphicsUtils {

  public static BufferedImage createClearImage(int width, int height) {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    return image;
  }

  public static BufferedImage createBlankImage(int width, int height, Color color) {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    g.setColor(color);
    g.fillRect(0, 0, width, height);
    return image;
  }

  public static void setupRenderingHints(Graphics2D g) {
    // antialiasing
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // alpha interpolation
    g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
        RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
    // colours
    g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
    // Dithering
    g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
    // fractional text metrics
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    // image interpolation
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    // rendering
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    // stroke normalization
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
    // text antialiasing
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
  }

  public static void drawCenteredText(Graphics2D graphics, int x, int y, int width, int height,
      String text, Font font, Color colour) {
    graphics.setFont(font);
    graphics.setColor(colour);
    FontMetrics metrics = graphics.getFontMetrics(font);
    int dx = (width - metrics.stringWidth(text)) / 2;
    graphics.drawString(text, x + dx, y + height);
  }

  public static void drawVerticalCenteredText(Graphics2D graphics, int x, int y, int width,
      int height, String title, Font font, Color colour) {
    // store original transformation
    AffineTransform origTransform = graphics.getTransform();
    // add transformation
    graphics.translate(x, y + height);
    graphics.rotate(Math.toRadians(-90));
    // draw centered text
    drawCenteredText(graphics, 0, 0, height, width, title, font, colour);
    // restore original transformation
    graphics.setTransform(origTransform);
  }

}
