/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
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
 *   <li>LIGHT - A light theme with high contrast</li>
 *   <li>WARM - A warm color palette with beige/cream backgrounds</li>
 *   <li>DARK - A dark theme with high contrast for low-light environments</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Theme {

  /**
   * Light theme with subtle blue accents and high contrast.
   *
   * <p>Color palette:
   * <ul>
   *   <li>Primary background: light gray (#f0f0f0)</li>
   *   <li>Panel background: white (#ffffff)</li>
   *   <li>Text: dark gray (#4c4c4d)</li>
   *   <li>Primary accent: navy blue (#000066)</li>
   *   <li>Secondary accent: dark red (#660000)</li>
   * </ul>
   */
  LIGHT(Color.decode("#f0f0f0"), // Main base color
      Color.decode("#ffffff"), // Background color for panels
      Color.decode("#efefef"), // Border of panels
      Color.decode("#000066"), // Primary color
      Color.decode("#660000"), // Secondary color
      Color.decode("#fffaf0"), // Accent
      Color.decode("#4C4C4D"), // Text
      Color.decode("#005500"), // Baize
      Color.decode("#008800"),  // Baize border
      "/images/AILogo.png" // logo
  ),

  /**
   * Warm theme with beige/cream background and red accents.
   *
   * <p>Color palette:
   * <ul>
   *   <li>Primary background: cream (#fef4e1)</li>
   *   <li>Panel background: dark gray (#494949)</li>
   *   <li>Text: bright red (#ea423d)</li>
   *   <li>Borders and accents: white</li>
   * </ul>
   */
  WARM(Color.decode("#fef4e1"), // Base color
      Color.decode("#494949"), // Background
      Color.white,             // Border
      Color.white,             // Primary
      Color.decode("#494949"), // Secondary
      Color.white,             // Accent
      Color.decode("#ea423d"), // Text
      Color.decode("#005500"), // Baize
      Color.decode("#008800"),  // Baize border
      "/images/AILogo.png" // logo
  ),

  /**
   * Dark theme with high contrast for low-light environments.
   *
   * <p>Color palette:
   * <ul>
   *   <li>Primary background: black</li>
   *   <li>Panel background: dark gray</li>
   *   <li>Text: white for maximum contrast</li>
   *   <li>Bright accent colors for visibility</li>
   * </ul>
   */
  DARK(Color.black,      // Base color
      Color.decode("#2b2d30"),   // Background
      Color.decode("#343538"),  // Border
      Color.blue,       // Primary
      Color.red,        // Secondary
      Color.decode("#cc895b"),     // Accent
      Color.white,      // Text
      Color.decode("#005500"), // Baize
      Color.decode("#003300"),  // Baize border
      "/images/AILogoW.png" // logo
  );

  private final Color base;
  private final Color background;
  private final Color border;
  private final Color primary;
  private final Color secondary;
  private final Color accent;
  private final Color text;
  private final Color baize;
  private final Color baizeBorder;
  private final String logo;

  private final BufferedImage logoImage;

  Theme(Color base, Color background, Color border, Color primary, Color secondary, Color accent,
      Color text, Color baize, Color baizeBorder, String logo) {
    this.base = base;
    this.background = background;
    this.border = border;
    this.primary = primary;
    this.secondary = secondary;
    this.accent = accent;
    this.text = text;
    this.baize = baize;
    this.baizeBorder = baizeBorder;
    this.logo = logo;

    BufferedImage tempImage = null;
    try {
      tempImage = ImageIO.read(Objects.requireNonNull(Theme.class.getResourceAsStream(logo)));
    } catch (IOException e) {
      e.printStackTrace();
    }
    logoImage = tempImage;
  }
}
