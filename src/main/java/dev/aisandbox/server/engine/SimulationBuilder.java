package dev.aisandbox.server.engine;

import java.util.List;

public interface SimulationBuilder {

    String getSimulationName();
    String getDescription();

    int getMinAgentCount();

    int getMaxAgentCount();

    String[] getAgentNames(int playerCount);

    Simulation build(List<Player> players, Theme theme);
}
