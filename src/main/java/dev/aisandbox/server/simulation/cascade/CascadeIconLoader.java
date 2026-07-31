/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

import dev.aisandbox.server.simulation.cascade.model.TileColour;
import dev.aisandbox.server.simulation.cascade.model.TileType;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;

/**
 * Loads and caches the tile icon images used to render the Cascade board, keyed by tile type and
 * colour.
 */
@Slf4j
final class CascadeIconLoader {

  private static final String IMAGE_PATH = "/images/cascade/";

  private final Map<String, BufferedImage> cache = new HashMap<>();

  /**
   * Returns the icon for the given tile type/colour combination, loading and caching it on first
   * access.
   *
   * @param type   the tile type
   * @param colour the tile colour
   * @return the 128x128 icon image, or {@code null} if it could not be loaded
   */
  BufferedImage getIcon(TileType type, TileColour colour) {
    return cache.computeIfAbsent(imageName(type, colour), this::loadImage);
  }

  private BufferedImage loadImage(String name) {
    String path = IMAGE_PATH + name + ".png";
    try {
      return ImageIO.read(CascadeIconLoader.class.getResourceAsStream(path));
    } catch (IOException e) {
      log.error("Error loading cascade icon {}", path, e);
      return null;
    }
  }

  private static String imageName(TileType type, TileColour colour) {
    return switch (type) {
      case EMPTY -> "empty";
      case STONE -> "stone";
      case PRISM -> "xx";
      default -> "" + colourLetter(colour) + typeLetter(type);
    };
  }

  private static char colourLetter(TileColour colour) {
    return switch (colour) {
      case RED -> 'r';
      case BLUE -> 'b';
      case GREEN -> 'g';
      case YELLOW -> 'y';
      case PURPLE -> 'p';
      case NONE -> throw new IllegalStateException("No icon colour for NONE");
    };
  }

  private static char typeLetter(TileType type) {
    return switch (type) {
      case STANDARD -> 'o';
      case BOMB -> 'b';
      case ROCKET_H -> 'h';
      case ROCKET_V -> 'v';
      case ICE -> 'i';
      default -> throw new IllegalStateException("No icon letter for " + type);
    };
  }
}
