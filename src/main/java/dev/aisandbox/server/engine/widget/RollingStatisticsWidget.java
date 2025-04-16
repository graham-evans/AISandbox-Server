package dev.aisandbox.server.engine.widget;

import static dev.aisandbox.server.engine.output.OutputConstants.STATISTICS_FONT;

import dev.aisandbox.server.engine.Theme;
import java.awt.Font;
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
 * Widget that shows statistics (mean/std/var) of a moving window o values.
 */
@Slf4j
public class RollingStatisticsWidget {

  private static final String DOUBLE_FORMAT = "%.2f";
  private static final int MARGIN = 5;
  private final int width;
  private final int height;
  private final int padding;
  private final int fontHeight;
  private final int titleFontSize;
  private final int windowSize;
  private final Theme theme;
  private final boolean opaque;
  private final Font font;
  private final Font titleFont;
  private final List<Double> values = new ArrayList<>();
  private final String STD = "\u03C3";
  private final String SQR = "\u00B2";

  private BufferedImage cachedImage = null;

  public RollingStatisticsWidget(int width, int height, int padding, Font font, Font titleFont,
      int windowSize, Theme theme, boolean opaque) {
    this.width = width;
    this.height = height;
    this.padding = padding;
    this.font = font;
    this.titleFont = titleFont;
    this.fontHeight = font.getSize();
    this.titleFontSize = titleFont.getSize();
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
      g.setFont(font);
      g.setColor(theme.getText());
      FontMetrics fm = g.getFontMetrics();
      // generate statistics
      DoubleStatistics stats = DoubleStatistics.of(
          EnumSet.of(Statistic.MIN, Statistic.MAX, Statistic.MEAN, Statistic.VARIANCE,
              Statistic.STANDARD_DEVIATION), values.stream().mapToDouble(d -> d).toArray());
      // draw statistics
      drawStringCentered("Statistics", g, fm, 0, font.getSize() + padding, width);
      int cursorY = fontHeight + (height - fontHeight * 5) / 2;
      drawStringCentered(
          "Minimum: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MIN)), g, fm, 0,
          cursorY, width);
      cursorY += fontHeight;
      drawStringCentered(
          "Maximum: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MAX)), g, fm, 0,
          cursorY, width);
      cursorY += fontHeight;
      drawStringCentered("Mean: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MEAN)),
          g, fm, 0, cursorY, width);
      cursorY += fontHeight;
      drawStringCentered(STD + ": " + String.format(DOUBLE_FORMAT,
          stats.getAsDouble(Statistic.STANDARD_DEVIATION)), g, fm, 0, cursorY, width);
      cursorY += fontHeight;
      drawStringCentered(
          STD + SQR + ": " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.VARIANCE)), g,
          fm, 0, cursorY, width);
    }
    // return image
    return image;
  }

  private void drawStringCentered(String text, Graphics2D g, FontMetrics fm, int x, int y,
      int width) {
    int dx = (width - fm.stringWidth(text)) / 2;
    g.drawString(text, x + dx, y);
  }

  @Setter
  @Accessors(chain = true, fluent = true)
  public static class RollingStatisticsWidgetBuilder {

    private int width = 200;
    private int height = 200;
    private int padding = 40;
    private int windowSize = 200;
    private boolean opaque = true;
    private Font font = STATISTICS_FONT;
    private Font titleFont = STATISTICS_FONT;
    private Theme theme = Theme.LIGHT;

    public RollingStatisticsWidget build() {
      return new RollingStatisticsWidget(width, height, padding, font, titleFont, windowSize, theme,
          opaque);
    }
  }
}
