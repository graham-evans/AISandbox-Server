package dev.aisandbox.server.engine.widget;

import static dev.aisandbox.server.engine.output.OutputConstants.STATISTICS_FONT;
import static dev.aisandbox.server.engine.output.OutputConstants.STATISTICS_HEIGHT;

import dev.aisandbox.server.engine.Theme;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.statistics.descriptive.DoubleStatistics;
import org.apache.commons.statistics.descriptive.Statistic;

public class RollingSuccessStatisticsWidget {

  private static final String DOUBLE_FORMAT = "%.2f";
  private final int width;
  private final int height;
  private final int padding;
  private final int windowSize;
  private final Theme theme;
  private final boolean opaque;
  private final List<SuccessResult> values = new ArrayList<>();
  private final String STD = "\u03C3";
  private final String SQR = "\u00B2";

  private BufferedImage cachedImage = null;

  public RollingSuccessStatisticsWidget(int width, int height, int padding, int windowSize,
      Theme theme, boolean opaque) {
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

  public void addFailure() {
    addResult(false, 0.0);
  }

  public void addSuccess(double value) {
    addResult(true, value);
  }

  public void addResult(boolean success, double value) {
    values.add(new SuccessResult(success, value));
    while (values.size() > windowSize) {
      values.removeFirst();
    }
    // delete the cache
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
      // draw title
      GraphicsUtils.drawCenteredText(g,0,padding,width,STATISTICS_HEIGHT,"Statistics",STATISTICS_FONT,theme.getText());
      // work out the text to add
      List<String> lines = new ArrayList<>();
      double successRate =
          (100.0 * values.stream().filter(SuccessResult::success).count()) / values.size();
      lines.add("Success rate: " + successRate + "%");
      if (successRate > 0.0) {
        // generate statistics
        DoubleStatistics stats = DoubleStatistics.of(
            EnumSet.of(Statistic.MIN, Statistic.MAX, Statistic.MEAN, Statistic.VARIANCE,
                Statistic.STANDARD_DEVIATION),
            values.stream().filter(SuccessResult::success).mapToDouble(SuccessResult::value)
                .toArray());
        lines.add("Minimum: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MIN)));
        lines.add("Maximum: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MAX)));
        lines.add("Mean: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MEAN)));
        lines.add(STD + ": " + String.format(DOUBLE_FORMAT,
            stats.getAsDouble(Statistic.STANDARD_DEVIATION)));
        lines.add(
            STD + SQR + ": " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.VARIANCE)));
      }
      // work out the starting point
      int cursorY = STATISTICS_HEIGHT + (height - STATISTICS_HEIGHT * lines.size()) / 2;
      for (String line : lines) {
        GraphicsUtils.drawCenteredText(g, 0, cursorY, width, STATISTICS_HEIGHT, line,
            STATISTICS_FONT, theme.getText());
        cursorY += STATISTICS_HEIGHT;
      }
    }
    // return image
    return image;
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

  private record SuccessResult(boolean success, double value) {

  }
}
