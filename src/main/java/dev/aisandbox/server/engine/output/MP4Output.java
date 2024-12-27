package dev.aisandbox.server.engine.output;

import dev.aisandbox.server.engine.Simulation;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * MP4Output class.
 */
@Slf4j
@RequiredArgsConstructor
public class MP4Output implements OutputRenderer {

    private final Simulation simulation;
    private final File outputFile;
    private SeekableByteChannel out = null;
    private AWTSequenceEncoder encoder;
    @Setter
    private int framesToSkip = -1;
    private long imageCounter = 0;


    @Override
    public String getName() {
        return "MP4 encoder";
    }

    @Override
    public void setup() {
        try {
            out = NIOUtils.writableFileChannel(outputFile.getAbsolutePath());
            encoder = new AWTSequenceEncoder(out, Rational.R(25, 1));
        } catch (Exception e) {
            log.warn("Error setting up the output", e);
        }
    }

    @Override
    public void display() {
        if (framesToSkip <= 0 || imageCounter % framesToSkip == 0) {
            try {
                BufferedImage image = new BufferedImage(OutputConstants.HD_WIDTH, OutputConstants.HD_HEIGHT, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();
                simulation.visualise(g2d);
                encoder.encodeImage(image);
            } catch (IOException e) {
                log.error("Error writing image to file", e);
            }
        }
        imageCounter++;
    }

    /**
     * Close the current movie file.
     */
    @Override
    public void close() {
        try {
            encoder.finish();
            NIOUtils.closeQuietly(out);
        } catch (IOException e) {
            log.error("Error closing encoder", e);
        }
    }
}
