package dev.aisandbox.server.engine.widget;

/**
 * Denotes a class that can collect and calculate statistics.
 */
@Deprecated
public interface StatisticReporter {
    /**
     * Set a text widget that can be used to report a summary of the statistics.
     * <p>
     * Every time the statistics change, the text widget should be reset and rewritten.
     *
     * @param reportingWidget The text widget to update with a summary of the collated statistics.
     */
    void setSummaryWidget(TextWidget reportingWidget);
}
