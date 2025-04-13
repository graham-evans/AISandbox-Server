package dev.aisandbox.server.engine;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

public class ThemeTest {

  static String splitCamelCase(String s) {
    return s.replaceAll(
        String.format("%s|%s|%s",
            "(?<=[A-Z])(?=[A-Z][a-z])",
            "(?<=[^A-Z])(?=[A-Z])",
            "(?<=[A-Za-z])(?=[^A-Za-z])"
        ),
        " "
    );
  }

  @Test
  public void printThemes()
      throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    File outputFile = new File("build/test/theme/index.html");
    outputFile.getParentFile().mkdirs();
    PrintWriter out = new PrintWriter(new FileWriter(outputFile));
    out.println("<html><head></head><body>");
    out.println("<h1>Theme Colours</h1>");
    out.println("<table><thead><tr><th>Theme</th>");
    for (Field field : Theme.class.getDeclaredFields()) {
      if (field.getType() == Color.class) {
        out.print("<th>");
        out.print(splitCamelCase(field.getName()).toLowerCase());
        out.println("</th>");
      }
    }
    out.println("</tr></thead><tbody>");
    for (Theme theme : Theme.values()) {
      out.println("<tr>");
      out.println("<td>" + theme.name() + "</td>");
      for (Field field : Theme.class.getDeclaredFields()) {
        if (field.getType() == Color.class) {
          Method m = Theme.class.getMethod(
              "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));
          Color c = (Color) m.invoke(theme);
          printColourBlock(out, c);
        }
      }
      out.println("</tr>");
    }
    out.println("</tbody></table>");
    out.println("</body></html>");
    out.close();
  }

  private void printColourBlock(PrintWriter out, Color color) {
    out.println(
        "<td style='width:120px;background:" + String.format("#%02X%02X%02X", color.getRed(),
            color.getGreen(), color.getBlue()) + ";'>&nbsp;</td>");
  }

}
