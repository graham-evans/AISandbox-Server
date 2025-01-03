package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.Theme;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CoinGameBuilder implements SimulationBuilder {

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
    public String[] getAgentNames(int playerCount) {
        return new String[]{"Player 1", "Player 2"};
    }

    @Getter
    @Setter
    private CoinScenario scenario = CoinScenario.SINGLE_21_2;

    @Override
    public Simulation build(List<Player> players, Theme theme) {
        return new CoinGame(players,scenario, theme);
    }



}
