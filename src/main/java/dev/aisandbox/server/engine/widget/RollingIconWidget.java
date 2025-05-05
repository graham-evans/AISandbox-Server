package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import lombok.experimental.Accessors;

public class RollingIconWidget {

  private final int width;
  private final int height;
  private final int iconWidth;
  private final int iconHeight;
  private final int iconPadding;
  private final int maxXCount;
  private final int maxMemory;
  private final boolean useCache;
  private final String title;
  private final Theme theme;
  private final List<BufferedImage> images = new ArrayList<>();

  private BufferedImage cachedImage = null;

  public RollingIconWidget(int width, int height, int iconWidth, int iconHeight, int iconPadding,
      boolean useCache, String title, Theme theme) {
    this.width = width;
    this.height = height;
    this.iconWidth = iconWidth;
    this.iconHeight = iconHeight;
    this.iconPadding = iconPadding;
    this.useCache = useCache;
    this.title = title;
    this.theme = theme;
    this.maxXCount = width / (iconWidth + iconPadding);
    int maxYCount = height / (iconHeight + iconPadding);
    this.maxMemory = maxXCount * maxYCount;
  }

  public static RollingIconWidgetBuilder builder() {
    return new RollingIconWidgetBuilder();
  }

  public void clearIcons() {
    images.clear();
    cachedImage = null;
  }

  public void addIcon(BufferedImage image) {
    assert (image.getWidth() == width && image.getHeight() == height);
    images.add(image);
    while (images.size() > maxMemory) {
      images.removeFirst();
    }
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
      Graphics2D g = image.createGraphics();
      drawImage(g, 0, 0);
      if (useCache) {
        cachedImage = image;
      }
    }
    return image;
  }

  /**
   * Draw the icons on an already existing Graphics2D object.
   * <p>
   * This will not use any cache.
   *
   * @param graphics2D The graphics context to draw with
   * @param originX    The horizontal origin
   * @param originY    The vertical origin
   */
  public void drawImage(Graphics2D graphics2D, int originX, int originY) {
    graphics2D.setColor(theme.getWidgetBackground());
    graphics2D.fillRect(originX, originY, width, height);
    for (int i = 0; i < images.size(); i++) {
      BufferedImage image = images.get(i);
      // work out position index
      int dx = i % maxXCount;
      int dy = i / maxXCount;
      // draw the image
      graphics2D.drawImage(image, originX + dx * iconWidth, originY + dy + iconHeight, null);
    }

  }

  @Setter
  @Accessors(chain = true, fluent = true)
  public static class RollingIconWidgetBuilder {

    private int width = 200;
    private int height = 200;
    private int iconWidth = 50;
    private int iconHeight = 50;
    private int iconPadding = 0;
    private boolean useCache = false;
    private String title = "Recent Icons";
    private Theme theme = Theme.LIGHT;

    public RollingIconWidget build() {
      return new RollingIconWidget(width, height, iconWidth, iconHeight, iconPadding, useCache,
          title, theme);
    }
  }

}
