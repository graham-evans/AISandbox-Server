package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CoinGameBuilder implements SimulationBuilder {

    @Override
    public String getName() {
        return "Coin Game Simulation";
    }

    @Override
    public int getMinPlayerCount() {
        return 2;
    }

    @Override
    public int getMaxPlayerCount() {
        return 2;
    }

    @Override
    public String[] getPlayerNames() {
        return new String[] {"Player 1", "Player 2"};
    }

    @Override
    public Simulation build(List<Player> players) {
        return new CoinGame(players);
    }
}
