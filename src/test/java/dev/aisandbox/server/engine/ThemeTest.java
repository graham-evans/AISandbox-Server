package dev.aisandbox.server.engine;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ThemeTest {

    @Test
    public void printThemes() throws IOException {
        File outputFile = new File("build/test/theme/index.html");
        outputFile.getParentFile().mkdirs();
        PrintWriter out = new PrintWriter(new FileWriter(outputFile));
        out.println("<html><head></head><body>");
        out.println("<h1>Theme Colours</h1>");
        out.println("<table><thead><tr><th>Theme</th><th>Background</th><th>Text</th><th>Widget Background</th><th>Graph Background</th><th>Primary Colour</th><th>Secondary Colour</th><th>Graph Outline Colour</th></tr></thead><tbody>");
        for (Theme theme : Theme.values()) {
            out.println("<tr>");
            out.println("<td>"+theme.name()+"</td>");
            printColourBlock(out,theme.getBackground());
            printColourBlock(out,theme.getText());
            printColourBlock(out,theme.getWidgetBackground());
            printColourBlock(out,theme.getGraphBackground());
            printColourBlock(out,theme.getPrimaryColor());
            printColourBlock(out,theme.getSecondaryColor());
            printColourBlock(out,theme.getGraphOutlineColor());
            out.println("</tr>");
        }
        out.println("</tbody></table>");
        out.println("</body></html>");
        out.close();
    }

    private void printColourBlock(PrintWriter out, Color color) {
        out.println("<td style='width:120px;background:"+String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue())  +";'>&nbsp;</td>");

    }

}
