package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class TextWidget {
    @Getter(AccessLevel.PACKAGE)
    private final List<String> lines = new ArrayList<>();
    private final int width;
    private final int height;
    private final double lineHeight;
    @Getter(AccessLevel.PACKAGE)
    private final Theme theme;
    private final Font font;
    private final int maxLines;
    // internal state
    private BufferedImage image = null;

    private TextWidget(int width, int height, int fontHeight, String fontName, Theme theme) {
        this.width = width;
        this.height = height;
        this.lineHeight = fontHeight * 1.1;
        this.theme = theme;
        // create font
        font = new Font(fontName, Font.PLAIN, fontHeight);
        maxLines = (int)(height / lineHeight);
    }

    public static TextWidgetBuilder builder() {
        return new TextWidgetBuilder();
    }

    public void addText(final String text) {
        lines.add(text);
        while (lines.size() > maxLines) {
            lines.removeFirst();
        }
        // invalidate image
        image = null;
    }

    public void reset() {
        lines.clear();
        image = null;
    }

    public BufferedImage getImage() {
        if (image == null) {
            image = renderText();
        }
        return image;
    }

    private BufferedImage renderText() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        GraphicsUtils.setupRenderingHints(g);
        g.setColor(theme.getBackground());
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, width, height);
        g.setColor(theme.getText());
        g.setFont(font);
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(lines.get(i), 0, (int)((i + 1) * lineHeight));
        }
        return image;
    }

    @Setter
    @Accessors(chain = true, fluent = true)
    public static class TextWidgetBuilder {
        private int width = 200;
        private int height = 200;
        private int fontHeight = 14;
        private String fontName = "Ariel";
        private Theme theme = Theme.LIGHT;

        public TextWidget build() {
            return new TextWidget(width, height, fontHeight, fontName, theme);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", TextWidgetBuilder.class.getSimpleName() + "[", "]")
                    .add("width=" + width)
                    .add("height=" + height)
                    .add("fontHeight=" + fontHeight)
                    .add("fontName='" + fontName + "'")
                    .add("theme=" + theme)
                    .toString();
        }
    }

}
