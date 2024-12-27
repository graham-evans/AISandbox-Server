package dev.aisandbox.server.engine.widget;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Deprecated
@RequiredArgsConstructor
public class RollingLabelFrequencyStatistics implements StatisticReporter{
    private final int dataWindow;
    private final List<String> winnerList = new ArrayList<>();
    @Getter(AccessLevel.PROTECTED)
    private final Map<String, Integer> winnerMap = new TreeMap<>();
    private final List<ResetableWidget> resetableWidgets = new ArrayList<>();
    private TextWidget statisticsWidget = null;

    public void addWinner(String winnerName) {
        winnerList.add(winnerName);
        winnerMap.put(winnerName, winnerMap.computeIfAbsent(winnerName, k -> 0) + 1);
        while (winnerMap.size() > dataWindow) {
            String old = winnerList.removeFirst();
            winnerMap.put(old, winnerMap.get(old) - 1);
        }
        if (statisticsWidget != null) {
            statisticsWidget.reset();
            for (Map.Entry<String, Integer> entry : winnerMap.entrySet()) {
                statisticsWidget.addText(entry.getKey()+": "+(100.0*entry.getValue()/winnerList.size())+"%");
            }
        }
    }

    public TextWidget.TextWidgetBuilder createSummaryWidgetBuilder() {
        return TextWidget.builder().statistics(this);   }

    @Override
    public void setSummaryWidget(TextWidget reportingWidget) {
        statisticsWidget=reportingWidget;
    }
}
