/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.options;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CommandLineOptionsTests {

  @Test
  public void testRunHighLowCards() {
    RuntimeOptions options = RuntimeUtils.parseCommandLine(
        "-s=HighLowCards --output=screen".split(" "));
    assertEquals(RuntimeOptions.RuntimeCommand.RUN, options.command(), "Run command not correct");
    assertEquals("HighLowCards", options.simulation(), "Simulation name not correct");
    assertEquals(RuntimeOptions.OutputOptions.SCREEN, options.output(), "Output not correct");
  }

  @Test
  public void testListSimulations() {
    RuntimeOptions options = RuntimeUtils.parseCommandLine("--list".split(" "));
    assertEquals(RuntimeOptions.RuntimeCommand.LIST, options.command(), "List command not correct");
    assertNull(options.simulation(), "Simulation name should be null");
  }

  @Test
  public void testListSimulationParameters() {
    RuntimeOptions options = RuntimeUtils.parseCommandLine("--list -s=HighLowCards".split(" "));
    assertEquals(RuntimeOptions.RuntimeCommand.LIST, options.command(), "List command not correct");
    assertEquals("HighLowCards", options.simulation(), "Simulation name not correct");
  }

  @Test
  public void testSetParameters() {
    RuntimeOptions options = RuntimeUtils.parseCommandLine(
        "-s HighLowCards -p echo:true -a=3".split(" "));
    assertEquals(RuntimeOptions.RuntimeCommand.RUN, options.command(), "Run command not correct");
    assertEquals("HighLowCards", options.simulation(), "Simulation name not correct");
    assertEquals(3, options.agents(), "Number of agents not correct");
    assertNotNull(options.parameters(), "Parameters not correct");
    assertEquals(1, options.parameters().size(), "Number of parameters not correct");
    assertTrue(options.parameters().contains("echo:true"), "Echo parameter not correct");
  }

}
