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
 *   <li>DARK - A dark theme with high contrast for low-light environments</li>
 *   <li>MIDNIGHT - A dark blue theme with elegant midnight tones</li>
 *   <li>WARM - A warm theme with cozy earth tones and orange accents</li>
 *   <li>FOREST - A natural theme with green tones and earthy accents</li>
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
  DARK(Color.decode("#1e1f22"),      // Base color
      Color.decode("#2b2d30"),   // Background
      Color.decode("#343538"),  // Border
      Color.decode("#5489f6"),       // Primary
      Color.decode("#db5c5c"),        // Secondary
      Color.decode("#cc895b"),     // Accent
      Color.white,      // Text
      Color.decode("#005500"), // Baize
      Color.decode("#003300"),  // Baize border
      "/images/AILogoW.png" // logo
  ),

  /**
   * Midnight theme with deep blue tones for elegant low-light usage.
   *
   * <p>Color palette:
   * <ul>
   *   <li>Primary background: deep navy (#0f1419)</li>
   *   <li>Panel background: midnight blue (#1a2332)</li>
   *   <li>Text: light blue-gray for contrast</li>
   *   <li>Cool blue accent colors throughout</li>
   * </ul>
   */
  MIDNIGHT(Color.decode("#0f1419"),     // Base color - deep navy
      Color.decode("#1a2332"),      // Background - midnight blue
      Color.decode("#2d3748"),      // Border - slate blue
      Color.decode("#4299e1"),      // Primary - bright blue
      Color.decode("#63b3ed"),      // Secondary - light blue
      Color.decode("#9f7aea"),      // Accent - purple-blue
      Color.decode("#e2e8f0"),      // Text - light blue-gray
      Color.decode("#2a4365"),      // Baize - dark blue
      Color.decode("#1a365d"),      // Baize border - darker blue
      "/images/AILogoW.png"         // logo
  ),

  /**
   * Warm theme with cozy earth tones and soft orange accents.
   *
   * <p>Color palette:
   * <ul>
   *   <li>Primary background: warm cream (#faf7f2)</li>
   *   <li>Panel background: soft ivory (#fffef9)</li>
   *   <li>Text: warm brown for comfort</li>
   *   <li>Orange and terracotta accent colors</li>
   * </ul>
   */
  WARM(Color.decode("#faf7f2"),     // Base color - warm cream
      Color.decode("#fffef9"),      // Background - soft ivory
      Color.decode("#f0ede6"),      // Border - warm beige
      Color.decode("#d69e2e"),      // Primary - golden orange
      Color.decode("#c05621"),      // Secondary - terracotta
      Color.decode("#ed8936"),      // Accent - warm orange
      Color.decode("#744210"),      // Text - warm brown
      Color.decode("#68d391"),      // Baize - warm green
      Color.decode("#48bb78"),      // Baize border - darker green
      "/images/AILogo.png"          // logo
  ),

  /**
   * Forest theme with natural green tones and earthy accents.
   *
   * <p>Color palette:
   * <ul>
   *   <li>Primary background: soft sage (#f7f9f7)</li>
   *   <li>Panel background: pale mint (#fdfefd)</li>
   *   <li>Text: deep forest green</li>
   *   <li>Natural green and earth tone accents</li>
   * </ul>
   */
  FOREST(Color.decode("#f7f9f7"),    // Base color - soft sage
      Color.decode("#fdfefd"),       // Background - pale mint
      Color.decode("#e6f4ea"),       // Border - light green
      Color.decode("#2d7d32"),       // Primary - forest green
      Color.decode("#388e3c"),       // Secondary - medium green
      Color.decode("#8bc34a"),       // Accent - lime green
      Color.decode("#1b5e20"),       // Text - deep forest
      Color.decode("#4caf50"),       // Baize - natural green
      Color.decode("#2e7d32"),       // Baize border - darker green
      "/images/AILogo.png"           // logo
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
