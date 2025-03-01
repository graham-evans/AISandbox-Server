package dev.aisandbox.server.simulation.highlowcards;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.Theme;

import java.util.List;
import java.util.Map;

public class HighLowCardsBuilder implements SimulationBuilder {

    @Override
    public String getSimulationName() {
        return "HighLowCards";
    }

    @Override
    public String getDescription() {
        return "Simulation to guess if the next card is 'higher' or 'lower' than the card before. Note: You don't get anything for a pair, not in this game!";
    }

    @Override
    public int getMinAgentCount() {
        return 1;
    }

    @Override
    public int getMaxAgentCount() {
        return 1;
    }

    @Override
    public Map<String, String> getParameters() {
        return Map.of();
    }

    @Override
    public String[] getAgentNames(int playerCount) {
        return new String[]{"Player 1"};
    }

    @Override
    public Simulation build(List<Agent> agents, Theme theme) {
        return new HighLowCards(agents.getFirst(), 9, theme);
    }
}
