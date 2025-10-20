/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * A widget for displaying scrollable text content in simulation visualizations.
 * <p>
 * This widget provides a text display area with automatic line wrapping and scrolling
 * functionality. New text lines are added at the bottom while older lines scroll upward and are
 * eventually removed when they scroll out of view. This makes it ideal for displaying logs, status
 * messages, or streaming text content in simulations.
 * </p>
 * <p>
 * Key features:
 * </p>
 * <ul>
 *   <li>Automatic word wrapping for long text lines</li>
 *   <li>Vertical scrolling - new lines appear at bottom, old lines disappear at top</li>
 *   <li>Configurable dimensions, font, and theme support</li>
 *   <li>Thread-safe rendering to BufferedImage</li>
 * </ul>
 * <p>
 * Use the {@link TextWidgetBuilder} to configure and create instances:
 * </p>
 * <pre>{@code
 * TextWidget widget = TextWidget.builder()
 *     .width(400)
 *     .height(300)
 *     .font(new Font(Font.MONOSPACED, Font.PLAIN, 14))
 *     .theme(Theme.DARK)
 *     .build();
 *
 * widget.addText("Hello, world!");
 * widget.addText("This is a longer line that will be wrapped automatically");
 * BufferedImage image = widget.getImage();
 * }</pre>
 *
 * @see TextWidgetBuilder
 * @see Theme
 */
public class TextWidget {

  @Getter(AccessLevel.PACKAGE)
  private final int width;
  private final int height;
  private final int lineHeight;
  @Getter(AccessLevel.PACKAGE)
  private final Theme theme;
  private final int PADDING = 20;
  // internal state
  @Getter
  private final BufferedImage image;
  private final Graphics2D graphics;
  private final BufferedImage blankLine;
  private final FontMetrics fontMetrics;
  private final int DESCENDER;

  /**
   * Creates a new TextWidget with the specified configuration.
   * <p>
   * This constructor initializes the widget with a blank image of the specified dimensions, sets up
   * the graphics context with the provided font and theme, and prepares the internal state for text
   * rendering and scrolling.
   * </p>
   *
   * @param width  the width of the widget in pixels
   * @param height the height of the widget in pixels
   * @param font   the font to use for text rendering
   * @param theme  the theme providing colors and styling
   */
  private TextWidget(int width, int height, Font font, Theme theme) {
    this.width = width;
    this.height = height;
    this.lineHeight = font.getSize() + 2;
    this.theme = theme;
    // create blank image
    image = GraphicsUtils.createBlankImage(width, height, theme.getBackground());
    graphics = image.createGraphics();
    // draw border
    graphics.setColor(theme.getBorder());
    graphics.drawRect(0, 0, width - 1, height - 1);
    blankLine = GraphicsUtils.createBlankImage(width - PADDING - 1, lineHeight,
        theme.getBackground());
    // setup text
    graphics.setColor(theme.getText());
    graphics.setFont(font);
    GraphicsUtils.setupRenderingHints(graphics);
    fontMetrics = graphics.getFontMetrics();
    DESCENDER = fontMetrics.getDescent();
  }

  /**
   * Creates a new TextWidgetBuilder for configuring TextWidget instances.
   * <p>
   * The builder allows for fluent configuration of widget properties before creating the final
   * TextWidget instance.
   * </p>
   *
   * @return a new TextWidgetBuilder with default settings
   */
  public static TextWidgetBuilder builder() {
    return new TextWidgetBuilder();
  }

