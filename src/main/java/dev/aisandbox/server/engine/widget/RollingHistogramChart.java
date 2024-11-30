package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.StringJoiner;

public class RollingHistogramChart implements ResetableWidget {
    private final int width;
    private final int height;
    private final Theme theme;
    private final RollingValueStatistics statistics;
    private BufferedImage image = null;

    private RollingHistogramChart(int width, int height, Theme theme, RollingValueStatistics statistics) {
        this.width = width;
        this.height = height;
        this.theme = theme;
        this.statistics = statistics;
        image = createBlank();
        if (statistics != null) {
            statistics.getWidgets().add(this);
        }
    }

    protected static RollingHistogramChartBuilder builder() {
        return new RollingHistogramChartBuilder();
    }

    @Override
    public void reset() {
        image = null;
    }

    public BufferedImage getImage() {
        if (image == null) {
            JFreeChart chart = createHistogram();
            image = chart.createBufferedImage(width, height);
        }
        return image;
    }

    private BufferedImage createBlank() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(theme.getBackground());
        g.fillRect(0, 0, width, height);
        return image;
    }

    private JFreeChart createHistogram() {

        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.RELATIVE_FREQUENCY);

        dataset.addSeries("Score", statistics.getScores().stream().mapToDouble(value -> value).toArray(), statistics.getScores().size());
        JFreeChart chart = ChartFactory.createHistogram("Score Frequency", "Score", "Frequency", dataset, PlotOrientation.VERTICAL, false, false, false);

        // customise the chart TODO
        return chart;
    }

    @Setter
    @Accessors(chain = true, fluent = true)
    public static class RollingHistogramChartBuilder {
        private int width = 200;
        private int height = 200;
        private Theme theme = Theme.DEFAULT;
        private RollingValueStatistics statistics = null;

        public RollingHistogramChart build() {
            return new RollingHistogramChart(width, height, theme, statistics);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", RollingHistogramChartBuilder.class.getSimpleName() + "[", "]")
                    .add("width=" + width)
                    .add("height=" + height)
                    .add("theme=" + theme)
                    .add("statistics=" + statistics)
                    .toString();
        }
    }
}
