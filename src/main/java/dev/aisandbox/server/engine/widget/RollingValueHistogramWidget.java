package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.math3.stat.Frequency;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.Histogram;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class RollingValueHistogramWidget {
    // fields from builder
    private final int width;
    private final int height;
    private final int window;
    private final int binCount=9;
    private final Theme theme;
    // internal fields
    private final List<Double> values = new ArrayList<>();
    private double minValue = Double.MAX_VALUE;
    private double maxValue = Double.MIN_VALUE;
    private BufferedImage image=null;

    public static RollingHistogramChartBuilder builder() {
        return new RollingHistogramChartBuilder();
    }

    public void addValue(double value) {
        // add new value
        values.add(value);
        // update min/max
        if (value < minValue) {minValue = value;}
        if (value > maxValue) {maxValue = value;}
        // remove extra values
        while (values.size() > window) {
            values.removeFirst();
        }
        // invalidate the image
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
            Histogram histogram = new Histogram(values,binCount,minValue,maxValue);
            CategoryChart chart = new CategoryChartBuilder().width(width).height(height).title("Xchart Histogram").xAxisTitle("Score").yAxisTitle("Frequency").build();
            chart.getStyler().setAvailableSpaceFill(.96);
            chart.getStyler().setOverlapped(false);
            chart.addSeries("histogram ", histogram.getxAxisData(), histogram.getyAxisData());
            chart.getStyler().setLegendVisible(false);
            Graphics2D g2d = image.createGraphics();
            chart.paint(g2d,width,height);
        }
        return image;
    }


    @Setter
    @Accessors(chain = true, fluent = true)
    public static class RollingHistogramChartBuilder {
        private int width = 200;
        private int height = 200;
        private int window = 200;
        private Theme theme = Theme.DEFAULT;

        public RollingValueHistogramWidget build() {
            return new RollingValueHistogramWidget(width, height, window, theme);
        }

    }
}
