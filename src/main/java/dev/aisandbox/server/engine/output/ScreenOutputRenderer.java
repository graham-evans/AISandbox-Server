package dev.aisandbox.server.engine.output;

import dev.aisandbox.server.engine.Simulation;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ScreenOutputRenderer implements OutputRenderer {

  ScreenFrame screenFrame = null;
  private Simulation simulation;

  @Override
  public String getName() {
    return "screen";
  }

  @Override
  public void setup(Simulation simulation) {
    this.simulation = simulation;
    screenFrame = new ScreenFrame();
  }

  @Override
  public void display() {
    // get image
    BufferedImage image = new BufferedImage(OutputConstants.HD_WIDTH, OutputConstants.HD_HEIGHT,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();
    simulation.visualise(g2d);
    // push image
    screenFrame.updateImage(image);
  }

  @Override
  public void close() {
    // close window
    screenFrame.dispose();
  }


}
