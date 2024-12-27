package dev.aisandbox.server.engine.widget;

import lombok.experimental.UtilityClass;

import java.awt.*;

@UtilityClass
public class GraphicsUtils {

    public static final void setupRenderingHints(Graphics2D g) {
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
    }

}
