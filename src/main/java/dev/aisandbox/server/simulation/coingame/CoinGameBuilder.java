package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Setter
@Getter
@Component
public class CoinGameBuilder implements SimulationBuilder {

    private int startingCoins = 14;

    @Override
    public String getName() {
        return "CoinGame";
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
    public String[] getPlayerNames(int playerCount) {
        return new String[] {"Player 1", "Player 2"};
    }

    @Override
    public Simulation build(List<Player> players) {
        return new CoinGame(players);
    }
}
