/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import java.awt.Color;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents the available visual themes for the AI Sandbox application UI.
 *
 * <p>Each theme defines a complete color palette including background colors, text colors, widget
 * styling, graph and visualization colors, and agent-specific colors. The themes are designed to
 * provide consistent visual styling across the entire application interface.
 *
 * <p>Available themes:
 * <ul>
 *   <li>LIGHT - A light theme with subtle blue accents and high contrast</li>
 *   <li>WARM - A warm color palette with beige/cream backgrounds</li>
 *   <li>DARK - A dark theme with high contrast for low-light environments</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Theme {

  /**
   * Light theme with subtle blue accents and high contrast. Primary background: light gray
   * (#efefef) Text: dark gray (#4c4c4d)
   */
  LIGHT(Color.decode("#f0f0f0"), // Main base color
      Color.decode("#ffffff"), // background color for panels
      Color.decode("#efefef"), // border of panels
      Color.decode("#000066"), // primary color
      Color.decode("#660000"), // secondary color
      Color.decode("#fffaf0"), // accent
      Color.decode("#4C4C4D"), // text
      Color.decode("#005500"), // baize
      Color.decode("#008800")), // baize border

  /**
   * Warm theme with beige/cream background and red accents. Primary background: cream (#fef4e1)
   * Text: darker gray (#494949)
   */
  WARM(Color.decode("#fef4e1"), // Base colour
      Color.decode("#494949"), // background
      Color.white,             // border
      Color.white,             // primary
      Color.decode("#494949"), // secondary
      Color.white,             // accent
      Color.decode("#ea423d"), // text
      Color.GREEN, // baize
      Color.YELLOW), // baize border

  /**
   * Dark theme with high contrast for low-light environments. Primary background: deep blue
   * (#000d43) Text: white
   */
  DARK(Color.black, Color.darkGray, Color.lightGray, Color.blue, Color.red, Color.yellow,
      Color.white, Color.GREEN, Color.cyan);

  private final Color base;
  private final Color background;
  private final Color border;
  private final Color primary;
  private final Color secondary;
  private final Color accent;
  private final Color text;
  private final Color baize;
  private final Color baizeBorder;
}
