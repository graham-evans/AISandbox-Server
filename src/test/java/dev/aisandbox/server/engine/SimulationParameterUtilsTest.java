package dev.aisandbox.server.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.aisandbox.server.options.RuntimeUtils;
import dev.aisandbox.server.simulation.SimulationEnumeration;
import dev.aisandbox.server.simulation.coingame.CoinGameBuilder;
import dev.aisandbox.server.simulation.highlowcards.HighLowCardsBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Slf4j
class SimulationParameterUtilsTest {

  /**
   * Test parameters on HighLowCards (0 parameters).
   */
  @Test
  void getHighLowCardParameters() {
    HighLowCardsBuilder builder = new HighLowCardsBuilder();
    List<SimulationParameter> params = builder.getParameters();
    assertEquals(0, params.size());
  }

  @Test
  void getCoinGameParameters() {
    CoinGameBuilder builder = new CoinGameBuilder();
    List<SimulationParameter> params = builder.getParameters();
    assertEquals(1, params.size());
  }

  /**
   * Test that all parameters in each simulation work
   *
   * @param simulation
   */
  @ParameterizedTest
  @EnumSource(SimulationEnumeration.class)
  void testSimulationParameters(SimulationEnumeration simulation) {
    SimulationBuilder builder = simulation.getBuilder();
    log.info("Testing {} simulation", builder.getSimulationName());
    // get all parameters
    List<SimulationParameter> params = builder.getParameters();
    for (SimulationParameter entry : params) {
      log.info("Testing parameter {} of simulation {}", entry.name(), simulation.name());
      assertNotNull(entry.name(), "Parameter name is null");
      assertFalse(entry.name().isBlank(), "Name should not be blank");
      assertNotNull(entry.description(), "Description is null");
      assertFalse(entry.description().isBlank(), "Description should not be blank");
      assertNotNull(RuntimeUtils.getParameterValue(builder, entry), "default value is null");
      // check boolean can be set
      if (entry.parameterType() == Boolean.class) {
        RuntimeUtils.setParameterValue(builder, entry, "false");
        assertTrue(RuntimeUtils.getParameterValue(builder, entry).equalsIgnoreCase("false"),
            "Setting value to false failed");
        RuntimeUtils.setParameterValue(builder, entry, "true");
        assertTrue(RuntimeUtils.getParameterValue(builder, entry).equalsIgnoreCase("true"),
            "Setting value to true failed");
      }
      if (entry.parameterType().isEnum()) {
        // check enums have different toString() results
        Set<String> strings = new HashSet<>();
        for (Object o : entry.parameterType().getEnumConstants()) {
          strings.add(o.toString());
        }
        assertEquals(entry.parameterType().getEnumConstants().length, strings.size(),
            "Duplicate enum toString() results");
      }

    }
  }

  @Test
  void testBadSimulationParameters() {
    SimulationBuilder builder = new BadSimulation();
    List<SimulationParameter> params = builder.getParameters();
    for (SimulationParameter entry : params) {
      assertNull(RuntimeUtils.getParameterValue(builder, entry));
    }
  }

}