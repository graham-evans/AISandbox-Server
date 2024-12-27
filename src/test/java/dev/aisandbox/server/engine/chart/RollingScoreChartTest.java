package dev.aisandbox.server.engine.chart;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RollingScoreChartTest {

    Random rand = new Random();

    @Test
    void builder() {
        RollingScoreChart rollingScoreChart = RollingScoreChart.builder()
                .dataWindow(10)
                .build();
        // add 15 entries
        for (int i = 0; i < 15; i++) {rollingScoreChart.addScore(rand.nextDouble());}
        assertEquals(10,rollingScoreChart.getDataWindow(),"Data window should be 10");
    }
}