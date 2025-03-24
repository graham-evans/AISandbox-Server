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
            Color.BLACK, // text
            Color.WHITE, // widget background
            Color.WHITE, // graph background
            Color.DARK_GRAY, // graph outline (axis)
            Color.WHITE, // baize (play area)

            Color.decode("#014f89"),
            Color.decode("#2a6f97"),
            Color.decode("#01497c"),

            Color.decode("#640d14"),
            Color.decode("#800e13"),
            Color.decode("#38040e"),

            Color.decode("#ffa200"),
            Color.decode("#ffaa00"),
            Color.decode("#ff9500")
    ), DARK(
            Color.BLACK,
            Color.WHITE,
            Color.lightGray,
            Color.WHITE,
            Color.DARK_GRAY,

            Color.decode("#006400"),

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
