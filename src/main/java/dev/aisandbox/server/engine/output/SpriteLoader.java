/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.output;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to load a spritesheet and return a {@link java.util.List} of
 * {@link java.awt.image.BufferedImage}.
 */
@Slf4j
@UtilityClass
public class SpriteLoader {

  /**
   * Load sprites as a list of {@link java.awt.image.BufferedImage}'s from a resource path.
   *
   * <p>The source image will be cut up into pieces, each with the given dimensions.
   *
   * @param path   The file path to load from.
   * @param width  the width of the sprites
   * @param height the height of the sprites
   * @return a {@link java.util.List} of {@link java.awt.image.BufferedImage}'s.
   */
  public static List<BufferedImage> loadSpritesFromResources(String path, int width, int height) {
    try {
      BufferedImage sheet = ImageIO.read(SpriteLoader.class.getResourceAsStream(path));
      List<BufferedImage> images = new ArrayList<>();
      int x = 0;
      int y = 0;
      while (y < sheet.getHeight()) {
        while (x < sheet.getWidth()) {
          images.add(sheet.getSubimage(x, y, width, height));
          x += width;
        }
        x = 0;
        y += height;
      }
      return images;
    } catch (IOException e) {
      log.error("Error while loading images from {}", path, e);
      return List.of();
    }
  }
}
