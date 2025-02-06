package dev.aisandbox.server.engine.widget;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HistogramWidgetTest {

    private static final File outputDir = new File("build/test/widgets/histogram");

    @BeforeAll
    public static void setupDir() {
        outputDir.mkdirs();
    }

    @Test
    public void noDataTest() throws IOException {
        RollingValueHistogramWidget widget = RollingValueHistogramWidget.builder().height(300).width(400).window(300).binCount(6).build();
        // dont add any data
        BufferedImage image = widget.getImage();
        // write to test directory
        ImageIO.write(image, "png",new File(outputDir,"empty.png"));
        // check contents
        assertEquals(300,image.getHeight(),"Image height");
        assertEquals(400,image.getWidth(),"Image width");
    }

    @Test
    public void oneDataTest() throws IOException {
        RollingValueHistogramWidget widget = RollingValueHistogramWidget.builder().height(300).width(400).window(300).binCount(6).build();
        // add single datapoint
        widget.addValue(10.5);
        // get image
        BufferedImage image = widget.getImage();
        // write to test directory
        ImageIO.write(image, "png",new File(outputDir,"one.png"));
        // check contents
        assertEquals(300,image.getHeight(),"Image height");
        assertEquals(400,image.getWidth(),"Image width");
    }

    @Test
    public void tenDataTest() throws IOException {
        RollingValueHistogramWidget widget = RollingValueHistogramWidget.builder().height(300).width(400).window(300).binCount(6).build();
        // add ten datapoint
        for (int i = 0; i < 10; i++) {
            widget.addValue(i);
        }
        // get image
        BufferedImage image = widget.getImage();
        // write to test directory
        ImageIO.write(image, "png",new File(outputDir,"ten.png"));
        // check contents
        assertEquals(300,image.getHeight(),"Image height");
        assertEquals(400,image.getWidth(),"Image width");
    }

}
