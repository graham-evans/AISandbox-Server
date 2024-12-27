package dev.aisandbox.server.engine.chart;

import lombok.RequiredArgsConstructor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class RollingScoreChart {
    private final int window;
    private final int width;
    private final int height;
    private final boolean cache;

    private BufferedImage image = null;
    private List<Double> scores = new ArrayList<>();
    private int startIndex = 1;

    public void addScore(double score) {
        // update score list
        scores.add(score);
        while (scores.size() > window) {
            scores.remove(0);
            startIndex++;
        }
        // update image
        image = null;
    }

    public BufferedImage getImage() {
        if (!cache || (image == null)) {
            JFreeChart chart = createBarChart();
            image = chart.createBufferedImage(width, height);
        }
        return image;
    }

    private JFreeChart createBarChart() {
        return ChartFactory.createScatterPlot("Results","Rounds","score",getDataset());
    }

    private XYDataset getDataset() {
        XYSeries series = new XYSeries("Results");
        int index = startIndex;
        for (Double score : scores) {
            series.add(index, score);
            index++;
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }

}
