/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

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

/**
 * A builder class that creates visual icons to represent moves in a cuboid puzzle. These icons show
 * which parts of the puzzle are affected by a move and indicate rotation direction with arrows.
 */
@Slf4j
public class CuboidMoveIconBuilder {

  /**
   * Left margin for the move icon layout in pixels.
   */
  static final int marginLeft = 10;

  /**
   * Right margin for the move icon layout in pixels.
   */
  static final int marginRight = 10;

  /**
   * Top margin for the move icon layout in pixels.
   */
  static final int marginTop = 10;

  /**
   * Bottom margin for the move icon layout in pixels.
   */
  static final int marginBottom = 10;

  /**
   * Color used for unfilled cells in the icon.
   */
  static final Color UNFILLED = Color.lightGray;

  /**
   * Color used for filled cells that are affected by the move.
   */
  static final Color FILLED = Color.darkGray;

  /**
   * Color used for grid lines separating cells.
   */
  static final Color LINES = Color.BLACK;

  /**
   * Preloaded arrow sprite images used to indicate rotation direction. Loaded from a sprite sheet
   * with multiple arrow directions.
   */
  static final List<BufferedImage> arrows = SpriteLoader.loadSpritesFromResources(
      "/images/twisty/CuboidArrows.png", Move.MOVE_ICON_WIDTH, Move.MOVE_ICON_WIDTH);

  /**
   * The width of the cuboid face in cells.
   */
  final int width;

  /**
   * The height of the cuboid face in cells.
   */
  final int height;

  /**
   * The x-coordinate where drawing of the cuboid face begins.
   */
  final int originX;

  /**
   * The scale factor used to size each cell in the icon.
   */
  final double scale;

  /**
   * The background layer of the icon, drawn first. Contains elements that appear behind the cuboid
   * face.
   */
  BufferedImage backgroundImage = GraphicsUtils.createBlankImage(Move.MOVE_ICON_WIDTH,
      Move.MOVE_ICON_HEIGHT, Color.WHITE);

  /**
   * The middle layer of the icon. Contains the filled and unfilled cells of the cuboid face.
   */
  BufferedImage middleImage = GraphicsUtils.createClearImage(Move.MOVE_ICON_WIDTH,
      Move.MOVE_ICON_HEIGHT);

  /**
   * The foreground layer of the icon, drawn last. Contains cell grid lines, arrows, and the move
   * name.
   */
  BufferedImage foregroundImage = GraphicsUtils.createClearImage(Move.MOVE_ICON_WIDTH,
      Move.MOVE_ICON_HEIGHT);

  /**
   * Graphics context for drawing on the background layer.
   */
  Graphics2D backgroundGraphics = backgroundImage.createGraphics();

  /**
   * Graphics context for drawing on the middle layer.
   */
  Graphics2D middleGraphics = middleImage.createGraphics();

  /**
   * Graphics context for drawing on the foreground layer.
   */
  Graphics2D foregroundGraphics = foregroundImage.createGraphics();

  /**
   * Constructs a new CuboidMoveIconBuilder with the specified dimensions and move name. Initializes
   * the icon with a grid representing the cuboid face.
   *
   * @param width  The width of the cuboid face in cells
   * @param height The height of the cuboid face in cells
   * @param name   The name of the move to display on the icon
   */
  public CuboidMoveIconBuilder(int width, int height, String name) {
    this.width = width;
    this.height = height;
    // setup image
    GraphicsUtils.setupRenderingHints(foregroundGraphics);
    // work out scale of each small square
    double horizontalScale = (Move.MOVE_ICON_WIDTH - marginLeft - marginRight) / (1.0 * width);
    double verticalScale = (Move.MOVE_ICON_HEIGHT - marginTop - marginBottom) / (1.0 * height);
    scale = Math.min(horizontalScale, verticalScale);
    // center the grid
    originX = marginLeft + (int) ((Move.MOVE_ICON_WIDTH - marginLeft - marginRight - width * scale)
        / 2.0);
    // draw cuboid
    drawCuboid(originX, marginTop, width, height, scale);
    // write label
    drawName(foregroundGraphics, name);
  }

  /**
   * Static factory method to create a new CuboidMoveIconBuilder. Note: The method name has a typo
   * (builer), but it's preserved for compatibility.
   *
   * @param width  The width of the cuboid face in cells
   * @param height The height of the cuboid face in cells
   * @param name   The name of the move to display on the icon
   * @return A new CuboidMoveIconBuilder instance
   */
  public static CuboidMoveIconBuilder builer(int width, int height, String name) {
    return new CuboidMoveIconBuilder(width, height, name);
  }

