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
    public static final int EDGE = 100;

    public static final BufferedImage logo;

    static {
        BufferedImage i;
        try {
            i = ImageIO.read(BanditRuntime.class.getResourceAsStream("/images/AILogo.png"));
        } catch (IOException e) {
            log.error("Error loading logo image", e);
            i = new BufferedImage(LOGO_WIDTH, LOGO_HEIGHT, BufferedImage.TYPE_INT_RGB);
        }
        logo = i;
    }
}
