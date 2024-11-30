package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.StringJoiner;

@RequiredArgsConstructor
public class RollingPieChart implements ResetableWidget {
    private final int width;
    private final int height;
    private final Theme theme;
    private final RollingLabelFrequencyStatistics rollingLabelFrequencyStatistics;

    @Setter
    @Accessors(chain = true, fluent = true)
    public static class RollingPieChartBuilder {
        private int width = 200;
        private int height = 200;
        private Theme theme = Theme.DEFAULT;
        private RollingLabelFrequencyStatistics statistics = null;

        public RollingPieChart build() {
            return new RollingPieChart(width, height, theme, statistics);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", RollingValueChart.RollingScoreChartBuilder.class.getSimpleName() + "[", "]")
                    .add("width=" + width)
                    .add("height=" + height)
                    .add("theme=" + theme)
                    .add("statistics=" + statistics)
                    .toString();
        }
    }

    @Override
    public void reset() {

    }
}
