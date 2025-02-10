package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class BaseGraph {
    // positional constants
    private final static int PADDING = 16; // pixel spacing around the outside
    private final static int MARGIN = 2; // pixel spacing between objects
    private final static int TITLE_FONT_SIZE = 18;
    private final static Font TITLE_FONT = new Font("Arial", Font.BOLD, TITLE_FONT_SIZE);
    private final static int AXIS_FONT_SIZE = 12;
    private final static Font AXIS_FONT = new Font("Arial", Font.PLAIN, AXIS_FONT_SIZE);
    private final static int TICK_FONT_SIZE = 10;
    private final static Font TICK_FONT = new Font("Arial", Font.PLAIN, TICK_FONT_SIZE);
    // graph inputs
    private final int width;
    private final int height;
    private final String title;
    private final String xAxisTitle;
    private final String yAxisTitle;
    private final Theme theme;
    private final NiceAxisScale xAxisScale;
    private final NiceAxisScale yAxisScale;
    // image fields
    @Getter
    private final BufferedImage image;
    private final Graphics2D graphics;
    private final int xBoxStart;
    private final int boxWidth;
    private final int yBoxStart;
    private final int boxHeight;


    public BaseGraph(int width, int height, String title, String xAxisTitle, String yAxisTitle, Theme theme, NiceAxisScale xAxisScale, NiceAxisScale yAxisScale) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.xAxisTitle = xAxisTitle;
        this.yAxisTitle = yAxisTitle;
        this.theme = theme;
        this.xAxisScale = xAxisScale;
        this.yAxisScale = yAxisScale;
        this.image = GraphicsUtils.createBlankImage(width, height, theme.getWidgetBackground());
        this.graphics = image.createGraphics();
        // calculate graph space
        xBoxStart = PADDING + AXIS_FONT_SIZE + MARGIN * 2;
        boxWidth = width - xBoxStart - PADDING;
        yBoxStart = PADDING + TITLE_FONT_SIZE + MARGIN;
        boxHeight = height - yBoxStart - PADDING - AXIS_FONT_SIZE - MARGIN * 5 - TICK_FONT_SIZE;
        // draw graph background
        graphics.setColor(theme.getGraphBackground());
        graphics.fillRect(xBoxStart, yBoxStart, boxWidth, boxHeight);
        graphics.setColor(theme.getGraphOutlineColor());
        graphics.drawRect(xBoxStart, yBoxStart, boxWidth, boxHeight);
        // draw x axis
        graphics.drawLine(xBoxStart, yBoxStart + boxHeight + MARGIN, xBoxStart + boxWidth, yBoxStart + boxHeight + MARGIN);
        for (double x : xAxisScale.getTicks()) {
            int dx = (int) (boxWidth * xAxisScale.getScaledValue(x));
            graphics.drawLine(xBoxStart + dx, yBoxStart + boxHeight + MARGIN, xBoxStart + dx, yBoxStart + boxHeight + MARGIN * 2);
            drawCenteredTest(xBoxStart + dx - 20, yBoxStart + boxHeight + MARGIN * 2, 40, TICK_FONT_SIZE, Double.toString(x), TICK_FONT, null);
        }
        // draw y axis
        graphics.drawLine(xBoxStart - MARGIN, yBoxStart, xBoxStart - MARGIN, yBoxStart + boxHeight);
        for (double y : yAxisScale.getTicks()) {
            int dy = (int) (boxHeight * (1.0 - yAxisScale.getScaledValue(y)));
            graphics.drawLine(xBoxStart - MARGIN, yBoxStart + dy, xBoxStart - MARGIN * 2, yBoxStart + dy);
        }
    }

    public void addAxisAndTitle() {
        drawCenteredTest(PADDING, PADDING, width - PADDING * 2, TITLE_FONT_SIZE, title, TITLE_FONT, Color.RED);
        drawCenteredTest(PADDING, height - PADDING - AXIS_FONT_SIZE, width - PADDING * 2, AXIS_FONT_SIZE, xAxisTitle, AXIS_FONT, Color.CYAN);
    }

    private void drawCenteredTest(int x, int y, int width, int height, String title, Font font, Color debugColour) {
        if (debugColour != null) {
            graphics.setColor(debugColour);
            graphics.fillRect(x, y, width, height);
        }
        graphics.setFont(font);
        graphics.setColor(theme.getText());

        FontMetrics metrics = graphics.getFontMetrics(font);
        int dx = (width - metrics.stringWidth(title)) / 2;

        graphics.drawString(title, x + dx, y + height);
    }

}
