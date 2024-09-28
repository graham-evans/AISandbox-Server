package dev.aisandbox.server.engine;

import java.util.List;

public interface SimulationInfo {

    String getName();

    int getPlayerCount();

    Simulation createSimulation(List<Player> players);
}
