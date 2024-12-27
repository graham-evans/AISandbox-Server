package dev.aisandbox.server.engine;

import dev.aisandbox.server.simulation.coingame.CoinGameBuilder;
import dev.aisandbox.server.simulation.highlowcards.HighLowCardsBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationParameterUtilsTest {

    /**
     * Test parameters on HighLowCards (0 parameters).
     */
    @Test
    void getHighLowCardParameters() {
        HighLowCardsBuilder builder = new HighLowCardsBuilder();
        List<SimulationParameter> parameterList = SimulationParameterUtils.getParameters(builder);
        assertEquals(0, parameterList.size());
    }

    @Test
    void getCoinGameParameters() {
        CoinGameBuilder builder = new CoinGameBuilder();
        List<SimulationParameter> parameterList = SimulationParameterUtils.getParameters(builder);
        assertEquals(1, parameterList.size(),"Number of parameters should be 1");
        assertEquals(SimulationParameter.ParameterType.ENUM,parameterList.getFirst().type(),"Parameter type should be ENUM");
        assertEquals("Scenario",parameterList.getFirst().parameterName(),"Parameter name should be 'scenario'");
        assertEquals("SINGLE_21_2",parameterList.getFirst().defaultValue(),"Parameter default value should be 'SINGLE_21_2'");
    }
}