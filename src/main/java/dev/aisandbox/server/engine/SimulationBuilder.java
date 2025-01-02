package dev.aisandbox.server.engine;

import java.util.List;

public interface SimulationBuilder {

    String getName();
    String getDescription();

    int getMinPlayerCount();

    int getMaxPlayerCount();

    String[] getPlayerNames(int playerCount);

    Simulation build(List<Player> players, Theme theme);
}
