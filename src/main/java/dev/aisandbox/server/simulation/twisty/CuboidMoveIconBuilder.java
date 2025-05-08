package dev.aisandbox.server.simulation.twisty;

import dev.aisandbox.server.engine.output.OutputConstants;
import dev.aisandbox.server.engine.output.SpriteLoader;
import dev.aisandbox.server.engine.widget.GraphicsUtils;
import dev.aisandbox.server.simulation.twisty.model.Move;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CuboidMoveIconBuilder {

  // constants to frame the icon
  final static int marginLeft = 10;
  final static int marginRight = 10;
  final static int marginTop = 10;
  final static int marginBottom = 10;
  // colours to fill squares
  final static Color UNFILLED = Color.lightGray;
  final static Color FILLED = Color.darkGray;
  final static Color LINES = Color.BLACK;
  // arrow images
  final static List<BufferedImage> arrows = SpriteLoader.loadSpritesFromResources(
      "/images/twisty/CuboidArrows.png", Move.MOVE_ICON_WIDTH, Move.MOVE_ICON_WIDTH);

  // dimensions
  final int width;
  final int height;
  final int originX;
  //  final int originY;
  final double scale;
  // drawing objects
  BufferedImage backgroundImage = GraphicsUtils.createBlankImage(Move.MOVE_ICON_WIDTH,
      Move.MOVE_ICON_HEIGHT, Color.WHITE);
  BufferedImage middleImage = GraphicsUtils.createClearImage(Move.MOVE_ICON_WIDTH,
      Move.MOVE_ICON_HEIGHT);
  BufferedImage foregroundImage = GraphicsUtils.createClearImage(Move.MOVE_ICON_WIDTH,
      Move.MOVE_ICON_HEIGHT);
  Graphics2D backgroundGraphics = backgroundImage.createGraphics();
  Graphics2D middleGraphics = middleImage.createGraphics();
  Graphics2D foregroundGraphics = foregroundImage.createGraphics();

  public CuboidMoveIconBuilder(int width, int height, String name) {
    this.width = width;
    this.height = height;
    // setup image
    GraphicsUtils.setupRenderingHints(foregroundGraphics);
    // work out scale of each small square
    double hScale = (Move.MOVE_ICON_WIDTH - marginLeft - marginRight) / (1.0 * width);
    double vScale = (Move.MOVE_ICON_HEIGHT - marginTop - marginBottom) / (1.0 * height);
    scale = Math.min(hScale, vScale);
    // center the grid
    originX = marginLeft + (int) ((Move.MOVE_ICON_WIDTH - marginLeft - marginRight - width * scale)
        / 2.0);
    // draw cuboid
    drawCuboid(originX, marginTop, width, height, scale);
    // write label
    drawName(foregroundGraphics, name);
  }

  public static CuboidMoveIconBuilder builer(int width, int height, String name) {
    return new CuboidMoveIconBuilder(width, height, name);
  }

  private static void drawName(Graphics2D g, String name) {
    GraphicsUtils.drawCenteredText(g, 0,
        Move.MOVE_ICON_HEIGHT - OutputConstants.LOG_FONT_HEIGHT - marginBottom,
        Move.MOVE_ICON_WIDTH, OutputConstants.LOG_FONT_HEIGHT, name, OutputConstants.LOG_FONT,
        Color.BLACK);
  }

  private void drawCuboid(int originX, int originY, int width, int height, double scale) {
    middleGraphics.setColor(UNFILLED);
    middleGraphics.fillRect(originX, originY, (int) (width * scale), (int) (height * scale));
    foregroundGraphics.setColor(LINES);
    // draw front vertical lines
    for (int i = 0; i <= width; i++) {
      foregroundGraphics.drawLine((int) (originX + i * scale), (int) originY,
          (int) (originX + i * scale), (int) (originY + height * scale));
    }
    // draw front horizontal lines
    for (int i = 0; i <= height; i++) {
      foregroundGraphics.drawLine(originX, originY + (int) (i * scale),
          originX + (int) (width * scale), originY + (int) (i * scale));
    }
  }

  public CuboidMoveIconBuilder fillFrontFace() {
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        fillFrontFace(x, y);
      }
    }
    return this;
  }

  public CuboidMoveIconBuilder fillFromTop(int n) {
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < n; y++) {
        fillFrontFace(x, y);
      }
    }
    return this;
  }

  public CuboidMoveIconBuilder fillFromBottom(int n) {
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < n; y++) {
        fillFrontFace(x, height - y - 1);
      }
    }
    return this;
  }

  public CuboidMoveIconBuilder fillFromLeft(int n) {
    for (int x = 0; x < n; x++) {
      for (int y = 0; y < height; y++) {
        fillFrontFace(x, y);
      }
    }
    return this;
  }

  public CuboidMoveIconBuilder fillFromRight(int n) {
    for (int x = 0; x < n; x++) {
      for (int y = 0; y < height; y++) {
        fillFrontFace(width - x - 1, y);
      }
    }
    return this;
  }

  private void fillFrontFace(int x, int y) {
    fillFrontFace(x, y, FILLED);
  }

  private void fillFrontFace(int x, int y, Color color) {
    middleGraphics.setColor(color);
    middleGraphics.fillRect((int) (originX + x * scale), (int) (marginTop + scale * y),
        (int) scale + 1, (int) scale + 1);
  }

  public CuboidMoveIconBuilder setRotation(char face, boolean inverse) {
    switch (face) {
      case 'F':
        foregroundGraphics.drawImage(arrows.get(inverse ? 5 : 0), 0, 0, null);
        break;
      case 'B':
        backgroundGraphics.drawImage(arrows.get(inverse ? 0 : 5), 0, 0, null);
        break;
      case 'U':
        foregroundGraphics.drawImage(arrows.get(inverse ? 6 : 1), 0, 0, null);
        break;
      case 'D':
        foregroundGraphics.drawImage(arrows.get(inverse ? 7 : 2), 0, 0, null);
        break;
      case 'R':
        foregroundGraphics.drawImage(arrows.get(inverse ? 8 : 3), 0, 0, null);
        break;
      case 'L':
        foregroundGraphics.drawImage(arrows.get(inverse ? 9 : 4), 0, 0, null);
        break;
 /*     case 'R':
        backgroundGraphics.drawImage(arrows.getSubimage(32 + 4 * 60, 20, 28, 40), 32, 20, null);
        foregroundGraphics.drawImage(arrows.getSubimage(40 + (inverse ? 60 : 0), 20, 20, 40), 40,
            20, null);
        break;
      case 'L':
        backgroundGraphics.drawImage(arrows.getSubimage(4 * 60, 20, 28, 40), 0, 20, null);
        foregroundGraphics.drawImage(arrows.getSubimage((inverse ? 60 : 0), 20, 20, 40), 0, 20,
            null);
        break;
      case 'U':
        backgroundGraphics.drawImage(arrows.getSubimage(4 * 60, 0, 60, 30), 0, 0, null);
        foregroundGraphics.drawImage(arrows.getSubimage((inverse ? 60 : 0), 0, 60, 15), 0, 0, null);
        break;
      case 'D':
        backgroundGraphics.drawImage(arrows.getSubimage(4 * 60, 40, 60, 35), 0, 40, null);
        foregroundGraphics.drawImage(arrows.getSubimage((inverse ? 60 : 0), 60, 60, 15), 0, 60,
            null);
        break;
      case 'F':
        foregroundGraphics.drawImage(arrows.getSubimage((inverse ? 3 : 2) * 60, 0, 60, 100), 0, 0,
            null);
        break;
      case 'B':
        backgroundGraphics.drawImage(arrows.getSubimage((inverse ? 2 : 3) * 60, 0, 60, 100), 0, 0,
            null);
        break;

  */
    }
    return this;
  }

  public BufferedImage getImage() {
    // merge the layers
    BufferedImage image = new BufferedImage(Move.MOVE_ICON_WIDTH, Move.MOVE_ICON_HEIGHT,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();
    g.drawImage(backgroundImage, 0, 0, null);
    g.drawImage(middleImage, 0, 0, null);
    g.drawImage(foregroundImage, 0, 0, null);
    return image;
  }


}
