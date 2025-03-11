package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


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
    public List<SimulationParameter> getParameters() {
        return List.of(new SimulationParameter("scenario", "The scenario to run", CoinScenario.class));
    }

    @Override
    public String[] getAgentNames(int agentCount) {
        return new String[]{"Player 1", "Player 2"};
    }

    @Override
    public Simulation build(List<Agent> agents, Theme theme) {
        return new CoinGame(agents, scenario, theme);
    }


}
