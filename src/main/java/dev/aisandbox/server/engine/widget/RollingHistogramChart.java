package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.math3.stat.Frequency;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class RollingHistogramChart {
    private final int width;
    private final int height;
    private final int window;
    private final Theme theme;
    private final List<Double> values = new ArrayList<>();
    private BufferedImage image;

    public RollingHistogramChart(int width, int height, int window, Theme theme) {
        this.width = width;
        this.height = height;
        this.window = window;
        this.theme = theme;
        image = GraphicsUtils.createBlankImage(width, height, theme.getWidgetBackground());
    }

    public static RollingHistogramChartBuilder builder() {
        return new RollingHistogramChartBuilder();
    }

    public void addValue(double value) {
        values.add(value);
        while (values.size() > window) {
            values.removeFirst();
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
            Frequency frequency = new Frequency();
            values.forEach(frequency::addValue);

            Graphics2D g2d = image.createGraphics();
            g2d.setColor(theme.getText());
            g2d.drawLine(0, 0, width, height);
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

        public RollingHistogramChart build() {
            return new RollingHistogramChart(width, height, window, theme);
        }

    }
}
