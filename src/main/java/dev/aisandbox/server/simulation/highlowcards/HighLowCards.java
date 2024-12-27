package dev.aisandbox.server.simulation.highlowcards;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HighLowCards implements SimulationInfo {

    @Override
    public String getName() {
        return "HighLowCards";
    }

    @Override
    public int getPlayerCount() {
        return 1;
    }

    @Override
    public Simulation createSimulation(List<Player> players) {
        return new HighLowCardsSimulation(players.getFirst(),9);
    }
}
