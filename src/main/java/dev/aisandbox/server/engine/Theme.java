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
            Color.darkGray,
            Styler.ChartTheme.XChart
    );

    private final Color background;
    private final Color text;
    private final Color widgetBackground;
    private final Color graphBackground;
    private final Styler.ChartTheme chartTheme;

}
