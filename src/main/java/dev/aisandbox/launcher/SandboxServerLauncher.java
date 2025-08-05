/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.launcher;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;

/**
 * Main entry point for the AI Sandbox Server application.
 * <p>
 * This launcher class determines whether to start the application in command-line interface (CLI)
 * mode or graphical user interface (GUI) mode based on the presence of command-line arguments.
 * </p>
 * <ul>
 *   <li>If command-line arguments are provided, launches the CLI version</li>
 *   <li>If no arguments are provided, launches the JavaFX GUI version</li>
 * </ul>
 * <p>
 * The CLI mode is suitable for automated runs, scripting, and server deployments where
 * a graphical interface is not available or needed. The GUI mode provides an interactive
 * interface for configuring and running simulations with real-time visualization.
 * </p>
 *
 * @see SandboxServerCLIApplication
 * @see SandboxServerFXApplication
 */
@Slf4j
public class SandboxServerLauncher {

  /**
   * Main entry point for the application.
   * <p>
   * Analyzes the command-line arguments to determine which mode to launch:
   * </p>
   * <ul>
   *   <li>Arguments present: Launch CLI application</li>
   *   <li>No arguments: Launch JavaFX GUI application</li>
   * </ul>
   *
   * @param args command-line arguments passed to the application
   */
  public static void main(String[] args) {
    log.info("Launching AISandbox");
    if (args.length > 0) {
      // launch CLI
      log.info("Launching AISandbox Server CLI");
      SandboxServerCLIApplication application = new SandboxServerCLIApplication();
      application.run(args);
    } else {
      // launch GUI
      log.info("Launching AISandbox Server FX");
      Application.launch(SandboxServerFXApplication.class, args);
    }
  }

}
