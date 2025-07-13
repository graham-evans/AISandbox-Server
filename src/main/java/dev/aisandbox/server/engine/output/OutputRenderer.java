/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.output;

import dev.aisandbox.server.engine.Simulation;
import java.io.File;

/**
 * Interface for rendering simulation output in various formats.
 * 
 * <p>OutputRenderer implementations are responsible for taking simulation data and rendering it
 * in a specific format or medium (e.g., visual display, file output, network stream, etc.).
 * This interface provides a standardized way to handle different types of output rendering
 * while maintaining flexibility for various implementation approaches.</p>
 * 
 * <p>The typical lifecycle of an OutputRenderer is:
 * <ol>
 *   <li>Creation and configuration</li>
 *   <li>Setup with simulation data via {@link #setup(Simulation)}</li>
 *   <li>Optional configuration of frame skipping via {@link #setSkipFrames(int)}</li>
 *   <li>Optional configuration of output directory via {@link #setOutputDirectory(File)}</li>
 *   <li>Repeated calls to {@link #display()} for each frame/update</li>
 *   <li>Cleanup via {@link #close()}</li>
 * </ol></p>
 * 
 * @author AI Sandbox Team
 * @since 1.0
 */
public interface OutputRenderer {

  /**
   * Returns the human-readable name of this output renderer.
   * 
   * <p>This name is typically used for user interface display purposes,
   * logging, and identification of the renderer type.</p>
   * 
   * @return the display name of this renderer, never null
   */
  String getName();

  /**
   * Initializes the renderer with the simulation that will be rendered.
   * 
   * <p>This method is called once before rendering begins and provides the renderer
   * with access to the simulation data it needs to perform its rendering tasks.
   * Implementations should use this opportunity to prepare any resources or
   * initialize internal state based on the simulation properties.</p>
   * 
   * @param simulation the simulation instance to be rendered, must not be null
   */
  void setup(Simulation simulation);

  /**
   * Configures the renderer to skip a specified number of frames during rendering.
   * 
   * <p>This method allows for performance optimization by reducing the rendering
   * frequency. For example, if framesToSkip is set to 2, the renderer will only
   * render every second frame (skipping 1, rendering 1).</p>
   * 
   * <p>The default implementation does nothing, indicating that frame skipping
   * is not supported or not applicable for this renderer type.</p>
   * 
   * @param framesToSkip the number of frames to skip between renders, must be >= 0
   * @throws IllegalArgumentException if framesToSkip is negative
   */
  default void setSkipFrames(int framesToSkip) {
    // Default implementation: no frame skipping support
    // Subclasses should override if they support frame skipping optimization
  }

  /**
   * Sets the output directory for file-based renderers.
   * 
   * <p>This method is relevant for renderers that produce file output (e.g., image
   * sequences, video files, data exports). The provided directory will be used
   * as the base location for any files created by this renderer.</p>
   * 
   * <p>The default implementation does nothing, indicating that this renderer
   * does not produce file output or does not require directory configuration.</p>
   * 
   * @param outputDirectory the directory where output files should be created,
   *                       may be null to use a default location
   */
  default void setOutputDirectory(File outputDirectory) {
    // Default implementation: no file output support
    // Subclasses should override if they produce file-based output
  }

  /**
   * Renders the current state of the simulation.
   * 
   * <p>This method is called repeatedly during simulation execution to update
   * the rendered output with the current simulation state. Implementations should
   * read the current state from the simulation provided in {@link #setup(Simulation)}
   * and render it according to their specific output format.</p>
   * 
   * <p>This method should be designed to execute efficiently as it may be called
   * many times per second during simulation execution.</p>
   * 
   */
  void display();


  void write(String text);

  /**
   * Closes the renderer and releases any resources it holds.
   * 
   * <p>This method should be called when rendering is complete to ensure proper
   * cleanup of resources such as file handles, graphics contexts, network connections,
   * or any other system resources that the renderer may have acquired.</p>
   * 
   * <p>The default implementation does nothing, indicating that this renderer
   * does not require explicit resource cleanup. Implementations that acquire
   * resources should override this method to ensure proper cleanup.</p>
   * 
   * <p>After this method is called, the renderer should not be used for further
   * rendering operations.</p>
   */
  default void close() {
    // Default implementation: no resources to clean up
    // Subclasses should override if they need to release resources
  }
}
