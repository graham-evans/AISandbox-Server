package dev.aisandbox.server.engine.widget;

import static dev.aisandbox.server.engine.output.OutputConstants.STATISTICS_FONT;
import static dev.aisandbox.server.engine.output.OutputConstants.STATISTICS_HEIGHT;

import dev.aisandbox.server.engine.Theme;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.statistics.descriptive.DoubleStatistics;
import org.apache.commons.statistics.descriptive.Statistic;

/**
 * Widget that shows statistics (mean/std/var) of a moving window of values.
 */
@Slf4j
public class RollingStatisticsWidget {

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

  public RollingStatisticsWidget(int width, int height, int padding, int windowSize, Theme theme,
      boolean opaque) {
    this.width = width;
    this.height = height;
    this.padding = padding;
    this.windowSize = windowSize;
    this.theme = theme;
    this.opaque = opaque;
  }

  public static RollingStatisticsWidgetBuilder builder() {
    return new RollingStatisticsWidgetBuilder();
  }

  public void addScore(double score) {
    // update score list
    values.add(score);
    // remove old values if window is full.
    while (values.size() > windowSize) {
      values.removeFirst();
    }
    // invalidate cached image
    cachedImage = null;
  }

  public BufferedImage getImage() {
    if (cachedImage == null) {
      cachedImage = renderStatistics();
    }
    return cachedImage;
  }

  public BufferedImage renderStatistics() {
    BufferedImage image =
        opaque ? GraphicsUtils.createBlankImage(width, height, theme.getWidgetBackground())
            : GraphicsUtils.createClearImage(width, height);
    if (!values.isEmpty()) {
      Graphics2D g = image.createGraphics();
      GraphicsUtils.setupRenderingHints(g);
      // generate statistics
      DoubleStatistics stats = DoubleStatistics.of(
          EnumSet.of(Statistic.MIN, Statistic.MAX, Statistic.MEAN, Statistic.VARIANCE,
              Statistic.STANDARD_DEVIATION), values.stream().mapToDouble(d -> d).toArray());
      // draw statistics
      GraphicsUtils.drawCenteredText(g,0,padding,width,STATISTICS_HEIGHT,"Statistics",STATISTICS_FONT,theme.getText());
      int cursorY = STATISTICS_HEIGHT + (height - STATISTICS_HEIGHT * 5) / 2;
      GraphicsUtils.drawCenteredText(g, 0, cursorY, width, STATISTICS_HEIGHT,
          "Minimum: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MIN)),
          STATISTICS_FONT, theme.getText());
      cursorY += STATISTICS_HEIGHT;
      GraphicsUtils.drawCenteredText(g, 0, cursorY, width, STATISTICS_HEIGHT,
          "Maximum: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MAX)),
          STATISTICS_FONT, theme.getText());
      cursorY += STATISTICS_HEIGHT;
      GraphicsUtils.drawCenteredText(g, 0, cursorY, width, STATISTICS_HEIGHT,
          "Mean: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MEAN)),
          STATISTICS_FONT, theme.getText());
      cursorY += STATISTICS_HEIGHT;
      GraphicsUtils.drawCenteredText(g, 0, cursorY, width, STATISTICS_HEIGHT,
          STD + ": " + String.format(DOUBLE_FORMAT,
              stats.getAsDouble(Statistic.STANDARD_DEVIATION)), STATISTICS_FONT, theme.getText());
      cursorY += STATISTICS_HEIGHT;
      GraphicsUtils.drawCenteredText(g, 0, cursorY, width, STATISTICS_HEIGHT,
          STD + SQR + ": " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.VARIANCE)),
          STATISTICS_FONT, theme.getText());
    }
    // return image
    return image;
  }

  @Setter
  @Accessors(chain = true, fluent = true)
  public static class RollingStatisticsWidgetBuilder {

    private int width = 200;
    private int height = 200;
    private int padding = 40;
    private int windowSize = 200;
    private boolean opaque = true;
    private Theme theme = Theme.LIGHT;

    public RollingStatisticsWidget build() {
      return new RollingStatisticsWidget(width, height, padding, windowSize, theme, opaque);
    }
  }
}
