/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

/**
 * Test class for Theme functionality and color handling. Tests theme color definitions and
 * documentation generation.
 */
public class ThemeTest {

  private static final String COLOUR_TEST = """
      <div style='width:640px;height:480px;background:{{base}};position:relative;'>
      <div style='position:absolute;left:10px;top:10px'>
      <img src='{{logo}}'/>
      </div>
      <div style='width:200px;height:200px;background:{{baize}};position:absolute;bottom:10px;left:10px;outline:1px solid {{baizeBorder}};'>
      <p style='color:{{text}}'>Baize</p>
      </div>
      <div style='width:200px;height:200px;background:{{background}};position:absolute;bottom:10px;left:220px;outline:1px solid {{border}};'>
      <p style='color:{{text}}'>Text Widget</p>
      </div>
      </div>
      """;

  @Test
  public void printThemes()
      throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    File outputFile = new File("build/test/theme/index.html");
    outputFile.getParentFile().mkdirs();
    PrintWriter out = new PrintWriter(new FileWriter(outputFile));
    out.println("<html><head></head><body>");
    out.println("<h1>Theme Colours</h1>");
    Template tmpl = Mustache.compiler().compile(COLOUR_TEST);
    for (Theme theme : Theme.values()) {
      out.print("<h2>");
      out.print(theme.name());
      out.println("</h2>");
      // create colour map
      Map<String, String> templateMap = new TreeMap<>();
      for (Field field : Theme.class.getDeclaredFields()) {
        if (field.getType() == Color.class) {
          Method m = Theme.class.getMethod(
              "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));
          Color c = (Color) m.invoke(theme);
          templateMap.put(field.getName(),
              String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()));
        }
      }
      templateMap.put("logo", "../../../src/main/resources/images/AILogo.png");
      // draw diagram
      out.println(tmpl.execute(templateMap));
      // draw table
      out.println("<table><thead><tr><th>Name</th><th>Color</th><th>Hex</th></thead><tbody>");
      for (Entry<String, String> entry : templateMap.entrySet()) {
        String colourName = entry.getKey();
        String colourHex = entry.getValue();
        out.println("<tr><td>");
        out.println(splitCamelCase(colourName).toLowerCase());
        out.println("</td><td>");
        out.println("<td style='width:120px;background:" + colourHex + ";'>&nbsp;</td>");
        out.println("<td>");
        out.println(colourHex);
        out.println("</td></tr>");
      }
      out.println("</tbody></table>");
    }
    out.println("</body></html>");
    out.close();
  }

  static String splitCamelCase(String s) {
    return s.replaceAll(
        String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
            "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
  }


}
