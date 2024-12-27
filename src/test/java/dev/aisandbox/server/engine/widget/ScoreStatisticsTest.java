package dev.aisandbox.server.engine.widget;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScoreStatisticsTest {

    @Test
    public void testAverage() {
        ScoreStatistics stats = new ScoreStatistics(5);
        stats.addScore(1.0);
        stats.addScore(2.0);
        stats.addScore(3.0);
        assertEquals(2.0,stats.getCurrentMean(),"Current Mean");
        assertEquals(1.0,stats.getCurrentMin(),"Current Min");
        assertEquals(3.0,stats.getCurrentMax(),"Current Max");
    }

    @Test
    public void testAverageWindow() {
        ScoreStatistics stats = new ScoreStatistics(3);
        stats.addScore(1.0);
        stats.addScore(2.0);
        stats.addScore(3.0);
        stats.addScore(4.0);
        assertEquals(3.0,stats.getCurrentMean(),"Current Mean");
        assertEquals(2.0,stats.getCurrentMin(),"Current Min");
        assertEquals(4.0,stats.getCurrentMax(),"Current Max");
    }

    public void statisticsSummaryWidgetTest() {
        ScoreStatistics stats = new ScoreStatistics(10);
        TextWidget text = TextWidget.builder().statistics(stats).build();
        // insert text
        text.addText("Hello World");
        // add a score to the stats
        stats.addScore(1.0);
        // check the text has changed
        assertNotEquals(1,text.getLines().size());
    }

}