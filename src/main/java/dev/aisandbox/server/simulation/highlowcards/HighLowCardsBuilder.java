package dev.aisandbox.server.simulation.highlowcards;

import dev.aisandbox.server.engine.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HighLowCardsBuilder implements SimulationBuilder {

    @Override
    public String getName() {
        return "HighLowCards";
    }

    @Override
    public int getMinPlayerCount() {
        return 1;
    }

    @Override
    public int getMaxPlayerCount() {
        return 1;
    }

    @Override
    public String[] getPlayerNames(int playerCount) {
        return new String[]{"Player 1"};
    }

    @Override
    public Simulation build(List<Player> players, Theme theme) {
        return new HighLowCards(players.getFirst(), 9, theme);
    }
}
