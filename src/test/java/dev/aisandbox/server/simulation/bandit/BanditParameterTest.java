package dev.aisandbox.server.simulation.bandit;

import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.options.ParameterEnumInfo;
import dev.aisandbox.server.options.RuntimeUtils;
import dev.aisandbox.server.simulation.bandit.model.BanditPullEnumeration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BanditParameterTest {

    @Test
    public void testBanditParameterCount() {
        SimulationBuilder simBuilder = new BanditScenario();
        List<ParameterEnumInfo> params = RuntimeUtils.listEnumParameters(simBuilder);
        assertEquals(5, params.size());
    }

    @Test
    public void assignParameterTest() throws Exception {
        BanditScenario simBuilder = new BanditScenario();
        RuntimeUtils.setEnumParameter(simBuilder, "banditPulls", "two_THOUSANd");
        assertEquals(BanditPullEnumeration.TWO_THOUSAND, simBuilder.getBanditPulls());
    }

    @Test
    public void assignInvalidParameterTest() throws Exception {
        BanditScenario simBuilder = new BanditScenario();
        RuntimeUtils.setEnumParameter(simBuilder, "banditPulls", "xxxxxx");
        assertEquals(BanditPullEnumeration.ONE_HUNDRED, simBuilder.getBanditPulls());
    }
}
