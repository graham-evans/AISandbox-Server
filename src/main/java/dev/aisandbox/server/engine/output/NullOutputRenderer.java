/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.output;

import dev.aisandbox.server.engine.Simulation;

/**
 * A no-operation implementation of {@link OutputRenderer} for headless execution.
 *
 * <p>This renderer is used when visual output is not required, such as in automated testing,
 * batch processing, or command-line execution scenarios. It implements all required methods but
 * performs no actual rendering operations.
 *
 * <p>The only visible output from this renderer is text written to the console via
 * {@link #write(String)}, which redirects to {@code System.out.println()}.
 *
 * <p>This implementation is useful for:
 * <ul>
 *   <li>Unit testing simulations without GUI dependencies</li>
 *   <li>Performance benchmarking without rendering overhead</li>
 *   <li>Server-side execution where visual output is not needed</li>
 *   <li>Batch processing of multiple simulation runs</li>
 * </ul>
 *
 * @see OutputRenderer
 */
public class NullOutputRenderer implements OutputRenderer {

  /**
   * Returns the name identifier for this renderer.
   *
   * @return "none" - indicating no visual output will be generated
   */
  @Override
  public String getName() {
    return "none";
  }

  /**
   * No-operation setup method.
   *
   * <p>This implementation does nothing as no initialization is required for null output.
   *
   * @param simulation the simulation to render (ignored in this implementation)
   */
  @Override
  public void setup(Simulation simulation) {
    // do nothing
  }

  /**
   * No-operation display method.
   *
   * <p>This implementation does nothing as no visual rendering is performed. Simulations will
   * continue to run normally, but no visual output will be generated.
   */
  @Override
  public void display() {
    // do nothing;
  }

  /**
   * Outputs text to the console.
   *
   * <p>This is the only method that produces visible output from the null renderer. Text is
   * printed directly to {@code System.out}, making it useful for logging simulation progress or
   * results in headless execution environments.
   *
   * @param text the text to write to the console
   */
  @Override
  public void write(String text) {
    System.out.println(text);
  }
}
