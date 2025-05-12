package dev.aisandbox.server.simulation.coingame;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import dev.aisandbox.server.engine.Theme;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class CoinIconsTest {

  @Test
  public void createCoinImages() throws IOException {
    BufferedImage[] image = CoinIcons.getCoinImages(21, Theme.LIGHT);
    for (int i = 0; i < image.length; i++) {
      File outfile = new File("build/test/coingame/coins/" + i + ".png");
      outfile.getParentFile().mkdirs();
      ImageIO.write(image[i], "png", outfile);
    }
  }

}