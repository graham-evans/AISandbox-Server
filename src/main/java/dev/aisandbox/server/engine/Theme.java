package dev.aisandbox.server.engine;

import lombok.Getter;

import java.awt.*;

@Getter
public enum Theme {

    DEFAULT(Color.WHITE,Color.BLACK);

    private final Color background;
    private final Color foreground;

    Theme(Color background, Color foreground) {
        this.background = background;
        this.foreground = foreground;
    }

}
