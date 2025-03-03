package dev.aisandbox.server.simulation.mine;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.Theme;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

public class MineHunterScenario implements SimulationBuilder {

    @Getter
    @Setter
    MineSize mineSize = MineSize.MEDIUM;

    @Override
    public String getSimulationName() {
        return "MineHunter";
    }

    @Override
    public String getDescription() {
        return "Find the mines in a grid using deduction. Mine Hunter pits the AI against a minefield! A known quantity of mines have been distributed across a grid of squares and the AI agent must work out where they are. To help, each uncovered square will show how many mines are in the surrounding squares.";
    }

    @Override
    public Map<String, String> getParameters() {
        return Map.of("mineSize", "The size of the minefield");
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
    public String[] getAgentNames(int playerCount) {
        return new String[]{"Agent 1"};
    }

    @Override
    public Simulation build(List<Agent> agents, Theme theme) {
        return new MineHunterRuntime(agents.getFirst(), mineSize,theme);
    }

}
