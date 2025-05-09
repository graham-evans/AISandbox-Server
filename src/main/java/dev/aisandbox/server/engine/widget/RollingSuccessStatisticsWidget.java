package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.widget.RollingStatisticsWidget.RollingStatisticsWidgetBuilder;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import lombok.experimental.Accessors;

public class RollingSuccessStatisticsWidget {

  private static final String DOUBLE_FORMAT = "%.2f";
  private final int width;
  private final int height;
  private final int padding;
  private final int windowSize;
  private final Theme theme;
  private final boolean opaque;
  private final List<Double> values = new ArrayList<>();
  private final String STD = "\u03C3";
  private final String SQR = "\u00B2";

  private BufferedImage cachedImage = null;

  public RollingSuccessStatisticsWidget(int width, int height, int padding, int windowSize, Theme theme,
      boolean opaque) {
    this.width = width;
    this.height = height;
    this.padding = padding;
    this.windowSize = windowSize;
    this.theme = theme;
    this.opaque = opaque;
  }

  public static RollingSuccessStatisticsWidgetBuilder builder() {
    return new RollingSuccessStatisticsWidgetBuilder();
  }


  @Setter
  @Accessors(chain = true, fluent = true)
  public static class RollingSuccessStatisticsWidgetBuilder {

    private int width = 200;
    private int height = 200;
    private int padding = 40;
    private int windowSize = 200;
    private boolean opaque = true;
    private Theme theme = Theme.LIGHT;

    public RollingSuccessStatisticsWidget build() {
      return new RollingSuccessStatisticsWidget(width, height, padding, windowSize, theme, opaque);
    }
  }
}
