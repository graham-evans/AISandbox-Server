package dev.aisandbox.server.engine.chart;

import dev.aisandbox.server.engine.Theme;
import lombok.Builder;
import lombok.Getter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Builder
public class RollingScoreChart {
    @Getter
    @Builder.Default
    private int dataWindow = 100;
    @Builder.Default
    private int width = 640;
    @Builder.Default
    private int height = 480;
    @Builder.Default
    private boolean cache = false;
    @Builder.Default
    private Theme theme = Theme.DEFAULT;

    // calculated fields moved to private class to avoid this issue with @Builder - https://github.com/projectlombok/lombok/issues/2307
    private final State state = new State();

    public void addScore(double score) {
        // update score list
        state.scores.add(score);
        while (state.scores.size() > dataWindow) {
            state.scores.remove(0);
            state.startIndex++;
        }
        // update image
        state.image = null;
    }

    public BufferedImage getImage() {
        if (!cache || (state.image == null)) {
            JFreeChart chart = createBarChart();
            state.image = chart.createBufferedImage(width, height);
        }
        return state.image;
    }

    private JFreeChart createBarChart() {
        XYSeries series = new XYSeries("Results");
        int index = state.startIndex;
        for (Double score : state.scores) {
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


    private static class State {
        protected BufferedImage image = null;
        protected List<Double> scores = new ArrayList<>();
        protected int startIndex = 1;
    }

}
