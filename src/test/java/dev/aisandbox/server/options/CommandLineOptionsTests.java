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

import dev.aisandbox.launcher.options.RuntimeUtils;
import dev.aisandbox.server.engine.setup.SimulationSettings;
import dev.aisandbox.server.simulation.coingame.CoinGameBuilder;
import dev.aisandbox.server.simulation.coingame.CoinScenario;
import org.junit.jupiter.api.Test;

/** Tests for command-line option parsing. */
public class CommandLineOptionsTests {

  @Test
  public void testRunHighLowCards() {
    SimulationSettings settings =
            RuntimeUtils.parseCommandLine("-s HighLowCards --png".split(" "));
    assertEquals("HighLowCards", settings.selectedSimulationBuilder().get().getSimulationName(), "Simulation " +
            "name not correct");
    assertTrue(settings.outputPNG().get(), "Output image not correct");
  }

  @Test
  public void testSetParameters() {
    SimulationSettings settings= RuntimeUtils.parseCommandLine(
        "-s CoinGame -p scenario:nim -a=3".split(" "));
    assertEquals("CoinGame", settings.selectedSimulationBuilder().get().getSimulationName(), "Simulation name not " +
            "correct");
    assertEquals(3, settings.agentCount().get(), "Number of agents not correct");
    assertEquals(CoinScenario.NIM,((CoinGameBuilder)settings.selectedSimulationBuilder().get()).getScenario(),
                "Parameter not passed to simulation builder");
  }

  @Test
  public void testSetParameters2() {
    SimulationSettings settings= RuntimeUtils.parseCommandLine(
            "-s CoinGame -p scenario:double_21_2 -a=3".split(" "));
    assertEquals("CoinGame", settings.selectedSimulationBuilder().get().getSimulationName(), "Simulation name not " +
            "correct");
    assertEquals(3, settings.agentCount().get(), "Number of agents not correct");
    assertEquals(CoinScenario.DOUBLE_21_2,((CoinGameBuilder)settings.selectedSimulationBuilder().get()).getScenario(),
            "Parameter not passed to simulation builder");
  }
}