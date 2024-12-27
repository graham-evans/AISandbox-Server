package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYChart;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RollingValueChart {
    private final int width;
    private final int height;
    private final int window;
    private final List<Double> values = new ArrayList<>();
    private final Theme theme;
    private int startIndex = 1;
    private BufferedImage image = null;

    private RollingValueChart(int width, int height, int window, Theme theme) {
        this.width = width;
        this.height = height;
        this.theme = theme;
        this.window = window;
        image = GraphicsUtils.createBlankImage(width, height, theme.getWidgetBackground());
    }

    public static RollingScoreChartBuilder builder() {
        return new RollingScoreChartBuilder();
    }

    public void addValue(double value) {
        values.add(value);
        while (values.size() > window) {
            values.remove(0);
            startIndex++;
        }
        image = null;
    }


    public BufferedImage getImage() {
        if (image == null) {
            image = renderImage();
        }
        return image;
    }

    private BufferedImage renderImage() {
        double[] xData = new double[values.size()];
        Arrays.setAll(xData, index -> index+startIndex);
        double[] yData = values.stream().mapToDouble(value -> value).toArray();
        XYChart chart = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)", xData, yData);
    }


    @Setter
    @Accessors(chain = true, fluent = true)
    public static class RollingScoreChartBuilder {
        private int width = 200;
        private int height = 200;
        private int window = 200;
        private Theme theme = Theme.DEFAULT;

        public RollingValueChart build() {
            return new RollingValueChart(width, height, window, theme);
        }


    }

}
