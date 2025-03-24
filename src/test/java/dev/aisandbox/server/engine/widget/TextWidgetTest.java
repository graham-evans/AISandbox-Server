package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static dev.aisandbox.server.engine.output.OutputConstants.LOG_FONT;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextWidgetTest {

    private static final File outputDir = new File("build/test/widgets/text");

    @BeforeAll
    public static void setupDir() {
        outputDir.mkdirs();
    }

    @Test
    public void defaultValueTest() {
        TextWidget text = TextWidget.builder().build();
        assertEquals(Theme.LIGHT, text.getTheme());
    }

    @Test
    public void sizeTest() {
        TextWidget text = TextWidget.builder().width(500).height(400).build();
        BufferedImage image = text.getImage();
        assertEquals(500, image.getWidth());
        assertEquals(400, image.getHeight());
    }

    @Test
    public void wrapTest() throws IOException {
        TextWidget text = TextWidget.builder().width(500).height(400).font(LOG_FONT)
                .build();
        text.addText("Line 1");
        text.addText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus mollis mauris at ipsum fringilla, at lacinia felis pretium. Phasellus placerat neque at aliquet mattis. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. In eleifend arcu eu velit condimentum ornare. Cras mauris dolor, accumsan sit amet dapibus eget, vulputate et metus. Proin convallis tristique placerat. Integer id lorem auctor, faucibus sapien vel, tincidunt elit.");
        text.addText("Line 3");
        BufferedImage image = text.getImage();
        ImageIO.write(image, "png", new File(outputDir, "wrap.png"));
    }

}
