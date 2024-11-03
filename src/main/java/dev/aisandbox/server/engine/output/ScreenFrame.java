package dev.aisandbox.server.engine.output;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class ScreenFrame extends JFrame {

    private BufferedImage image;

    private ImageCanvas canvas;

    public ScreenFrame() throws HeadlessException {
        // setup default image
        image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 800, 600);


        this.setTitle("AISandbox-Server");
        canvas = new ImageCanvas();
        canvas.setDoubleBuffered(true);
        this.add(canvas);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.toFront();
    }

    public void updateImage(BufferedImage image) {
        this.image = image;
        canvas.repaint();
    }

    private class ImageCanvas extends JPanel {
        @Override
        public void paint(Graphics g) {
            double horizontalScale = (double) this.getWidth() / image.getWidth();
            double verticalScale = (double) this.getHeight() / image.getHeight();

            double scale = Math.min(horizontalScale, verticalScale);

            int startX = (int) ((this.getWidth() - image.getWidth() * scale) / 2.0);
            int startY = (int) ((this.getHeight() - image.getHeight() * scale) / 2.0);

            log.debug("drawing image {}x{} scaled to {}x{} at {},{} on canvas dimensions {}x{} ima", image.getWidth(), image.getHeight(), image.getWidth() * scale, image.getHeight() * scale, startX, startY, getWidth(), getHeight());

            g.drawImage(image, startX, startY, (int) (image.getWidth() * scale), (int) (image.getHeight() * scale), null);
        }

    }


}
