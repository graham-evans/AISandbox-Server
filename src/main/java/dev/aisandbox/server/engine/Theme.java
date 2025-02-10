package dev.aisandbox.server.engine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.knowm.xchart.style.Styler;

import java.awt.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Theme {

    DEFAULT(
            Color.decode("#eeeeee"),
            Color.BLACK,
            Color.lightGray,
            Color.WHITE,
            Color.BLUE,
            Color.ORANGE,
            Color.DARK_GRAY,
            Styler.ChartTheme.XChart
    );

    private final Color background;
    private final Color text;
    private final Color widgetBackground;
    private final Color graphBackground;
    private final Color primaryColor;
    private final Color secondaryColor;
    private final Color graphOutlineColor;

    private final Styler.ChartTheme chartTheme;

}
