package dev.aisandbox.server.engine.output;

import dev.aisandbox.server.simulation.bandit.BanditRuntime;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@UtilityClass
@Slf4j
public class OutputConstants {

    public static final int HD_WIDTH = 1920;
    public static final int HD_HEIGHT = 1080;
    public static final int LOGO_WIDTH = 91;
    public static final int LOGO_HEIGHT = 108;
    @Deprecated
    public static final int MARGIN = 100;
    public static final int TITLE_HEIGHT = 80;
    public static final int TOP_MARGIN = 50;
    public static final int BOTTOM_MARGIN = 50;
    public static final int LEFT_MARGIN = 50;
    public static final int RIGHT_MARGIN = 50;
    public static final int WIDGET_SPACING = 50;


    public static final BufferedImage LOGO;

    static {
        // load logo
        BufferedImage i;
        try {
            i = ImageIO.read(BanditRuntime.class.getResourceAsStream("/images/AILogo.png"));
        } catch (IOException e) {
            log.error("Error loading logo image", e);
            i = new BufferedImage(LOGO_WIDTH, LOGO_HEIGHT, BufferedImage.TYPE_INT_RGB);
        }
        LOGO = i;
    }
}
