/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.widget;

import static dev.aisandbox.server.engine.output.OutputConstants.HD_WIDTH;
import static dev.aisandbox.server.engine.output.OutputConstants.TITLE_HEIGHT;

import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputConstants;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * A widget for displaying the title of a simulation.
 * <p>
 * This widget renders a title text centered horizontally across the full width of the display. The
 * title is rendered using the standard title font and is positioned at the top of the visualization
 * area. The widget generates a pre-rendered image that can be drawn efficiently during each frame.
 * </p>
 * <p>
 * The title widget uses the theme's background and text colors to ensure proper contrast and
 * consistency with the overall visual design.
 * </p>
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * TitleWidget titleWidget = TitleWidget.builder()
 *     .title("Multi-armed Bandit")
 *     .theme(Theme.DARK)
 *     .build();
 *
 * // Later, in the visualize method:
 * graphics2D.drawImage(titleWidget.getImage(), 0, TOP_MARGIN, null);
 * </pre>
 */
@Slf4j
public class TitleWidget {

  /**
   * The title text to display
   */
  private final String title;
  /**
   * The theme for color and styling
   */
  private final Theme theme;
  /**
   * The pre-rendered image containing the title
   */
  @Getter
  private final BufferedImage image;

  /**
   * Creates a new title widget with the specified title and theme.
   * <p>
   * The constructor immediately generates a pre-rendered image with the title text centered
   * horizontally. The image uses the full HD width and standard title height defined in
   * OutputConstants.
   * </p>
   *
   * @param title the text to display as the title
   * @param theme the visual theme for colors and styling
   */
  public TitleWidget(String title, Theme theme) {
    this.title = title;
    this.theme = theme;
    // generate image
    image = GraphicsUtils.createBlankImage(HD_WIDTH, TITLE_HEIGHT, theme.getBase());
    Graphics2D g = image.createGraphics();
    GraphicsUtils.setupRenderingHints(g);
    g.setColor(theme.getBase());
    g.fillRect(0, 0, HD_WIDTH, TITLE_HEIGHT);
    g.setColor(theme.getText());
    g.setFont(OutputConstants.TITLE_FONT);
    FontMetrics fm = g.getFontMetrics();
    int stringWidth = fm.stringWidth(title);
    g.drawString(title, HD_WIDTH / 2 - stringWidth / 2, TITLE_HEIGHT - fm.getDescent());
  }

  /**
   * Creates a new builder for constructing TitleWidget instances.
   *
   * @return a new TitleWidgetBuilder
   */
  public static TitleWidgetBuilder builder() {
    return new TitleWidgetBuilder();
  }

  /**
   * Builder class for creating TitleWidget instances with fluent syntax.
   * <p>
   * Provides default values for title ("Title") and theme (LIGHT), which can be overridden using
   * the fluent setter methods.
   * </p>
   */
  @Setter
  @Accessors(chain = true, fluent = true)
  public static class TitleWidgetBuilder {

    /**
     * Default title text
     */
    private String title = "Title";
    /**
     * Default theme
     */
    private Theme theme = Theme.LIGHT;

    /**
     * Builds a new TitleWidget with the configured parameters.
     *
     * @return a new TitleWidget instance
     */
    public TitleWidget build() {
      return new TitleWidget(title, theme);
    }
  }
}
