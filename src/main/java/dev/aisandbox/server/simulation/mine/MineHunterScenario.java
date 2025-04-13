package dev.aisandbox.server.simulation.mine;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.SimulationParameter;
import dev.aisandbox.server.engine.Theme;
import java.util.List;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;

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
    return List.of(
        new SimulationParameter("mineSize", "The size of the minefield", MineSize.class));
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
  public String[] getAgentNames(int agentCount) {
    return new String[]{"Agent 1"};
  }

  @Override
  public Simulation build(List<Agent> agents, Theme theme, Random random) {
    return new MineHunterRuntime(agents.getFirst(), mineSize, theme, random);
  }

}
