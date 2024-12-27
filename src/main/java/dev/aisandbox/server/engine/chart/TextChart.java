package dev.aisandbox.server.engine.chart;

import dev.aisandbox.server.engine.Theme;
import lombok.Builder;
import lombok.Getter;
import org.jfree.chart.JFreeChart;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Builder
public class TextChart {
    @Getter
    @Builder.Default
    private int dataWindow = 100;
    @Builder.Default
    private int width = 640;
    @Builder.Default
    private int height = 480;
    @Builder.Default
    private boolean cache = false;
    @Builder.Default
    private Theme theme = Theme.DEFAULT;

    // calculated fields moved to private class to avoid this issue with @Builder - https://github.com/projectlombok/lombok/issues/2307
    private final State state = new State();

    public void addText(final String text) {
        state.lines.add(text);
        while (state.lines.size() > state.lineCount) {
            state.lines.remove(0);
        }
        // update image
        state.image = null;
    }

    public BufferedImage getImage() {
        if (!cache || (state.image == null)) {
            state.image = renderText();
        }
        return state.image;
    }

    private BufferedImage renderText() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(theme.getBackground());
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, width, height);
        g.setColor(theme.getForeground());
        for (int i = 0; i < state.lines.size(); i++) {
            g.drawString(state.lines.get(i),0,(i+1)*12);
        }
        return image;
    }

    private static class State {
        protected BufferedImage image = null;
        protected List<String> lines = new ArrayList<>();
        protected int lineCount = 8;
        protected Font font = new Font("Arial", Font.PLAIN, 12);
    }
}
