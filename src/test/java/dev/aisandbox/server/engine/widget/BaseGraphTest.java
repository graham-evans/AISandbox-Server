package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.widget.axis.NiceAxisScale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseGraphTest {
    private static final File outputDir = new File("build/test/widgets/graph");

    @BeforeAll
    public static void setupDir() {
        outputDir.mkdirs();
    }

    @Test
    public void testTitles() throws IOException {
        BaseGraph graph = new BaseGraph(500, 300, "Main Title", "X Title", "Y Title", Theme.LIGHT, new NiceAxisScale(0, 100), new NiceAxisScale(0, 20));
        graph.addAxisAndTitle();
        BufferedImage image = graph.getImage();
        ImageIO.write(image, "png", new File(outputDir, "title.png"));
        assertEquals(500, image.getWidth());
        assertEquals(300, image.getHeight());

    }
}
