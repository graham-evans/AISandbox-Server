/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.output;

import dev.aisandbox.server.engine.Simulation;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BitmapOutputRenderer implements OutputRenderer {

  private Simulation simulation;

  private long imageCounter = 0;

  private File outputDirectory = new File("./");

  private int skipframes = -1;

  @Override
  public void setup(Simulation simulation) {
    this.simulation = simulation;
  }

  @Override
  public String getName() {
    return "PNG file";
  }

  @Override
  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  @Override
  public void setSkipFrames(int framesToSkip) {
    if (framesToSkip > 0) {
      this.skipframes = framesToSkip;
    }
  }

  @Override
  public void display() {
    if (skipframes == 0 || imageCounter % skipframes == 0) {
      try {
        BufferedImage image = new BufferedImage(OutputConstants.HD_WIDTH, OutputConstants.HD_HEIGHT,
            BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        simulation.visualise(g2d);
        ImageIO.write(image, "png", new File(outputDirectory, imageCounter + ".png"));
      } catch (IOException e) {
        log.error("Error writing image to file", e);
      }
    }
    imageCounter++;
  }

}
