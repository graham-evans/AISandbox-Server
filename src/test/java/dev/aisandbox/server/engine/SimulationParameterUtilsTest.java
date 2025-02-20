package dev.aisandbox.server.engine;

import dev.aisandbox.server.options.RuntimeUtils;
import dev.aisandbox.server.simulation.SimulationEnumeration;
import dev.aisandbox.server.simulation.coingame.CoinGameBuilder;
import dev.aisandbox.server.simulation.highlowcards.HighLowCardsBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class SimulationParameterUtilsTest {

    /**
     * Test parameters on HighLowCards (0 parameters).
     */
    @Test
    void getHighLowCardParameters() {
        HighLowCardsBuilder builder = new HighLowCardsBuilder();
        Map<String, String> params = builder.getParameters();
        assertEquals(0, params.size());
    }

    @Test
    void getCoinGameParameters() {
        CoinGameBuilder builder = new CoinGameBuilder();
        Map<String, String> params = builder.getParameters();
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
        Map<String, String> prams = builder.getParameters();
        for (Map.Entry<String, String> entry : prams.entrySet()) {
            String key = entry.getKey();
            String description = entry.getValue();
            log.info(" Parameter {} = {}", key, description);
            // get the current value of this parameter
            log.info("  Default value = {}", RuntimeUtils.getParameterValue(builder, key));
            // get the type of this parameter
            Class<?> paramClass = RuntimeUtils.getParameterClass(builder, key);
            if (paramClass.isEnum()) {
                // try and call enum setter
                RuntimeUtils.setParameterValue(builder, key, paramClass.getEnumConstants()[0].toString());
            } else {
                log.info(" dont know how to test {}", paramClass.getName());
            }

        }
    }

    @Test
    void testBadSimulationParameters() {
        assertThrows(IllegalArgumentException.class, () -> {
            SimulationBuilder builder = new BadSimulation();
            Map<String, String> params = builder.getParameters();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String description = entry.getValue();
                log.info(" Parameter {} = {}", key, description);
                // get the current value of this parameter
                log.info("  Default value = {}", RuntimeUtils.getParameterValue(builder, key));
            }
        });
    }

}