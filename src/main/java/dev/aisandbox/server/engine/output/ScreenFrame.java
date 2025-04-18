package dev.aisandbox.server.engine.output;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.Serial;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ScreenFrame extends JFrame {

  @Serial
  private static final long serialVersionUID = -5464920993358869235L;
  private final ImageCanvas canvas;
  private BufferedImage image;

  public ScreenFrame() throws HeadlessException {
    // set title
    super("AISandbox-Server");
    // setup default image
    try {
      image = ImageIO.read(
          ScreenFrame.class.getResourceAsStream("/images/backgrounds/testcard.png"));
    } catch (Exception e) {
      log.error("Error loading testcard image", e);
      image = new BufferedImage(OutputConstants.HD_WIDTH, OutputConstants.HD_HEIGHT,
          BufferedImage.TYPE_INT_RGB);
      Graphics2D g = image.createGraphics();
      g.setColor(Color.BLUE);
      g.fillRect(0, 0, OutputConstants.HD_WIDTH, OutputConstants.HD_HEIGHT);
    }
    canvas = new ImageCanvas();
    canvas.setDoubleBuffered(true);
    this.add(canvas);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setSize(800, 600);
    this.setLocationRelativeTo(null);
    this.setVisible(true);
    this.toFront();
    this.setIconImage(
        new ImageIcon(ScreenFrame.class.getResource("/images/AILogo.png")).getImage());
  }

  public void updateImage(BufferedImage image) {
    this.image = image;
    canvas.repaint();
  }

  private class ImageCanvas extends JPanel {

    @Serial
    private static final long serialVersionUID = -1432009394376930530L;

    @Override
    public void paint(Graphics g) {
      double horizontalScale = (double) this.getWidth() / OutputConstants.HD_WIDTH;
      double verticalScale = (double) this.getHeight() / OutputConstants.HD_HEIGHT;

      double scale = Math.min(horizontalScale, verticalScale);

      int startX = (int) ((this.getWidth() - OutputConstants.HD_WIDTH * scale) / 2.0);
      int startY = (int) ((this.getHeight() - OutputConstants.HD_HEIGHT * scale) / 2.0);

      log.debug("drawing image {}x{} scaled to {}x{} at {},{} on canvas dimensions {}x{} ima",
          image.getWidth(), image.getHeight(), image.getWidth() * scale, image.getHeight() * scale,
          startX, startY, getWidth(), getHeight());

      g.drawImage(image, startX, startY, (int) (OutputConstants.HD_WIDTH * scale),
          (int) (OutputConstants.HD_HEIGHT * scale), null);
    }

  }


}