  /**
   * Draws the move name centered at the bottom of the icon.
   *
   * @param g    The graphics context to draw on
   * @param name The name of the move to display
   */
  private static void drawName(Graphics2D g, String name) {
    GraphicsUtils.drawCenteredText(g, 0,
        Move.MOVE_ICON_HEIGHT - OutputConstants.LOG_FONT_HEIGHT - marginBottom,
        Move.MOVE_ICON_WIDTH, OutputConstants.LOG_FONT_HEIGHT, name, OutputConstants.LOG_FONT,
        Color.BLACK);
  }

  /**
   * Draws the basic cuboid grid structure with empty cells and grid lines.
   *
   * @param originX The x-coordinate where drawing begins
   * @param originY The y-coordinate where drawing begins
   * @param width   The width of the cuboid face in cells
   * @param height  The height of the cuboid face in cells
   * @param scale   The scale factor for each cell
   */
  private void drawCuboid(int originX, int originY, int width, int height, double scale) {
    // Fill the entire face with unfilled color
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

  /**
   * Fills all cells on the front face to indicate they are affected by the move.
   *
   * @return This builder instance for method chaining
   */
  public CuboidMoveIconBuilder fillFrontFace() {
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        fillFrontFace(x, y);
      }
    }
    return this;
  }

  /**
   * Fills a specific cell on the front face with the default fill color.
   *
   * @param x The x-coordinate of the cell to fill (0-based)
   * @param y The y-coordinate of the cell to fill (0-based)
   */
  private void fillFrontFace(int x, int y) {
    fillFrontFace(x, y, FILLED);
  }

  /**
   * Fills a specific cell on the front face with a specified color.
   *
   * @param x     The x-coordinate of the cell to fill (0-based)
   * @param y     The y-coordinate of the cell to fill (0-based)
   * @param color The color to fill the cell with
   */
  private void fillFrontFace(int x, int y, Color color) {
    middleGraphics.setColor(color);
    middleGraphics.fillRect((int) (originX + x * scale), (int) (marginTop + scale * y),
        (int) scale + 1, (int) scale + 1);
  }

  /**
   * Fills cells from the top edge to a specified depth to represent moves like U, Uw, 3U.
   *
   * @param n The number of layers from the top to fill
   * @return This builder instance for method chaining
   */
  public CuboidMoveIconBuilder fillFromTop(int n) {
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < n; y++) {
        fillFrontFace(x, y);
      }
    }
    return this;
  }

  /**
   * Fills cells from the bottom edge to a specified depth to represent moves like D, Dw, 3D.
   *
   * @param n The number of layers from the bottom to fill
   * @return This builder instance for method chaining
   */
  public CuboidMoveIconBuilder fillFromBottom(int n) {
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < n; y++) {
        fillFrontFace(x, height - y - 1);
      }
    }
    return this;
  }

  /**
   * Fills cells from the left edge to a specified depth to represent moves like L, Lw, 3L.
   *
   * @param n The number of layers from the left to fill
   * @return This builder instance for method chaining
   */
  public CuboidMoveIconBuilder fillFromLeft(int n) {
    for (int x = 0; x < n; x++) {
      for (int y = 0; y < height; y++) {
        fillFrontFace(x, y);
      }
    }
    return this;
  }

  /**
   * Fills cells from the right edge to a specified depth to represent moves like R, Rw, 3R.
   *
   * @param n The number of layers from the right to fill
   * @return This builder instance for method chaining
   */
  public CuboidMoveIconBuilder fillFromRight(int n) {
    for (int x = 0; x < n; x++) {
      for (int y = 0; y < height; y++) {
        fillFrontFace(width - x - 1, y);
      }
    }
    return this;
  }


  /**
   * Adds a rotation arrow to indicate the direction of the move.
   *
   * @param face    The face being rotated ('F', 'B', 'U', 'D', 'L', 'R')
   * @param inverse Whether the rotation is counterclockwise (true) or clockwise (false)
   * @return This builder instance for method chaining
   */
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
      default:
        log.warn("Unknown face: {}", face);
    }
    return this;
  }

  /**
   * Combines all image layers into a single final icon image.
   *
   * @return The completed move icon as a BufferedImage
   */
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
