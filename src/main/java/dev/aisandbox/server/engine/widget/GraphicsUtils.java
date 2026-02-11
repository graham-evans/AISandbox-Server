/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import lombok.experimental.UtilityClass;

/**
 * Utility class providing common graphics operations and helper methods.
 * <p>
 * This class contains static methods for common graphics tasks such as creating images, setting up
 * rendering hints for high quality output, and drawing centered text. These utilities are used
 * throughout the simulation framework to ensure consistent visual quality and reduce code
 * duplication.
 * </p>
 * <p>
 * All methods are static and the class cannot be instantiated due to the {@code @UtilityClass}
 * annotation from Lombok.
 * </p>
 */
@UtilityClass
public class GraphicsUtils {

  /**
   * Creates a transparent image with the specified dimensions.
   * <p>
   * This method creates a BufferedImage with an alpha channel (ARGB) that is initially completely
   * transparent. This is useful for creating overlay images or images that will be composited over
   * other content.
   * </p>
   *
   * @param width  the width of the image in pixels
   * @param height the height of the image in pixels
   * @return a new transparent BufferedImage
   */
  public static BufferedImage createClearImage(int width, int height) {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    return image;
  }

  /**
   * Creates an image filled with a solid color.
   * <p>
   * This method creates a BufferedImage with an alpha channel and fills it entirely with the
   * specified color. This is commonly used for creating background images or solid color widgets.
   * </p>
   *
   * @param width  the width of the image in pixels
   * @param height the height of the image in pixels
   * @param color  the color to fill the image with
   * @return a new BufferedImage filled with the specified color
   */
  public static BufferedImage createBlankImage(int width, int height, Color color) {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    g.setColor(color);
    g.fillRect(0, 0, width, height);
    return image;
  }

  /**
   * Configures a Graphics2D context with high-quality rendering hints.
   * <p>
   * This method sets up various rendering hints to improve the visual quality of drawn content. It
   * enables antialiasing for smooth edges, configures text rendering for clarity, and sets
   * interpolation methods for better image scaling.
   * </p>
   * <p>
   * This should be called on any Graphics2D context used for simulation visualization to ensure
   * consistent, high-quality output across all simulations.
   * </p>
   *
   * @param g the Graphics2D context to configure
   */
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

  /**
   * Draws text vertically centered within a specified rectangle.
   * <p>
   * This method rotates the graphics context by 90 degrees counterclockwise and draws the text
   * centered within the rotated coordinate space. This is particularly useful for Y-axis labels
   * that need to be rotated.
   * </p>
   * <p>
   * The original graphics transformation is preserved and restored after drawing.
   * </p>
   *
   * @param graphics the Graphics2D context to draw on
   * @param x        the X coordinate of the rectangle's left edge
   * @param y        the Y coordinate of the rectangle's top edge
   * @param width    the width of the rectangle
   * @param height   the height of the rectangle
   * @param title    the text to draw
   * @param font     the font to use for the text
   * @param colour   the color to use for the text
   */
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

  /**
   * Draws text centered within the specified bounding box.
   *
   * @param graphics the graphics context to draw on
   * @param x the x-coordinate of the bounding box
   * @param y the y-coordinate of the bounding box
   * @param width the width of the bounding box
   * @param height the height of the bounding box
   * @param text the text to draw
   * @param font the font to use for rendering
   * @param colour the color to use for the text
   */
  public static void drawCenteredText(Graphics2D graphics, int x, int y, int width, int height,
      String text, Font font, Color colour) {
    graphics.setFont(font);
    graphics.setColor(colour);
    FontMetrics metrics = graphics.getFontMetrics(font);
    int dx = (width - metrics.stringWidth(text)) / 2;
    graphics.drawString(text, x + dx, y + height);
  }

}
