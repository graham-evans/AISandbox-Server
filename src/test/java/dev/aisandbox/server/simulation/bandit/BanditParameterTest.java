package dev.aisandbox.server.simulation.bandit;

import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.SimulationParameter;
import dev.aisandbox.server.options.RuntimeUtils;
import dev.aisandbox.server.simulation.bandit.model.BanditPullEnumeration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BanditParameterTest {

    @Test
    public void testBanditParameterCount() {
        SimulationBuilder simBuilder = new BanditScenario();
        List<SimulationParameter> params = simBuilder.getParameters();
        assertEquals(5, params.size());
    }

    @Test
    public void assignParameterEnumTest() throws Exception {
        BanditScenario simBuilder = new BanditScenario();
        RuntimeUtils.setParameterValue(simBuilder, "banditPulls", "TWO_THOUSAND");
        assertEquals(BanditPullEnumeration.TWO_THOUSAND, simBuilder.getBanditPulls());
    }

    @Test
    public void assignParameterEnumBadCaseTest() throws Exception {
        BanditScenario simBuilder = new BanditScenario();
        RuntimeUtils.setParameterValue(simBuilder, "banditPulls", "two_THOUSANd");
        assertEquals(BanditPullEnumeration.TWO_THOUSAND, simBuilder.getBanditPulls());
    }

    @Test
    public void assignInvalidParameterTest() throws Exception {
        BanditScenario simBuilder = new BanditScenario();
        RuntimeUtils.setParameterValue(simBuilder, "banditPulls", "invalid");
        assertEquals(BanditPullEnumeration.ONE_HUNDRED, simBuilder.getBanditPulls());
    }
}
