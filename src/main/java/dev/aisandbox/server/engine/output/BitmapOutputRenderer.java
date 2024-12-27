package dev.aisandbox.server.engine.output;

import dev.aisandbox.server.engine.Simulation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class BitmapOutputRenderer implements OutputRenderer {
    private final Simulation simulation;

    private long imageCounter = 0;

    @Override
    public String getName() {
        return "PNG file";
    }

    @Override
    public void display() {
        try {
            BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            simulation.visualise(g2d);
            ImageIO.write(image, "png", new File(imageCounter + ".png"));
        } catch (IOException e) {
            log.error("Error writing image to file", e);
        }
        imageCounter++;
    }

}
