package dev.aisandbox.server.simulation.twisty;

import dev.aisandbox.server.engine.output.OutputConstants;
import dev.aisandbox.server.engine.widget.GraphicsUtils;
import dev.aisandbox.server.simulation.twisty.model.Move;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CuboidMoveIcon {

  // constants to frame the icon
  final static int marginLeft = 10;
  final static int marginRight = 10;
  final static int marginTop = 10;
  final static int marginBottom = 34;
  // colours to fill squares
  final static Color UNFILLED = Color.WHITE;
  final static Color FILLED = Color.lightGray;
  // arrow images
  final static BufferedImage arrows;

  static {
    BufferedImage arrowImage = null;
    try {
      arrowImage = ImageIO.read(
          CuboidMoveIcon.class.getResourceAsStream("/images/twisty/CuboidArrows.png"));
    } catch (IOException e) {
      log.error("Error loading arrows!", e);
    }
    arrows = arrowImage;
  }

  // dimentions
  final int width;
  final int height;
  final int originX;
  //  final int originY;
  final double scale;
  // drawing objects
  BufferedImage backgroundImage = GraphicsUtils.createBlankImage(Move.MOVE_ICON_WIDTH,
      Move.MOVE_ICON_HEIGHT, Color.WHITE);
  BufferedImage foregroundImage = GraphicsUtils.createClearImage(Move.MOVE_ICON_WIDTH,
      Move.MOVE_ICON_HEIGHT);
  Graphics2D backgroundGraphics = backgroundImage.createGraphics();
  Graphics2D foregroundGraphics = foregroundImage.createGraphics();

  public CuboidMoveIcon(int width, int height, String name) {
    this.width = width;
    this.height = height;
    // setup image
    GraphicsUtils.setupRenderingHints(foregroundGraphics);
    // work out scale of each small square
    double hScale = (Move.MOVE_ICON_WIDTH - marginLeft - marginRight) / (1.0 * width);
    double vScale = (Move.MOVE_ICON_HEIGHT - marginTop - marginBottom) / (1.0 * height);
    scale = Math.min(hScale, vScale);
    // work out the origin of the grid
//    originY = Move.MOVE_ICON_HEIGHT - marginBottom - (int) (
//        (Move.MOVE_ICON_HEIGHT - marginBottom - marginTop - height * scale) / 2.0);

    originX = marginLeft + (int) ((Move.MOVE_ICON_WIDTH - marginLeft - marginRight - width * scale)
        / 2.0);

    // draw cuboid
    drawCuboid(foregroundGraphics, originX, marginTop, width, height, scale);
    // write label
    drawName(foregroundGraphics, name);
//    midgroundGraphics.setColor(UNFILLED);
//    midgroundGraphics.fillRect(originX, originY - (int) (height * scale), (int) (width * scale),
//        (int) (height * scale));

    //   midgroundGraphics.setColor(Color.cyan);
    //   midgroundGraphics.fillRect(marginLeft,marginTop,Move
    //   .MOVE_ICON_WIDTH-marginLeft-marginRight,Move.MOVE_ICON_HEIGHT-marginTop-marginBottom);
    // load arrows
    //  arrows = ImageIO.read(
    //      CuboidMoveIcon.class.getResourceAsStream("/images/twisty/CuboidArrows.png"));
    // overlay images onto background
  }

  public static CuboidMoveIcon builer(int width, int height, String name) {
    return new CuboidMoveIcon(width, height, name);
  }

  private static void drawCuboid(Graphics2D g, int originX, int originY, int width, int height,
      double scale) {
    g.setColor(Color.darkGray);
    // draw front vertical lines
    for (int i = 0; i <= width; i++) {
      g.drawLine((int) (originX + i * scale), (int) originY, (int) (originX + i * scale),
          (int) (originY + height * scale));
    }
    // draw front horizontal lines
    for (int i = 0; i <= height; i++) {
      g.drawLine(originX, originY + (int) (i * scale), originX + (int) (width * scale),
          originY + (int) (i * scale));
    }
  }

  private static void drawName(Graphics2D g, String name) {
    GraphicsUtils.drawCenteredText(g, 0,
        Move.MOVE_ICON_HEIGHT - OutputConstants.LOG_FONT_HEIGHT * 2, Move.MOVE_ICON_WIDTH,
        OutputConstants.LOG_FONT_HEIGHT, name, OutputConstants.LOG_FONT, Color.BLACK);
  }

  public CuboidMoveIcon fillFrontFace() {
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        fillFrontFace(x, y);
      }
    }
    return this;
  }

  public CuboidMoveIcon fillFromTop(int n) {
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < n; y++) {
        fillFrontFace(x, y);
      }
    }
    return this;
  }

  private void fillFrontFace(int x, int y) {
    fillFrontFace(x, y, FILLED);
  }

  private void fillFrontFace(int x, int y, Color color) {
    backgroundGraphics.setColor(color);
    backgroundGraphics.fillRect((int) (originX + x * scale), (int) (marginTop + scale * y),
        (int) scale + 1, (int) scale + 1);
  }

  public CuboidMoveIcon setRotation(char face, boolean inverse) {
    switch (face) {
      case 'R':
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
    }
    return this;
  }

  public BufferedImage getImage() {
    // merge the layers
    BufferedImage image = new BufferedImage(Move.MOVE_ICON_WIDTH, Move.MOVE_ICON_HEIGHT,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();
    g.drawImage(backgroundImage, 0, 0, null);
    g.drawImage(foregroundImage, 0, 0, null);
    return image;
  }


}
