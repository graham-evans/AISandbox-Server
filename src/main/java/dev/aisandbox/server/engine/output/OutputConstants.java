/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.output;

import dev.aisandbox.server.simulation.bandit.BanditRuntime;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Constants and resources for simulation output rendering.
 * <p>
 * This utility class provides standardized dimensions, fonts, colors, and other constants
 * used throughout the AI Sandbox for consistent visual output. All simulations should use
 * these constants to ensure uniform appearance and proper layout across different
 * simulation types.
 * </p>
 * <p>
 * The constants are organized into several categories:
 * </p>
 * <ul>
 *   <li><strong>Screen Dimensions:</strong> HD resolution constants for consistent output size</li>
 *   <li><strong>Layout Margins:</strong> Standard spacing for UI elements and widgets</li>
 *   <li><strong>Typography:</strong> Pre-loaded fonts for titles, text, and statistics</li>
 *   <li><strong>Assets:</strong> Common images like logos and icons</li>
 * </ul>
 * <p>
 * All constants are static and the class cannot be instantiated.
 * </p>
 */
@UtilityClass
@Slf4j
public class OutputConstants {

  // Screen dimension constants
  /** Standard HD width for all simulation output (1920 pixels) */
  public static final int HD_WIDTH = 1920;
  /** Standard HD height for all simulation output (1080 pixels) */
  public static final int HD_HEIGHT = 1080;
  
  // Logo dimensions
  /** Width of the AI Sandbox logo in pixels */
  public static final int LOGO_WIDTH = 91;
  /** Height of the AI Sandbox logo in pixels */
  public static final int LOGO_HEIGHT = 108;
  
  // Layout and spacing constants
  /** @deprecated Use specific margin constants instead */
  @Deprecated
  public static final int MARGIN = 100;
  /** Standard height for title areas */
  public static final int TITLE_HEIGHT = 50;
  /** Standard height for header sections */
  public static final int HEADER_HEIGHT = 40;
  /** Top margin for content areas */
  public static final int TOP_MARGIN = 50;
  /** Bottom margin for content areas */
  public static final int BOTTOM_MARGIN = 50;
  /** Left margin for content areas */
  public static final int LEFT_MARGIN = 50;
  /** Right margin for content areas */
  public static final int RIGHT_MARGIN = 50;
  /** Standard spacing between widgets and UI elements */
  public static final int WIDGET_SPACING = 50;
  /** Font height for log text display */
  public static final int LOG_FONT_HEIGHT = 16;
  /** Height for statistics display areas */
  public static final int STATISTICS_HEIGHT = 32;
  /** Height for widget title areas */
  public static final int WIDGET_TITLE_HEIGHT = 18;
  
  // Typography - pre-loaded fonts for consistent text rendering
  /** Font used for main titles and headings */
  public static final Font TITLE_FONT;
  /** Font used for section headers */
  public static final Font HEADER_FONT;
  /** Font used for log messages and general text */
  public static final Font LOG_FONT;
  /** Font used for statistical displays and data */
  public static final Font STATISTICS_FONT;
  /** Font used for widget titles and labels */
  public static final Font WIDGET_TITLE_FONT;

  // Shared graphical assets
  /** Pre-loaded AI Sandbox logo image */
  public static final BufferedImage LOGO;

  /** List of font file paths to attempt loading from resources */
  private static final List<String> FONT_LIST = List.of("/fonts/Arimo-VariableFont_wght.ttf",
      "/fonts/Hack-Regular.ttf");

  static {
    // load logo image from resources
    BufferedImage i;
    try {
      i = ImageIO.read(BanditRuntime.class.getResourceAsStream("/images/AILogo.png"));
    } catch (IOException e) {
      log.error("Error loading logo image", e);
      // Create fallback logo if resource loading fails
      i = new BufferedImage(LOGO_WIDTH, LOGO_HEIGHT, BufferedImage.TYPE_INT_RGB);
    }
    LOGO = i;
    // Load fonts
    GraphicsEnvironment GE = GraphicsEnvironment.getLocalGraphicsEnvironment();
    try {
      for (String path : FONT_LIST) {
        log.debug("Loading font from {}", path);
        Font font = Font.createFont(Font.TRUETYPE_FONT,
            OutputConstants.class.getResourceAsStream(path));
        log.debug("Registering font {}", font.getFontName());
        GE.registerFont(font);
      }
    } catch (FontFormatException | IOException e) {
      log.error("Error loading fonts", e);
    }
    // create base fonts
    TITLE_FONT = new Font("Arimo Regular", Font.PLAIN, TITLE_HEIGHT);
    HEADER_FONT = new Font("Arimo Regular", Font.PLAIN, HEADER_HEIGHT);
    LOG_FONT = new Font("Hack Regular", Font.PLAIN, LOG_FONT_HEIGHT);
    STATISTICS_FONT = new Font("Arimo Regular", Font.PLAIN, STATISTICS_HEIGHT);
    WIDGET_TITLE_FONT = new Font("Arimo Regular", Font.PLAIN, WIDGET_TITLE_HEIGHT);
  }
}
