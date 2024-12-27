package dev.aisandbox.server.engine.widget;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.statistics.descriptive.DoubleStatistics;
import org.apache.commons.statistics.descriptive.Statistic;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@RequiredArgsConstructor
public class ScoreStatistics {

    private final int dataWindow;
    @Getter(AccessLevel.PROTECTED)
    private final List<Double> scores = new ArrayList<>();
    @Getter(AccessLevel.PROTECTED)
    private final List<ResetableWidget> widgets = new ArrayList<>();
    @Getter(AccessLevel.PROTECTED)
    private int startIndex = 1;
    @Setter(AccessLevel.PROTECTED)
    private TextWidget summaryWidget = null;

    @Getter
    private double currentMean = 0.0;
    @Getter
    private double currentMin = 0.0;
    @Getter
    private double currentMax = 0.0;
    @Getter
    private double currentVar = 0.0;
    @Getter
    private double currentStdDev = 0.0;

    public void addScore(double score) {
        // update score list
        scores.add(score);
        while (scores.size() > dataWindow) {
            scores.removeFirst();
            startIndex++;
        }
        // recalculate the statistics
        DoubleStatistics stats = DoubleStatistics.of(
                EnumSet.of(
                        Statistic.MIN,
                        Statistic.MAX,
                        Statistic.MEAN,
                        Statistic.VARIANCE,
                        Statistic.STANDARD_DEVIATION),
                scores.stream().mapToDouble(d -> d).toArray());
        currentMin = stats.getAsDouble(Statistic.MIN);
        currentMax = stats.getAsDouble(Statistic.MAX);
        currentVar = stats.getAsDouble(Statistic.VARIANCE);
        currentMean = stats.getAsDouble(Statistic.MEAN);
        currentStdDev = stats.getAsDouble(Statistic.STANDARD_DEVIATION);
        // reset any widgets
        widgets.forEach(ResetableWidget::reset);
        // write summary
        if (summaryWidget != null) {
            summaryWidget.reset();
            summaryWidget.addText("Mean: " + String.format("%.4f", currentMean));
            summaryWidget.addText("Min: " + String.format("%.4f", currentMin));
            summaryWidget.addText("Max: " + String.format("%.4f", currentMax));
            summaryWidget.addText("Var: " + String.format("%.4f", currentVar));
            summaryWidget.addText("StdDev: " + String.format("%.4f", currentStdDev));
        }
    }

    public TextWidget.TextWidgetBuilder createSummaryWidgetBuilder() {
        return TextWidget.builder().statistics(this);
    }

    public RollingScoreChart.RollingScoreChartBuilder createScoreChartBuilder() {
        return RollingScoreChart.builder().statistics(this);
    }


}
