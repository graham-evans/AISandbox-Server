/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.widget;

import static dev.aisandbox.server.engine.output.OutputConstants.WIDGET_TITLE_FONT;
import static dev.aisandbox.server.engine.output.OutputConstants.WIDGET_TITLE_HEIGHT;

import dev.aisandbox.server.engine.Theme;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Widget for displaying a rolling grid of icons.
 *
 * <p>This widget displays small icons in a grid layout, maintaining a maximum number of icons
 * in memory. Old icons are automatically removed when the limit is exceeded.
 */
@Slf4j
@SuppressWarnings("PMD.NullAssignment") // null is used to invalidate a cached object - this is ok.
public class RollingIconWidget {

  private final static int PADDING = 16;
  private final int width;
  private final int height;
  private final int iconWidth;
  private final int iconHeight;
  private final int maxXCount;
  private final int maxMemory;
  private final boolean useCache;
  private final String title;
  private final Theme theme;
  private final List<BufferedImage> images = new ArrayList<>();
  private final int iconOriginX;
  private final int iconOriginY;

  private BufferedImage cachedImage = null;

  /**
   * Creates a new rolling icon widget.
   *
   * @param width the width of the widget in pixels
   * @param height the height of the widget in pixels
   * @param iconWidth the width of each icon
   * @param iconHeight the height of each icon
   * @param useCache whether to cache the rendered image
   * @param title the title of the widget
   * @param theme the theme for colors and styling
   */
  public RollingIconWidget(int width, int height, int iconWidth, int iconHeight, boolean useCache,
      String title, Theme theme) {
    this.width = width;
    this.height = height;
    this.iconWidth = iconWidth;
    this.iconHeight = iconHeight;
    this.useCache = useCache;
    this.title = title;
    this.theme = theme;
    this.maxXCount = (width - PADDING * 2) / iconWidth;
    int maxYCount = (height - WIDGET_TITLE_HEIGHT - PADDING * 3) / iconHeight;
    this.maxMemory = maxXCount * maxYCount;
    log.debug("Setting up rolling icon widget with max number of icons = {}", maxMemory);
    iconOriginX = (width - PADDING * 2 - iconWidth * maxXCount) / 2 + PADDING;
    iconOriginY = (height - WIDGET_TITLE_HEIGHT - PADDING * 3 - iconHeight * maxYCount) / 2
        + WIDGET_TITLE_HEIGHT + PADDING * 2;
  }

  /**
   * Creates a builder for constructing RollingIconWidget instances.
   *
   * @return a new RollingIconWidgetBuilder
   */
  public static RollingIconWidgetBuilder builder() {
    return new RollingIconWidgetBuilder();
  }

  /**
   * Clears all icons from the widget.
   */
  public void clearIcons() {
    log.info("Clear icons");
    images.clear();
    cachedImage = null;
  }

  /**
   * Adds an icon to the widget.
   *
   * @param iconImage the icon image to add
   */
  public void addIcon(@NonNull BufferedImage iconImage) {
    assert (iconImage.getWidth() == iconWidth && iconImage.getHeight() == iconHeight);
    images.add(iconImage);
    while (images.size() > maxMemory) {
      images.removeFirst();
    }
    log.info("Adding icon: {} / {}", images.size(), maxMemory);
    cachedImage = null;
  }

  /**
   * Returned the (possibly cached) image with all the current icons.
   *
   * @return The BufferedImage of icons
   */
  public BufferedImage getImage() {
    BufferedImage image = cachedImage;
    if (image == null) {
      image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics2D = image.createGraphics();
      GraphicsUtils.setupRenderingHints(graphics2D);
      graphics2D.setColor(theme.getBackground());
      graphics2D.fillRect(0, 0, width, height);
      graphics2D.setColor(theme.getText());
      GraphicsUtils.drawCenteredText(graphics2D, 0, PADDING, width, WIDGET_TITLE_HEIGHT, title,
          WIDGET_TITLE_FONT, theme.getText());
      log.info("Drawing image: {} / {}", images.size(), maxMemory);
      for (int i = 0; i < images.size(); i++) {
        BufferedImage icon = images.get(i);
        // work out position index
        int dx = i % maxXCount;
        int dy = i / maxXCount;
        // draw the image
        graphics2D.drawImage(icon, iconOriginX + dx * iconWidth, iconOriginY + dy * iconHeight,
            null);
      }
      if (useCache) {
        cachedImage = image;
      }
    }
    return image;
  }

  /**
   * Builder for creating RollingIconWidget instances with fluent API.
   */
  @Setter
  @Accessors(chain = true, fluent = true)
  public static class RollingIconWidgetBuilder {

    private int width = 200;
    private int height = 200;
    private int iconWidth = 50;
    private int iconHeight = 50;
    private boolean useCache = false;
    private String title = "Recent Icons";
    private Theme theme = Theme.LIGHT;

    /**
     * Builds and returns a new RollingIconWidget instance.
     *
     * @return a new RollingIconWidget with the configured parameters
     */
    public RollingIconWidget build() {
      return new RollingIconWidget(width, height, iconWidth, iconHeight, useCache, title, theme);
    }
  }

}
