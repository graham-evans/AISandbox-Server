/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
*/
package dev.aisandbox.server.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SimulationVersion {

  private static final String VERSION;

  static {
    String v = "unknown";
    try (InputStream in = SimulationVersion.class.getResourceAsStream("/version.properties")) {
      if (in != null) {
        Properties props = new Properties();
        props.load(in);
        v = props.getProperty("version", "unknown");
      }
    } catch (IOException ignored) {
      // fall through to "unknown"
    }
    VERSION = v;
  }

  public static String get() {
    return VERSION;
  }
}