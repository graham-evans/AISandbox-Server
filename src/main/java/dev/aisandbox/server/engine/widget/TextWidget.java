package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.StringJoiner;

public class TextWidget {
    @Getter(AccessLevel.PACKAGE)
    private final int width;
    private final int height;
    private final int lineHeight;
    @Getter(AccessLevel.PACKAGE)
    private final Theme theme;
    private final int PADDING = 10;
    // internal state
    @Getter
    private final BufferedImage image;
    private final Graphics2D graphics;
    private  BufferedImage blankLine;
    private final FontMetrics fontMetrics;
    private final int descender;

    private TextWidget(int width, int height, int fontHeight, String fontName, Theme theme) {
        this.width = width;
        this.height = height;
        this.lineHeight = fontHeight + 2;
        this.theme = theme;
        // create blank image
        image = GraphicsUtils.createBlankImage(width,height,theme.getWidgetBackground());
        graphics = image.createGraphics();
        blankLine = GraphicsUtils.createBlankImage(width-PADDING,lineHeight,theme.getWidgetBackground());
        // setup text
        graphics.setColor(theme.getText());
        Font font = new Font(fontName, Font.PLAIN, fontHeight);
        graphics.setFont(font);
        GraphicsUtils.setupRenderingHints(graphics);
        fontMetrics = graphics.getFontMetrics();
        descender = fontMetrics.getDescent();
    }

    public static TextWidgetBuilder builder() {
        return new TextWidgetBuilder();
    }

    private void addTextLine(final String text) {
        // move image one line up
        BufferedImage scrollImage = image.getSubimage(PADDING,PADDING+lineHeight,width-PADDING,height-PADDING*2-lineHeight);
        graphics.drawImage(scrollImage,PADDING,PADDING,null);
        // blank the bottom line
        graphics.drawImage(blankLine,PADDING,height-PADDING-lineHeight,null);
        // draw the text line
        graphics.drawString(text,PADDING,height-PADDING- descender);
    }

    public void addText(final String text) {
        // is this a simple line of text
        if (fontMetrics.stringWidth(text) <= width - PADDING*2) {
            addTextLine(text);
        } else {
            // try to split the line-up
            StringBuilder line = new StringBuilder();
            for (String word : text.split(" ")) {
                if (line.isEmpty()) { // case 1 - first word always gets added
                    line.append(word);
                    line.append(" ");
                }
                if (fontMetrics.stringWidth(line.toString()+word) <= width - PADDING*2) { // case 2 - room for another word
                    line.append(word);
                    line.append(" ");
                } else {
                    // write the line
                    addTextLine(line.toString());
                    // start the next line
                    line.setLength(0);
                    line.append(word);
                }
            }
            if (!line.isEmpty()) {
                addTextLine(line.toString());
            }
        }

    }

    public void reset() {
        graphics.setColor(theme.getWidgetBackground());
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(theme.getText());
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
