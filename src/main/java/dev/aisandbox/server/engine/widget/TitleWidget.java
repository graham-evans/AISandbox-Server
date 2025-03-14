package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;

import static dev.aisandbox.server.engine.output.OutputConstants.HD_WIDTH;
import static dev.aisandbox.server.engine.output.OutputConstants.TITLE_HEIGHT;

@Slf4j
public class TitleWidget {

    private final String title;
    private final Theme theme;
    @Getter
    private final BufferedImage image;

    public TitleWidget(String title, Theme theme) {
        this.title = title;
        this.theme = theme;
        // generate image
        image = new BufferedImage(HD_WIDTH, TITLE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(theme.getBackground());
        g.fillRect(0, 0, HD_WIDTH, TITLE_HEIGHT);
        g.setColor(theme.getText());
        g.setFont(new Font("Arial", Font.PLAIN, TITLE_HEIGHT));
        FontMetrics fm = g.getFontMetrics();
        int stringWidth = fm.stringWidth(title);
        g.drawString(title, HD_WIDTH / 2 - stringWidth / 2, TITLE_HEIGHT-fm.getDescent());
    }

    public static TitleWidgetBuilder builder() {
        return new TitleWidgetBuilder();
    }

    @Setter
    @Accessors(chain = true, fluent = true)
    public static class TitleWidgetBuilder {
        private String title = "Title";
        private Theme theme = Theme.LIGHT;

        public TitleWidget build() {
            return new TitleWidget(title, theme);
        }
    }
}
