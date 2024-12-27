package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYChart;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class RollingValueChartWidget {
    // fields from the builder
    private final int width;
    private final int height;
    private final int window;
    private final String title;
    private final String xTitle;
    private final String yTitle;
    private final Theme theme;
    // internal fields
    private final List<Double> values = new ArrayList<>();
    private int startIndex = 1;
    private BufferedImage image = null;

    public static RollingScoreChartBuilder builder() {
        return new RollingScoreChartBuilder();
    }

    public void addValue(double value) {
        values.add(value);
        while (values.size() > window) {
            values.removeFirst();
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
        BufferedImage image = GraphicsUtils.createBlankImage(width, height, theme.getWidgetBackground());
        if (!values.isEmpty()) {
            double[] xData = new double[values.size()];
            Arrays.setAll(xData, index -> index + startIndex);
            double[] yData = values.stream().mapToDouble(value -> value).toArray();
            XYChart chart = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)", xData, yData);
            chart.setTitle(title);
            chart.getStyler().setLegendVisible(false);
            chart.setXAxisTitle(xTitle);
            chart.setYAxisTitle(yTitle);
            Graphics2D graphics = image.createGraphics();
            chart.paint(graphics, width, height);
        }
        return image;
    }


    @Setter
    @Accessors(chain = true, fluent = true)
    public static class RollingScoreChartBuilder {
        private int width = 200;
        private int height = 200;
        private int window = 200;
        private String title = "Values";
        private String yTitle = "Score";
        private String xTitle = "Episode";
        private Theme theme = Theme.DEFAULT;

        public RollingValueChartWidget build() {
            return new RollingValueChartWidget(width, height, window,title,xTitle,yTitle, theme);
        }
    }

}
