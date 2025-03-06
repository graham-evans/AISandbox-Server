package dev.aisandbox.server.simulation.bandit;

import dev.aisandbox.server.engine.*;
import dev.aisandbox.server.simulation.bandit.model.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

@Slf4j
public class BanditScenario implements SimulationBuilder {

    /**
     * How many pulls in each test
     */
    @Getter
    @Setter
    private BanditPullEnumeration banditPulls = BanditPullEnumeration.ONE_HUNDRED;

    /**
     * How the normals for each bandit are chosen
     */
    @Getter
    @Setter
    private BanditNormalEnumeration banditNormal = BanditNormalEnumeration.NORMAL_0_1;

    /**
     * How the std for each bandit are chosen
     */
    @Getter
    @Setter
    private BanditStdEnumeration banditStd = BanditStdEnumeration.ONE;

    /**
     * How the bandits are updated after each step
     */
    @Getter
    @Setter
    private BanditUpdateEnumeration banditUpdate = BanditUpdateEnumeration.FIXED;

    /**
     * The number of bandits to include
     */
    @Getter
    @Setter
    private BanditCountEnumeration banditCount = BanditCountEnumeration.FIVE;

    @Override
    public String getSimulationName() {
        return "Bandit";
    }

    @Override
    public String getDescription() {
        return "The classic 'Multi-Armed Bandit scenario where an agent needs to learn which 'bandit' returns the best results.";
    }

    @Override
    public List<SimulationParameter> getParameters() {
        return List.of(new SimulationParameter("banditCount", "The number of bandits", BanditCountEnumeration.class),
                new SimulationParameter("banditUpdate", "How bandits change between pulls", BanditUpdateEnumeration.class),
                new SimulationParameter("banditStd", "How the standard deviation for each bandit is chosen", BanditStdEnumeration.class),
                new SimulationParameter("banditNormal", "How the normal (average) for each bandit is chosen", BanditNormalEnumeration.class),
                new SimulationParameter("banditPulls", "The number of bandit 'pulls' in each episode", BanditPullEnumeration.class)
        );
    }

    @Override
    public int getMinAgentCount() {
        return 1;
    }

    @Override
    public int getMaxAgentCount() {
        return 1;
    }

    @Override
    public String[] getAgentNames(int playerCount) {
        return new String[]{"Player 1"};
    }

    @Override
    public Simulation build(List<Agent> agents, Theme theme) {
        return new BanditRuntime(agents.getFirst(), new Random(), banditCount.getNumber(), banditPulls.getNumber(), banditNormal, banditStd, banditUpdate, theme);
    }

}
