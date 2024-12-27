package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.statistics.descriptive.DoubleStatistics;
import org.apache.commons.statistics.descriptive.Statistic;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Widget that shows statistics (mean/std/var) of a moving window o values.
 */
@Slf4j
public class RollingStatisticsWidget {
    private static final String DOUBLE_FORMAT = "%.2f";
    private static final int MARGIN = 5;
    private final int width;
    private final int height;
    private final int fontHeight;
    private final int windowSize;
    private final Theme theme;
    private final Font font;
    private final List<Double> values=new ArrayList<>();

    private BufferedImage cachedImage = null;

    public RollingStatisticsWidget(int width, int height, int fontHeight, String fontName, int windowSize, Theme theme) {
        this.width = width;
        this.height = height;
        this.fontHeight = fontHeight;
        font = new Font(fontName, Font.PLAIN, fontHeight);
        this.windowSize = windowSize;
        this.theme = theme;
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
        BufferedImage image = GraphicsUtils.createBlankImage(width, height, theme.getWidgetBackground());
        if (!values.isEmpty()) {
            Graphics2D g = image.createGraphics();
            g.setFont(font);
            g.setColor(theme.getText());
            // generate statistics
            DoubleStatistics stats = DoubleStatistics.of(
                    EnumSet.of(
                            Statistic.MIN,
                            Statistic.MAX,
                            Statistic.MEAN,
                            Statistic.VARIANCE,
                            Statistic.STANDARD_DEVIATION),
                    values.stream().mapToDouble(d -> d).toArray());
            // draw statistics
            int cursorY = MARGIN + fontHeight;
            g.drawString("Minimum: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MIN)), MARGIN, cursorY);
            cursorY += fontHeight;
            g.drawString("Maximum: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MAX)), MARGIN, cursorY);
            cursorY += fontHeight;
            g.drawString("Mean: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.MEAN)), MARGIN, cursorY);
            cursorY += fontHeight;
            g.drawString("Variance: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.VARIANCE)), MARGIN, cursorY);
            cursorY += fontHeight;
            g.drawString("Std Deviation: " + String.format(DOUBLE_FORMAT, stats.getAsDouble(Statistic.STANDARD_DEVIATION)), MARGIN, cursorY);
        }
        // return image
        return image;
    }

    @Setter
    @Accessors(chain = true, fluent = true)
    public static class RollingStatisticsWidgetBuilder {
        private int width = 200;
        private int height = 200;
        private int fontHeight = 14;
        private int windowSize = 200;
        private String fontName = "Ariel";
        private Theme theme = Theme.DEFAULT;

        public RollingStatisticsWidget build() {
            return new RollingStatisticsWidget(width, height, fontHeight, fontName, windowSize, theme);
        }
    }
}
