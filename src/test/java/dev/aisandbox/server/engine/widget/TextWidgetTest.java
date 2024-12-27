package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextWidgetTest {
    @Test
    public void defaultValueTest() {
        TextWidget text = TextWidget.builder().build();
        assertEquals(Theme.DEFAULT,text.getTheme());
    }
}