  /**
   * Adds a line of text to the widget.
   * <p>
   * If the text fits on a single line within the widget's width, it will be added as-is. If the
   * text is too long, it will be automatically wrapped at word boundaries. Each complete line
   * causes the existing content to scroll upward.
   * </p>
   * <p>
   * Word wrapping strategy:
   * </p>
   * <ul>
   *   <li>Text is split at space characters</li>
   *   <li>Words are added to lines until adding another word would exceed the width</li>
   *   <li>When a line is full, it's rendered and a new line is started</li>
   *   <li>Single words longer than the widget width are not broken</li>
   * </ul>
   *
   * @param text the text to add to the widget (may contain spaces for word wrapping)
   */
  public void addText(final String text) {
    // is this a simple line of text
    if (fontMetrics.stringWidth(text) <= width - PADDING * 2) {
      addTextLine(text);
    } else {
      // try to split the line-up
      StringBuilder line = new StringBuilder();
      for (String word : text.split(" ")) {
        if (line.isEmpty()) { // case 1 - first word always gets added
          line.append(word);
          line.append(" ");
        }
        if (fontMetrics.stringWidth(line + word)
            <= width - PADDING * 2) { // case 2 - room for another word
          line.append(word);
          line.append(" ");
        } else {
          // write the line
          addTextLine(line.toString());
          // start the next line
          line.setLength(0);
          line.append(word);
        }
      }
      if (!line.isEmpty()) {
        addTextLine(line.toString());
      }
    }

  }

  /**
   * Adds a single line of text to the widget and triggers scrolling.
   * <p>
   * This method performs the actual rendering of a text line. It scrolls the existing content
   * upward by one line height, clears the bottom line, and draws the new text.
   * </p>
   *
   * @param text the single line of text to render (should not contain line breaks)
   */
  private void addTextLine(final String text) {
    // move image one line up
    BufferedImage scrollImage = image.getSubimage(PADDING, PADDING + lineHeight, width - PADDING,
        height - PADDING * 2 - lineHeight);
    graphics.drawImage(scrollImage, PADDING, PADDING, null);
    // blank the bottom line
    graphics.drawImage(blankLine, PADDING, height - PADDING - lineHeight, null);
    // draw the text line
    graphics.drawString(text, PADDING, height - PADDING - DESCENDER);
  }

  /**
   * Clears all text from the widget and resets it to a blank state.
   * <p>
   * This method fills the entire widget area with the background color, effectively removing all
   * previously displayed text. The text color is then restored for subsequent text rendering.
   * </p>
   */
  public void reset() {
    graphics.setColor(theme.getBackground());
    graphics.fillRect(0, 0, width, height);
    graphics.setColor(theme.getText());
  }

  /**
   * Builder class for creating and configuring TextWidget instances.
   * <p>
   * This builder uses a fluent API pattern, allowing for method chaining to configure multiple
   * properties in a single expression. All methods return the builder instance for continued
   * chaining.
   * </p>
   * <p>
   * Default values:
   * </p>
   * <ul>
   *   <li>Width: 200 pixels</li>
   *   <li>Height: 200 pixels</li>
   *   <li>Font: Sans-serif, plain, 16pt</li>
   *   <li>Theme: LIGHT theme</li>
   * </ul>
   * <p>
   * Example usage:
   * </p>
   * <pre>{@code
   * TextWidget widget = TextWidget.builder()
   *     .width(300)
   *     .height(150)
   *     .font(new Font(Font.MONOSPACED, Font.BOLD, 12))
   *     .theme(Theme.DARK)
   *     .build();
   * }</pre>
   */
  @Setter
  @Accessors(chain = true, fluent = true)
  public static class TextWidgetBuilder {

    /**
     * Width of the widget in pixels.
     */
    private int width = 200;

    /**
     * Height of the widget in pixels.
     */
    private int height = 200;

    /**
     * Font to use for text rendering.
     */
    private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 16);

    /**
     * Theme providing colors and styling.
     */
    private Theme theme = Theme.LIGHT;

    /**
     * Creates a new TextWidget instance with the current builder configuration.
     * <p>
     * This method finalizes the builder configuration and creates an immutable TextWidget instance
     * ready for use.
     * </p>
     *
     * @return a new TextWidget configured with this builder's settings
     */
    public TextWidget build() {
      return new TextWidget(width, height, font, theme);
    }

  }

}
