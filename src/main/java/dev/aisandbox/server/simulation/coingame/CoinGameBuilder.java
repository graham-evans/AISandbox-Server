package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.Theme;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;


public class CoinGameBuilder implements SimulationBuilder {

    @Getter
    @Setter
    private CoinScenario scenario = CoinScenario.SINGLE_21_2;

    @Override
    public String getSimulationName() {
        return "CoinGame";
    }

    @Override
    public String getDescription() {
        return "Various versions of the 'Coin Game' where agents take turns to remove coins from one or more piles. The loser is the one who it forced to take the last coin.";
    }

    @Override
    public int getMinAgentCount() {
        return 2;
    }

    @Override
    public int getMaxAgentCount() {
        return 2;
    }

    @Override
    public Map<String, String> getParameters() {
        return Map.of("scenario","The scenario to run");
    }

    @Override
    public String[] getAgentNames(int playerCount) {
        return new String[]{"Player 1", "Player 2"};
    }

    @Override
    public Simulation build(List<Agent> agents, Theme theme) {
        return new CoinGame(agents, scenario, theme);
    }


}
