package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.maths.BinContents;
import dev.aisandbox.server.engine.maths.bins.BinningEngine;
import dev.aisandbox.server.engine.maths.bins.EqualWidthBinner;
import dev.aisandbox.server.engine.widget.axis.AxisScale;
import dev.aisandbox.server.engine.widget.axis.NiceAxisScale;
import dev.aisandbox.server.engine.widget.axis.TightAxisScale;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

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
    private final Theme theme;
    private final String title;
    private final String xAxisTitle;
    private final String yAxisTitle;
    private final BinningEngine binEngine;
    // internal fields
    private final List<Double> values = new ArrayList<>();
    private BufferedImage image = null;

    public static RollingHistogramChartBuilder builder() {
        return new RollingHistogramChartBuilder();
    }

    public void addValue(double value) {
        // add new value
        values.add(value);
        // remove extra values
        while (values.size() > window) {
            values.removeFirst();
        }
        // invalidate the image
        image = null;
    }

    public BufferedImage getImage() {
        if (image == null) {
            if (values.isEmpty()) {
                image = GraphicsUtils.createBlankImage(width, height, theme.getBackground());
            } else {
                // calculate the 'bins'
                List<BinContents> bins = binEngine.binValues(values);
                // TODO choose to use density
                double maxValue = bins.stream().mapToDouble(value -> (double) value.quantity()).max().orElse(1.0);
                // create the scales
                AxisScale xAxis = new TightAxisScale(bins.getFirst().binStart(), bins.getLast().binEnd(), width / 40);
                AxisScale yAxis = new NiceAxisScale(0.0, maxValue, 5);
                // draw the graph
                BaseGraph graph = new BaseGraph(width, height, title, xAxisTitle, yAxisTitle, theme, xAxis, yAxis);
                for (BinContents bin : bins) {
                    graph.addBox(bin.binStart(), 0.0, bin.binEnd(), bin.quantity(), theme.getAgent1Main(), theme.getAgent1Highlight());
                }
                graph.addAxisAndTitle();
                image = graph.getImage();
            }
        }
        return image;
    }





 /*   private BufferedImage renderImage() {
        BufferedImage image = GraphicsUtils.createBlankImage(width, height, theme.getWidgetBackground());
        if (!values.isEmpty()) {
            Histogram histogram = new Histogram(values,binCount,minValue,maxValue);
            CategoryChart chart = new CategoryChartBuilder().width(width).height(height).title(title).xAxisTitle(xAxisTitle).yAxisTitle(yAxisTitle).theme(theme.getChartTheme()).build();
            chart.getStyler().setAvailableSpaceFill(.96);
            chart.getStyler().setOverlapped(false);
            chart.addSeries("histogram ", histogram.getxAxisData(), histogram.getyAxisData());
            chart.getStyler().setLegendVisible(false);
            Graphics2D g2d = image.createGraphics();
            chart.paint(g2d,width,height);
        }
        return image;
    }
*/

    @Setter
    @Accessors(chain = true, fluent = true)
    public static class RollingHistogramChartBuilder {
        /**
         * The width of the final image.
         *
         * @Setter Set the width in pixels.
         * @Getter The width of the final image in pixels.
         */
        private int width = 200;
        /**
         * The height of the final image.
         *
         * @Setter Set the height in pixels.
         * @Setter The height in pixels.
         */
        private int height = 200;
        /**
         * The number of values to hold in memory
         */
        private int window = 200;
        /**
         * The theme to use whilst drawing.
         */
        private Theme theme = Theme.LIGHT;
        /**
         * The Main title, shown at the top of the widget
         */
        private String title = "Histogram Title";
        /**
         * The horizontal title, shown at the bottom of the widget.
         */
        private String xAxisTitle = "Score";
        /**
         * The vertical title, shown at the left of the widget.
         */
        private String yAxisTitle = "Frequency";
        /**
         * The binning engine, this reflects how the blocks in the graph will be drawn.
         */
        private BinningEngine binEngine = new EqualWidthBinner();

        public RollingValueHistogramWidget build() {
            return new RollingValueHistogramWidget(width, height, window, theme, title, xAxisTitle, yAxisTitle, binEngine);
        }

    }


}
