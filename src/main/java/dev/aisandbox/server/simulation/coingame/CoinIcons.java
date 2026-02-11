/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

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

/**
 * Utility class for generating graphical representations of coins and game rows for the Coin Game
 * simulation. This class provides methods to create images of game rows and stacked coin piles with
 * varying quantities.
 */
@Slf4j
@UtilityClass
public class CoinIcons {

  /**
   * The width of a coin pile image in pixels.
   */
  public static final int PILE_WIDTH = 290;

  /**
   * The height of a row label image in pixels.
   */
  public static final int ROW_HEIGHT = 80;

  /**
   * The vertical spacing between coins in a stack in pixels.
   */
  private static final int COIN_SPACE = 20;

  /**
   * The width of an individual coin image in pixels.
   */
  private static final int COIN_IMAGE_WIDTH = 241;

  /**
   * The height of an individual coin image in pixels.
   */
  private static final int COIN_IMAGE_HEIGHT = 91;

  /**
   * The total height of an area that can display up to 20 stacked coins. Calculated as 20 spacing
   * intervals plus the height of a single coin.
   */
  public static final int COINS_HEIGHT = 20 * COIN_SPACE + COIN_IMAGE_HEIGHT;

  /**
   * Generates an array of images representing row labels for the coin game.
   *
   * @param rowCount The number of rows to generate labels for
   * @param theme    The visual theme to apply to the text
   * @return An array of BufferedImage objects, each containing a row label
   */
  public static BufferedImage[] getRowImages(int rowCount, Theme theme) {
    BufferedImage[] images = new BufferedImage[rowCount];
    for (int i = 0; i < rowCount; i++) {
      // Create a new image for this row label
      images[i] = new BufferedImage(PILE_WIDTH, ROW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = images[i].createGraphics();
      // Apply rendering hints for better text quality
      GraphicsUtils.setupRenderingHints(g);
      g.setColor(Color.BLACK);
      // Draw the centered row label text
      GraphicsUtils.drawCenteredText(g, 0, 0, PILE_WIDTH, ROW_HEIGHT - 4, "Row " + i,
          OutputConstants.HEADER_FONT, theme.getText());
    }
    return images;
  }

  /**
   * Generates an array of images representing different quantities of coins. Each image shows a
   * stack of n coins where n is the index of the image in the array.
   *
   * @param cointCount The maximum number of coins to generate images for
   * @param theme      The visual theme to apply to the text
   * @return An array of BufferedImage objects showing different quantities of stacked coins
   * @throws IOException If the coin image resource cannot be loaded
   */
  public static BufferedImage[] getCoinImages(int cointCount, Theme theme) throws IOException {
    BufferedImage[] images = new BufferedImage[cointCount + 1];

    // Load the coin image from resources
    BufferedImage coinImage = ImageIO.read(
        CoinIcons.class.getResourceAsStream("/images/coins/gold.png"));
    log.debug("loaded coins image of width {} and height {}", coinImage.getWidth(),
        coinImage.getHeight());

    // Create the font for numeric labels
    Font font = new Font("Arial", Font.BOLD, 20);

    // Generate images for quantities from 0 to cointCount
    for (int i = 0; i <= cointCount; i++) {
      // Create a new image for this quantity of coins
      images[i] = new BufferedImage(PILE_WIDTH, COINS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = images[i].createGraphics();
      GraphicsUtils.setupRenderingHints(g);

      // Center the coins horizontally
      int xPadding = (PILE_WIDTH - coinImage.getWidth()) / 2;

      // Draw the stack of coins from bottom to top
      for (int j = 1; j <= i; j++) {
        g.drawImage(coinImage, xPadding,
            COINS_HEIGHT - coinImage.getHeight() - (j - 1) * COIN_SPACE, null);
      }

      // Draw a label showing the number of coins
      g.setColor(Color.DARK_GRAY);
      g.fillRect(PILE_WIDTH / 2 - 40, COINS_HEIGHT - 40, 80, 40);
      GraphicsUtils.drawCenteredText(g, PILE_WIDTH / 2 - 40, COINS_HEIGHT - 40 - 6, 80, 40,
          Integer.toString(i), OutputConstants.HEADER_FONT, theme.getText());
    }
    return images;
  }
}
