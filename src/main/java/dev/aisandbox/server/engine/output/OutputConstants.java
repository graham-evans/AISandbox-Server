package dev.aisandbox.server.engine.output;

import dev.aisandbox.server.simulation.bandit.BanditRuntime;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class OutputConstants {

  public static final int HD_WIDTH = 1920;
  public static final int HD_HEIGHT = 1080;
  public static final int LOGO_WIDTH = 91;
  public static final int LOGO_HEIGHT = 108;
  @Deprecated
  public static final int MARGIN = 100;
  public static final int TITLE_HEIGHT = 50;
  public static final int TOP_MARGIN = 50;
  public static final int BOTTOM_MARGIN = 50;
  public static final int LEFT_MARGIN = 50;
  public static final int RIGHT_MARGIN = 50;
  public static final int WIDGET_SPACING = 50;
  public static final int LOG_FONT_HEIGHT = 16;
  public static final Font TITLE_FONT;
  public static final Font LOG_FONT;
  public static final Font STATISTICS_FONT;


  public static final BufferedImage LOGO;

  private static final List<String> FONT_LIST = List.of("/fonts/Arimo-VariableFont_wght.ttf",
      "/fonts/Hack-Regular.ttf");

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
    // Load fonts
    GraphicsEnvironment GE = GraphicsEnvironment.getLocalGraphicsEnvironment();
    try {
      for (String path : FONT_LIST) {
        log.debug("Loading font from {}", path);
        Font font = Font.createFont(Font.TRUETYPE_FONT,
            OutputConstants.class.getResourceAsStream(path));
        log.debug("Registering font {}", font.getFontName());
        GE.registerFont(font);
      }
    } catch (FontFormatException | IOException e) {
      log.error("Error loading fonts", e);
    }
    // create base fonts
    TITLE_FONT = new Font("Arimo Regular", Font.PLAIN, TITLE_HEIGHT);
    LOG_FONT = new Font("Hack Regular", Font.PLAIN, LOG_FONT_HEIGHT);
    STATISTICS_FONT = new Font("Arimo Regular", Font.PLAIN, 32);
  }
}
