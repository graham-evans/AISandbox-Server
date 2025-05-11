package dev.aisandbox.server.engine;

import java.awt.Color;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents the available visual themes for the AI Sandbox application UI.
 * <p>
 * Each theme defines a complete color palette including background colors, text colors, widget
 * styling, graph and visualization colors, and agent-specific colors. The themes are designed to
 * provide consistent visual styling across the entire application interface.
 * <p>
 * Available themes:
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
  LIGHT(Color.decode("#efefef"), // Main background color
      Color.decode("#4c4c4d"), // Primary text color
      Color.decode("#fffaf0"), // Widget background color (floral white)
      Color.decode("#fffaf0"), // Graph background color (floral white)
      Color.decode("#4c4c4d"), // Graph outline/axis color
      Color.decode("#fffaf0"), // Baize (play area) color
      Color.decode("#07144b"), // Primary graph data color (dark blue)

      Color.decode("#85d8ef"), // Agent 1 main color (light blue)
      Color.decode("#85d8ef"), // Agent 1 highlight color
      Color.decode("#85d8ef"), // Agent 1 lowlight color

      Color.decode("#640d14"), // Agent 2 main color (dark red)
      Color.decode("#800e13"), // Agent 2 highlight color
      Color.decode("#38040e"), // Agent 2 lowlight color

      Color.decode("#ffa200"), // Selected agent main color (orange)
      Color.decode("#ffaa00"), // Selected agent highlight color
      Color.decode("#ff9500")  // Selected agent lowlight color
  ),

  /**
   * Warm theme with beige/cream background and red accents. Primary background: cream (#fef4e1)
   * Text: darker gray (#494949)
   */
  WARM(Color.decode("#fef4e1"), // Main background color (cream)
      Color.decode("#494949"), // Primary text color
      Color.white,             // Widget background color
      Color.white,             // Graph background color
      Color.decode("#494949"), // Graph outline/axis color
      Color.white,             // Baize (play area) color
      Color.decode("#ea423d"), // Primary graph data color (warm red)

      Color.decode("#038da2"), // Agent 1 main color (teal)
      Color.decode("#038da2"), // Agent 1 highlight color
      Color.decode("#038da2"), // Agent 1 lowlight color

      Color.decode("#640d14"), // Agent 2 main color (dark red)
      Color.decode("#800e13"), // Agent 2 highlight color
      Color.decode("#38040e"), // Agent 2 lowlight color

      Color.decode("#ffa200"), // Selected agent main color (orange)
      Color.decode("#ffaa00"), // Selected agent highlight color
      Color.decode("#ff9500")  // Selected agent lowlight color
  ),

  /**
   * Dark theme with high contrast for low-light environments. Primary background: deep blue
   * (#000d43) Text: white
   */
  DARK(Color.decode("#000d43"), // Main background color (deep blue)
      Color.white,             // Primary text color
      Color.decode("#001155"), // Widget background color (slightly lighter blue)
      Color.decode("#ffe0a6"), // Graph background color (light amber)
      Color.decode("#000d43"), // Graph outline/axis color
      Color.decode("#001155"), // Baize (play area) color
      Color.decode("#ff0800"), // Primary graph data color (bright red)

      Color.decode("#014f89"), // Agent 1 main color (medium blue)
      Color.decode("#2a6f97"), // Agent 1 highlight color
      Color.decode("#01497c"), // Agent 1 lowlight color

      Color.decode("#640d14"), // Agent 2 main color (dark red)
      Color.decode("#800e13"), // Agent 2 highlight color
      Color.decode("#38040e"), // Agent 2 lowlight color

      Color.decode("#ffa200"), // Selected agent main color (orange)
      Color.decode("#ffaa00"), // Selected agent highlight color
      Color.decode("#ff9500")  // Selected agent lowlight color
  );

  /**
   * The main application background color
   */
  private final Color background;

  /**
   * The primary text color used throughout the application
   */
  private final Color text;

  /**
   * Background color for UI widgets and components
   */
  private final Color widgetBackground;

  /**
   * Background color for graph and chart areas
   */
  private final Color graphBackground;

  /**
   * Color for graph axes, borders and outlines
   */
  private final Color graphOutlineColor;

  /**
   * Color for the main gameplay or visualization area
   */
  private final Color baize;

  /**
   * Primary color for graph data visualization
   */
  private final Color graphColor1;

  /**
   * Main color for the first agent
   */
  private final Color agent1Main;

  /**
   * Highlight/accent color for the first agent
   */
  private final Color agent1Highlight;

  /**
   * Secondary/lowlight color for the first agent
   */
  private final Color agent1Lowlight;

  /**
   * Main color for the second agent
   */
  private final Color agent2Main;

  /**
   * Highlight/accent color for the second agent
   */
  private final Color agent2Highlight;

  /**
   * Secondary/lowlight color for the second agent
   */
  private final Color agent2Lowlight;

  /**
   * Main color for the currently selected agent
   */
  private final Color agentSelectedMain;

  /**
   * Highlight/accent color for the currently selected agent
   */
  private final Color agentSelectedHighlight;

  /**
   * Secondary/lowlight color for the currently selected agent
   */
  private final Color agentSelectedLowlight;

  /**
   * Returns the main color for the specified agent index.
   *
   * @param i The agent index (0 for first agent, 1 for second agent)
   * @return The main color for the specified agent, or dark gray if the index is out of range
   */
  public Color getAgentMain(int i) {
    return switch (i) {
      case 0 -> agent1Main;
      case 1 -> agent2Main;
      default -> Color.DARK_GRAY;
    };
  }
}
