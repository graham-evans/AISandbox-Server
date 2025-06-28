/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.options;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.aisandbox.launcher.options.RuntimeOptions;
import dev.aisandbox.launcher.options.RuntimeUtils;
import org.junit.jupiter.api.Test;

public class CommandLineOptionsTests {

  @Test
  public void testRunHighLowCards() {
    RuntimeOptions options = RuntimeUtils.parseCommandLine("-s HighLowCards --png".split(" "));
    assertFalse(options.help(), "Help not requested");
    assertEquals("HighLowCards", options.simulation(), "Simulation name not correct");
    assertTrue(options.outputImage(), "Output image not correct");
  }

  @Test
  public void testSetParameters() {
    RuntimeOptions options = RuntimeUtils.parseCommandLine(
        "-s HighLowCards -p echo:true -a=3".split(" "));
    assertEquals("HighLowCards", options.simulation(), "Simulation name not correct");
    assertEquals(3, options.agents(), "Number of agents not correct");
    assertNotNull(options.parameters(), "Parameters not correct");
    assertEquals(1, options.parameters().size(), "Number of parameters not correct");
    assertTrue(options.parameters().contains("echo:true"), "Echo parameter not correct");
  }

}
