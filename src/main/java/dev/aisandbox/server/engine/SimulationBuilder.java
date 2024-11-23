package dev.aisandbox.server.engine;

import java.util.List;

public interface SimulationBuilder {

    String getName();

    int getMinPlayerCount();

    int getMaxPlayerCount();

    String[] getPlayerNames(int playerCount);

    Simulation build(List<Player> players, Theme theme);
}
