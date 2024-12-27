package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CoinGame implements SimulationInfo {

    @Override
    public String getName() {
        return "Coin Game Simulation";
    }

    @Override
    public int getPlayerCount() {
        return 2;
    }

    @Override
    public Simulation createSimulation(List<Player> players) {
        return new CoinGameSimulation(players);
    }
}
