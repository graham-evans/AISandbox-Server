package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


public class TextWidget {
    private final Font font;
    private final List<String> lines = new ArrayList<>();
    private final int maxLines;
    private final int width;
    private final int height;
    private final int lineHeight;
    private final String fontName;
    private final Theme theme;
    private BufferedImage image = null;

    public TextWidget(int width, int height, int lineHeight, String fontName, Theme theme) {
        this.fontName = fontName;
        this.width = width;
        this.height = height;
        this.lineHeight = lineHeight;
        this.theme = theme;
        // create font
        font = new Font(this.fontName, Font.PLAIN, lineHeight);
        maxLines = height / lineHeight;
    }

    public void addText(final String text) {
        lines.add(text);
        while (lines.size() > maxLines) {
            lines.removeFirst();
        }
        // invalidate image
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
        g.setColor(theme.getBackground());
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, width, height);
        g.setColor(theme.getText());
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(lines.get(i), 0, (i + 1) * 12);
        }
        return image;
    }

}
