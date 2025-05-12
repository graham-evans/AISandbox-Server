package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputConstants;
import dev.aisandbox.server.engine.widget.GraphicsUtils;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class CoinIcons {

  public static final int PILE_WIDTH = 290;
  public static final int ROW_HEIGHT = 80;
  private static final int COIN_SPACE = 20;
  private static final int COIN_IMAGE_WIDTH = 241;
  private static final int COIN_IMAGE_HEIGHT = 91;
  public static final int COINS_HEIGHT = 20 * COIN_SPACE + COIN_IMAGE_HEIGHT;

  public static BufferedImage[] getRowImages(int rowCount, Theme theme) {
    BufferedImage[] images = new BufferedImage[rowCount];
    for (int i = 0; i < rowCount; i++) {
      images[i] = new BufferedImage(PILE_WIDTH, ROW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = images[i].createGraphics();
      GraphicsUtils.setupRenderingHints(g);
      g.setColor(Color.BLACK);
      GraphicsUtils.drawCenteredText(g, 0, 0, PILE_WIDTH, ROW_HEIGHT - 4, "Row " + i,
          OutputConstants.HEADER_FONT, theme.getText());
    }
    return images;
  }

  public static BufferedImage[] getCoinImages(int cointCount, Theme theme) throws IOException {
    BufferedImage[] images = new BufferedImage[cointCount + 1];
    // load coin image
    BufferedImage coinImage = ImageIO.read(
        CoinIcons.class.getResourceAsStream("/images/coins/gold.png"));
    log.debug("loaded coins image of width {} and height {}", coinImage.getWidth(),
        coinImage.getHeight());
    // create the font
    Font font = new Font("Arial", Font.BOLD, 20);

    // draw the coin images
    for (int i = 0; i <= cointCount; i++) {
      images[i] = new BufferedImage(PILE_WIDTH, COINS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = images[i].createGraphics();
      GraphicsUtils.setupRenderingHints(g);
      int xPadding = (PILE_WIDTH - coinImage.getWidth()) / 2;
      // draw coins
      for (int j = 1; j <= i; j++) {
        g.drawImage(coinImage, xPadding, COINS_HEIGHT - coinImage.getHeight() - (j-1) * COIN_SPACE,
            null);
      }
      // draw number
      g.setColor(Color.DARK_GRAY);
      g.fillRect(PILE_WIDTH / 2 - 40, COINS_HEIGHT - 40, 80, 40);
      GraphicsUtils.drawCenteredText(g, PILE_WIDTH / 2 - 40, COINS_HEIGHT - 40 - 6, 80, 40,
          Integer.toString(i), OutputConstants.HEADER_FONT, theme.getText());
    }
    return images;
  }


}
