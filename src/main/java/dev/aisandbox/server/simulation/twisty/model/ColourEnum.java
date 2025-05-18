/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.twisty.model;

import java.awt.Color;
import lombok.Getter;

/**
 * Enumeration of colors used in the twisty simulation. Each color is defined with a hexadecimal
 * color code and a character representation. The enum provides convenient access to both string hex
 * codes and AWT Color objects.
 */
public enum ColourEnum {
  /**
   * White color (#FFFFFF) represented by character 'W'.
   */
  WHITE("FFFFFF", 'W'),
  /**
   * Orange color (#FFA600) represented by character 'O'.
   */
  ORANGE("FFA600", 'O'),
  /**
   * Green color (#068E00) represented by character 'G'.
   */
  GREEN("068E00", 'G'),
  /**
   * Red color (#8E0000) represented by character 'R'.
   */
  RED("8E0000", 'R'),
  /**
   * Blue color (#02008E) represented by character 'B'.
   */
  BLUE("02008E", 'B'),
  /**
   * Yellow color (#F7FF00) represented by character 'Y'.
   */
  YELLOW("F7FF00", 'Y'),
  /**
   * Olive color (#9FD82C) represented by character 'L'.
   */
  OLIVE("9FD82C", 'L'),
  /**
   * Pink color (#D12CD8) represented by character 'P'.
   */
  PINK("D12CD8", 'P'),
  /**
   * Cyan color (#2CD1D8) represented by character 'C'.
   */
  CYAN("2CD1D8", 'C'),
  /**
   * Grey color (#B8B8B8) represented by character 'E'.
   */
  GREY("B8B8B8", 'E'),
  /**
   * Ivory color (#D2D399) represented by character 'I'.
   */
  IVORY("D2D399", 'I');

  /**
   * The hexadecimal string representation of the color (without leading #).
   */
  @Getter
  private final String hex;

  /**
   * The Java AWT Color object representation of this color.
   */
  @Getter
  private final Color awtColour;

  /**
   * The character representation of this color (used for compact notation).
   */
  @Getter
  private final char character;

  /**
   * Constructs a new ColourEnum with the specified hexadecimal color code and character
   * representation.
   *
   * @param hex       The hexadecimal color code (without leading #)
   * @param character The character representation of the color
   */
  ColourEnum(String hex, char character) {
    this.hex = hex;
    this.character = character;
    awtColour = new Color(Integer.valueOf(hex.substring(0, 2), 16),
        Integer.valueOf(hex.substring(2, 4), 16), Integer.valueOf(hex.substring(4, 6), 16));
  }
}
