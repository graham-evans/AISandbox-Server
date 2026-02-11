/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.output;

import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.fx.RuntimeController;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Output renderer that displays simulation frames in the JavaFX interface.
 *
 * <p>This renderer sends the current simulation visualization to the RuntimeController
 * for display in the JavaFX GUI, with optional frame skipping for performance optimization.
 */
@Slf4j
@RequiredArgsConstructor
public class FXRenderer implements OutputRenderer {

  private final RuntimeController runtime;
  int skipframes = 0;
  private Simulation simulation;
  private long imageCounter = 0;

  @Override
  public String getName() {
    return "JavaFX Renderer";
  }

  @Override
  public void setup(Simulation simulation) {
    this.simulation = simulation;
  }

  @Override
  public void setSkipFrames(int framesToSkip) {
    skipframes = framesToSkip;
  }

  @Override
  public void display() {
    if (skipframes == 0 || imageCounter % skipframes == 0) {
      BufferedImage image = new BufferedImage(OutputConstants.HD_WIDTH, OutputConstants.HD_HEIGHT,
          BufferedImage.TYPE_INT_RGB);
      Graphics2D g2d = image.createGraphics();
      simulation.visualise(g2d);
      runtime.updateImage(image);
    }
    imageCounter++;
  }

  @Override
  public void write(String text) {
    runtime.updateOutput(text);
  }
}
