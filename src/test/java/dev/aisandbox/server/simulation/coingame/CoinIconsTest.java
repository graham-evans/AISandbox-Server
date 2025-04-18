package dev.aisandbox.server.simulation.coingame;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

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
    BufferedImage[] image = CoinIcons.getCoinImages(21);
    for (int i = 0; i < image.length; i++) {
      File outfile = new File("build/test/coingame/coins/" + i + ".png");
      outfile.getParentFile().mkdirs();
      ImageIO.write(image[i], "png", outfile);
    }
  }

  @Test
  public void testTriangle0() {
    assertArrayEquals(new int[]{0}, CoinIcons.getTriangleRows(0));
  }

  @Test
  public void testTriangle1() {
    assertArrayEquals(new int[]{1}, CoinIcons.getTriangleRows(1));
  }

  @Test
  public void testTriangle2() {
    assertArrayEquals(new int[]{2}, CoinIcons.getTriangleRows(2));
  }

  @Test
  public void testTriangle3() {
    assertArrayEquals(new int[]{2, 1}, CoinIcons.getTriangleRows(3));
  }

  @Test
  public void testTriangle4() {
    int[] res = CoinIcons.getTriangleRows(4);
    log.info("4 = {}", res);
    assertArrayEquals(new int[]{3, 1}, res);
  }

  @Test
  public void testTriangle5() {
    assertArrayEquals(new int[]{3, 2}, CoinIcons.getTriangleRows(5));
  }

  @Test
  public void testTriangle6() {
    assertArrayEquals(new int[]{3, 2, 1}, CoinIcons.getTriangleRows(6));
  }

  @Test
  public void testTriangle7() {
    assertArrayEquals(new int[]{4, 2, 1}, CoinIcons.getTriangleRows(7));
  }

}