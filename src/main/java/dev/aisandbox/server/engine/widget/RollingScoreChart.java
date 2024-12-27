package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.image.BufferedImage;
import java.util.StringJoiner;

public class RollingScoreChart implements ResetableWidget {
    private final int width;
    private final int height;
    private final Theme theme;
    private final ScoreStatistics statistics;
    private BufferedImage image = null;

    private RollingScoreChart(int width, int height, Theme theme, ScoreStatistics statistics) {
        this.width = width;
        this.height = height;
        this.theme = theme;
        this.statistics = statistics;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        if (statistics != null) {
            statistics.getWidgets().add(this);
        }
    }

    protected static RollingScoreChartBuilder builder() {
        return new RollingScoreChartBuilder();
    }

    @Override
    public void reset() {
        image = null;
    }

    public BufferedImage getImage() {
        if (image == null) {
            JFreeChart chart = createBarChart();
            image = chart.createBufferedImage(width, height);
        }
        return image;
    }

    private JFreeChart createBarChart() {
        XYSeries series = new XYSeries("Results");
        int index = statistics.getStartIndex();
        for (Double score : statistics.getScores()) {
            series.add(index, score);
            index++;
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        JFreeChart chart = ChartFactory.createScatterPlot("Results", "Rounds", "score", dataset, PlotOrientation.VERTICAL, false, false, false);
        // customise the chart TODO
        chart.setBackgroundPaint(theme.getBackground());
        return chart;
    }

    @Setter
    @Accessors(chain = true, fluent = true)
    public static class RollingScoreChartBuilder {
        private int width = 200;
        private int height = 200;
        private Theme theme = Theme.DEFAULT;
        private ScoreStatistics statistics = null;

        public RollingScoreChart build() {
            return new RollingScoreChart(width, height, theme, statistics);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", RollingScoreChartBuilder.class.getSimpleName() + "[", "]")
                    .add("width=" + width)
                    .add("height=" + height)
                    .add("theme=" + theme)
                    .add("statistics=" + statistics)
                    .toString();
        }
    }

}
