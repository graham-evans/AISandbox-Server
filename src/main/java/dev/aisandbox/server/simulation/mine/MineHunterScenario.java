package dev.aisandbox.server.simulation.mine;

import dev.aisandbox.server.engine.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    public List<SimulationParameter> getParameters() {
        return List.of(new SimulationParameter("mineSize", "The size of the minefield", MineSize.class));
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
        return new MineHunterRuntime(agents.getFirst(), mineSize, theme);
    }

}
