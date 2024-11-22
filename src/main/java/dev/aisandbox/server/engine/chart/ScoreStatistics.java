package dev.aisandbox.server.engine.chart;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.statistics.descriptive.DoubleStatistics;
import org.apache.commons.statistics.descriptive.Statistic;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@RequiredArgsConstructor
public class ScoreStatistics {

    private final int dataWindow;
    private List<Double> scores = new ArrayList<>();
    private int startIndex = 1;

    @Getter
    private double currentMean = 0.0;
    @Getter
    private double currentMin = 0.0;
    @Getter
    private double currentMax = 0.0;
    @Getter
    private double currentVar = 0.0;

    public void addScore(double score) {
        // update score list
       scores.add(score);
        while (scores.size() > dataWindow) {
            scores.remove(0);
            startIndex++;
        }
        // recalculate the statistics
        DoubleStatistics stats = DoubleStatistics.of(EnumSet.of(Statistic.MIN,Statistic.MAX,Statistic.MEAN,Statistic.VARIANCE),scores.stream().mapToDouble(d->d).toArray());
        currentMin=stats.getAsDouble(Statistic.MIN);
        currentMax=stats.getAsDouble(Statistic.MAX);
        currentVar=stats.getAsDouble(Statistic.VARIANCE);
        currentMean=stats.getAsDouble(Statistic.MEAN);
    }

}
