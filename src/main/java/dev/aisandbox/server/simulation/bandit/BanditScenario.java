package dev.aisandbox.server.simulation.bandit;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.simulation.bandit.model.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
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
    public String getName() {
        return "Bandit";
    }

    @Override
    public int getMinPlayerCount() {
        return 1;
    }

    @Override
    public int getMaxPlayerCount() {
        return 1;
    }

    @Override
    public String[] getPlayerNames(int playerCount) {
        return new String[]{"Player 1"};
    }

    @Override
    public Simulation build(List<Player> players, Theme theme) {
        return new BanditRuntime(players.getFirst(), new Random(), banditCount.getNumber(), banditPulls.getNumber(), banditNormal, banditStd, banditUpdate,theme);
    }

}
