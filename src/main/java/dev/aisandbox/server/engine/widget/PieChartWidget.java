package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

@Slf4j
public class PieChartWidget {

    private final static int PADDING = 16; // pixel spacing around the outside
    private final static int TITLE_FONT_SIZE = 18;
    private final static Font TITLE_FONT = new Font("Arial", Font.BOLD, TITLE_FONT_SIZE);
    private final int width;
    private final int height;
    private final String title;
    private final Theme theme;
    private final Random random = new Random();
    @Getter
    private BufferedImage image;
    private List<Slice> segments = List.of();


    public PieChartWidget(int width, int height, String title, Theme theme) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.theme = theme;
        image = drawGraph();
    }

    public static PieChartWidgetBuilder builder() {
        return new PieChartWidgetBuilder();
    }

    public void setPie(List<Slice> segments) {
        this.segments = segments;
        image = drawGraph();
    }

    private BufferedImage drawGraph() {
        BufferedImage image = GraphicsUtils.createBlankImage(width, height, theme.getWidgetBackground());
        Graphics2D g = image.createGraphics();
        g.setColor(theme.getGraphBackground());
        g.fillRect(PADDING, PADDING, width - PADDING * 2, height - PADDING * 2);
        // add title
        GraphicsUtils.drawCenteredText(g, PADDING, PADDING, width - PADDING * 2, TITLE_FONT_SIZE, title, TITLE_FONT, theme.getText());
        // find center point
        int pieDiameter = Math.min(width - PADDING * 4, height - PADDING * 4 - TITLE_FONT_SIZE);
        int startX = (width - pieDiameter) / 2;
        int startY = (height - PADDING * 2 - TITLE_FONT_SIZE - pieDiameter) / 2 + PADDING + TITLE_FONT_SIZE;

        // draw pie chart
        // Calculate total value of all segments
        double totalValue = 0;
        for (Slice slice : segments) {
            totalValue += slice.value;
        }
        // draw each slice using drawArc and fillArc (degrees with 90' = north)
        double startAngle = 90;
        for (Slice slice : segments) {
            // Draw slice
            double value = slice.value / totalValue;
            double angle = value * 360;
            g.setColor(slice.baseColor);
            Shape arc = new Arc2D.Double(startX, startY, pieDiameter, pieDiameter, startAngle, -angle, Arc2D.PIE);
            Rectangle bounds = arc.getBounds();
            g.fill(arc);
            g.setColor(theme.getGraphBackground());
            g.draw(arc);
            startAngle -= angle;
        }
        // draw each title using sin/cos (radians with 0 = east)
        startAngle = -Math.PI / 2.0;
        g.setColor(theme.getText());
        for (Slice slice : segments) {
            double value = slice.value / totalValue;
            double angle = value * 360.0 * Math.PI / 180.0;
            // Draw the value in the middle of the arc
            double midAngle = startAngle + (angle / 2);
            int textX = (int) (startX + pieDiameter / 2 + Math.cos(midAngle) * pieDiameter / 3);
            int textY = (int) (startY + pieDiameter / 2 + Math.sin(midAngle) * pieDiameter / 3);

       //     g.fillOval(textX-3,textY-3,6,6);
            GraphicsUtils.drawCenteredText(g, textX - 20, textY - 10, 40, 20,
                    slice.title,
                   new Font("Arial", Font.PLAIN, 12),
                 theme.getText());
            startAngle += angle;
        }
        return image;
    }

    @Setter
    @Accessors(chain = true, fluent = true)
    public static class PieChartWidgetBuilder {
        private int width = 200;
        private int height = 200;
        private String title = "Pie Chart";
        private Theme theme = Theme.LIGHT;

        public PieChartWidget build() {
            return new PieChartWidget(width, height, title, theme);
        }

    }

    public record Slice(String title, double value, Color baseColor) {
    }
}
