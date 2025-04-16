package dev.aisandbox.server.simulation.coingame;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class CoinIcons {

  public static final int ROW_WIDTH = 100;
  public static final int ROW_HEIGHT = 41 + 6 * 4;
  public static final int COINS_WIDTH = 500;

  public static BufferedImage[] getRowImages(int rowCount) {
    BufferedImage[] images = new BufferedImage[rowCount];
    for (int i = 0; i < rowCount; i++) {
      images[i] = new BufferedImage(ROW_WIDTH, ROW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = images[i].createGraphics();
      g.setColor(Color.BLACK);
      drawCenteredString(g, "Row " + i, new Rectangle(0, 0, ROW_WIDTH, ROW_HEIGHT),
          new Font("Arial", Font.BOLD, 20));
    }
    return images;
  }

  public static BufferedImage[] getCoinImages(int cointCount) throws IOException {
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
      images[i] = new BufferedImage(COINS_WIDTH, ROW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = images[i].createGraphics();
      //   g.setColor(Color.yellow);
      //   g.fillRect(0, 0, COINS_WIDTH, ROW_HEIGHT);
      int[] crows = getTriangleRows(i);
      int xPadding = (COINS_WIDTH - crows[0] * coinImage.getWidth()) / 2;
      int yPadding = (ROW_HEIGHT - crows.length * 4 - coinImage.getHeight()) / 2;
      for (int j = 0; j < crows.length; j++) {
        for (int k = 0; k < crows[j]; k++) {
          g.drawImage(coinImage, (int) ((k + j / 2.0) * coinImage.getWidth()) + xPadding,
              ROW_HEIGHT - coinImage.getHeight() - j * 4 - yPadding, null);
        }
      }

      // center the text
      String text = Integer.toString(i);
      // Get the FontMetrics
      FontMetrics metrics = g.getFontMetrics(font);
      // Determine the X coordinate for the text
      int textWidth = metrics.stringWidth(text);
      int x = (COINS_WIDTH - textWidth) / 2;
      int boxX = x - 10;
      // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top
      // of the screen)
      int y = ((ROW_HEIGHT - metrics.getHeight()) / 2) + metrics.getAscent();
      int boxY = ((ROW_HEIGHT - metrics.getHeight()) / 2);
      int textHeight = metrics.getHeight();
      // Set the font
      g.setFont(font);
      // draw the background
      g.setColor(Color.DARK_GRAY);
      g.fill3DRect(boxX, boxY, textWidth + 20, textHeight, true);
      // Draw the String
      g.setColor(Color.WHITE);
      g.drawString(text, x, y);
    }
    return images;
  }

  /**
   * Draw a String centered in the middle of a Rectangle.
   *
   * @param g    The Graphics instance.
   * @param text The String to draw.
   * @param rect The Rectangle to center the text in.
   */
  public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
    // Get the FontMetrics
    FontMetrics metrics = g.getFontMetrics(font);
    // Determine the X coordinate for the text
    int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of
    // the screen)
    int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
    // Set the font
    g.setFont(font);
    // Draw the String
    g.drawString(text, x, y);
  }

  /**
   * Get the number of rows required to best draw a stack of coins into a triangle
   *
   * @param num
   * @return
   */
  public static int[] getTriangleRows(int num) {
    List<Integer> rows = new ArrayList<>();
    rows.add(0);
    int cursor = 0;
    int max = 0;
    for (int i = 0; i < num; i++) {
      log.info("Adding 1 to {}, cursor={}", rows, cursor);
      // add coin to the pile
      if (cursor < rows.size()) {
        rows.set(cursor, rows.get(cursor) + 1);
        cursor++;
      } else if (rows.getLast() == 2) {
        rows.add(1);
        cursor = 0;
      } else {
        rows.set(0, rows.get(0) + 1);
        cursor = 1;
      }

    }
    int[] rowsArray = rows.stream().mapToInt(i -> i).toArray();
    log.info("Result = {}", rowsArray);
    return rowsArray;
  }

}
