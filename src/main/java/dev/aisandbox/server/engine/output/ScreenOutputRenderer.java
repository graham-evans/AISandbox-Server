package dev.aisandbox.server.engine.output;

import dev.aisandbox.server.engine.Simulation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
@RequiredArgsConstructor
public class ScreenOutputRenderer implements OutputRenderer {
    private final Simulation simulation;

    JFrame frame = null;
    JLabel label = null;
    long frameCounter = 1;

    @Override
    public String getName() {
        return "screen";
    }

    @Override
    public void display() {
        // get image
        BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        simulation.visualise(g2d);
        // setup screen
        if (frame == null) {
            // setup label
            label = new JLabel();
            label.setIcon(new ImageIcon(image));
            label.setText("Frame " + frameCounter);
            label.setVerticalTextPosition(SwingConstants.BOTTOM);
            label.setHorizontalTextPosition(SwingConstants.CENTER);
            frameCounter++;
            // setup screen
            frame = new JFrame("Simulation Output");
            frame.add(label);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.toFront();
        }
    }

    @Override
    public void close() {
        // close window
    }

    public class MyCanvas extends Canvas {
        public void paint(Graphics g) {
            Toolkit t = Toolkit.getDefaultToolkit();
            Image i = t.getImage("p3.gif");
            g.drawImage(i, 120, 100, this);
        }
    }

}
