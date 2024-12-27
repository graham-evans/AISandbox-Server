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

    ScreenFrame screenFrame=null;

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
        if (screenFrame == null) {
            screenFrame = new ScreenFrame();
        }
        // push image
        screenFrame.updateImage(image);
    }

    @Override
    public void close() {
        // close window
    }



}
