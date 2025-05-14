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

  private TextWidget(int width, int height, Font font, Theme theme) {
    this.width = width;
    this.height = height;
    this.lineHeight = font.getSize() + 2;
    this.theme = theme;
    // create blank image
    image = GraphicsUtils.createBlankImage(width, height, theme.getWidgetBackground());
    graphics = image.createGraphics();
    blankLine = GraphicsUtils.createBlankImage(width - PADDING, lineHeight,
        theme.getWidgetBackground());
    // setup text
    graphics.setColor(theme.getText());
    graphics.setFont(font);
    GraphicsUtils.setupRenderingHints(graphics);
    fontMetrics = graphics.getFontMetrics();
    DESCENDER = fontMetrics.getDescent();
  }

  public static TextWidgetBuilder builder() {
    return new TextWidgetBuilder();
  }

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

  public void reset() {
    graphics.setColor(theme.getWidgetBackground());
    graphics.fillRect(0, 0, width, height);
    graphics.setColor(theme.getText());
  }

  @Setter
  @Accessors(chain = true, fluent = true)
  public static class TextWidgetBuilder {

    private int width = 200;
    private int height = 200;
    private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
    private Theme theme = Theme.LIGHT;

    public TextWidget build() {
      return new TextWidget(width, height, font, theme);
    }

  }

}
