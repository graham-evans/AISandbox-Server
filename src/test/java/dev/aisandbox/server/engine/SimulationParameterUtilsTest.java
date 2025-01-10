package dev.aisandbox.server.engine;

import dev.aisandbox.server.options.ParameterEnumInfo;
import dev.aisandbox.server.options.RuntimeUtils;
import dev.aisandbox.server.simulation.coingame.CoinGameBuilder;
import dev.aisandbox.server.simulation.coingame.CoinScenario;
import dev.aisandbox.server.simulation.highlowcards.HighLowCardsBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class SimulationParameterUtilsTest {

    /**
     * Test parameters on HighLowCards (0 parameters).
     */
    @Test
    void getHighLowCardParameters() {
        HighLowCardsBuilder builder = new HighLowCardsBuilder();
        List<ParameterEnumInfo> params = RuntimeUtils.listEnumParameters(builder);
        assertEquals(0, params.size());
    }

    @Test
    void getCoinGameParameters() {
        CoinGameBuilder builder = new CoinGameBuilder();
        List<ParameterEnumInfo> params = RuntimeUtils.listEnumParameters(builder);
        assertEquals(1, params.size());
    }

    @Test
    void setPropertyTest() throws InvocationTargetException, IllegalAccessException {
        CoinGameBuilder builder = new CoinGameBuilder();

        List<ParameterEnumInfo> params = RuntimeUtils.listEnumParameters(builder);

        log.info("Found {} parameters {}", params.size(), params);

        assertEquals(1, params.size(), "Number of parameters should be 1");

        assertEquals("scenario", params.getFirst().parameterName(), "First parameter should be 'scenario'");

        RuntimeUtils.setEnumParameter(builder, "scenario", "nim");

        assertEquals(CoinScenario.NIM, builder.getScenario(), "Updating scenario didn't work");
    }
}