package dev.aisandbox.server.engine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Theme {

    LIGHT(
            Color.decode("#efefef"), // background
            Color.decode("#4c4c4d"), // text
            Color.decode("#fffaf0"), // widget background
            Color.decode("#fffaf0"), // graph background
            Color.decode("#4c4c4d"), // graph outline (axis)
            Color.decode("#fffaf0"), // baize (play area)
            Color.decode("#07144b"), // graph colour

            Color.decode("#85d8ef"),
            Color.decode("#85d8ef"),
            Color.decode("#85d8ef"),

            Color.decode("#640d14"),
            Color.decode("#800e13"),
            Color.decode("#38040e"),

            Color.decode("#ffa200"),
            Color.decode("#ffaa00"),
            Color.decode("#ff9500")
    ),
    WARM(
            Color.decode("#fef4e1"), // background
            Color.decode("#494949"), // text
            Color.decode("#ffffff"), // widget background
            Color.decode("#ffffff"), // graph background
            Color.decode("#494949"), // graph outline (axis)
            Color.decode("#ffffff"), // baize (play area)
            Color.decode("#ea423d"), // graph colour 1

            Color.decode("#038da2"),
            Color.decode("#038da2"),
            Color.decode("#038da2"),

            Color.decode("#640d14"),
            Color.decode("#800e13"),
            Color.decode("#38040e"),

            Color.decode("#ffa200"),
            Color.decode("#ffaa00"),
            Color.decode("#ff9500")
    ),


    DARK(
            Color.decode("#000d43"), // background
            Color.decode("#ffffff"), // text
            Color.decode("#001155"), // widget background
            Color.decode("#ffe0a6"), // graph background
            Color.decode("#000d43"), // graph outline (axis)
            Color.decode("#001155"), // baize (play area)
            Color.decode("#ff0800"), // graph colour 1

            Color.decode("#014f89"),
            Color.decode("#2a6f97"),
            Color.decode("#01497c"),

            Color.decode("#640d14"),
            Color.decode("#800e13"),
            Color.decode("#38040e"),

            Color.decode("#ffa200"),
            Color.decode("#ffaa00"),
            Color.decode("#ff9500")
    );

    private final Color background;
    private final Color text;
    private final Color widgetBackground;
    private final Color graphBackground;
    private final Color graphOutlineColor;
    private final Color baize;

    private final Color graphColor1;

    private final Color agent1Main;
    private final Color agent1Highlight;
    private final Color agent1Lowlight;

    private final Color agent2Main;
    private final Color agent2Highlight;
    private final Color agent2Lowlight;

    private final Color agentSelectedMain;
    private final Color agentSelectedHighlight;
    private final Color agentSelectedLowlight;

    public Color getAgentMain(int i) {
        return switch (i) {
            case 0 -> agent1Main;
            case 1 -> agent2Main;
            default -> Color.DARK_GRAY;
        };
    }
}
