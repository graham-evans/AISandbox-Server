package dev.aisandbox.server.engine.chart;

import lombok.Builder;
import lombok.Getter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Builder
public class RollingHistogramChart {
    // calculated fields moved to private class to avoid this issue with @Builder - https://github.com/projectlombok/lombok/issues/2307
    private final State state = new State();
    @Getter
    @Builder.Default
    private int dataWindow = 100;
    @Builder.Default
    private int width = 640;
    @Builder.Default
    private int height = 480;
    @Builder.Default
    private boolean cache = false;

    public void addScore(double score) {
        // update score list
        state.scores.add(score);
        while (state.scores.size() > dataWindow) {
            state.scores.remove(0);
        }
        // update image
        state.image = null;
    }

    public BufferedImage getImage() {
        if (!cache || (state.image == null)) {
            JFreeChart chart = createHistogram();
            state.image = chart.createBufferedImage(width, height);
        }
        return state.image;
    }

    private JFreeChart createHistogram() {

        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.RELATIVE_FREQUENCY);

        dataset.addSeries("Score", state.scores.stream().mapToDouble(value -> value).toArray(), state.scores.size());
        JFreeChart chart = ChartFactory.createHistogram("Score Frequency", "Score", "Frequency", dataset, PlotOrientation.VERTICAL, false, false, false);

        // customise the chart TODO
        return chart;
    }

    private static class State {
        protected BufferedImage image = null;
        protected List<Double> scores = new ArrayList<>();
    }
}
