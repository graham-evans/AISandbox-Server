package dev.aisandbox.server.engine;

import java.util.List;
import java.util.Random;

public class BadSimulation implements SimulationBuilder {

    @Override
    public String getSimulationName() {
        return "Bad Simulation";
    }

    @Override
    public String getDescription() {
        return "Simulation to negatively test parameters";
    }

    @Override
    public List<SimulationParameter> getParameters() {
        return List.of(new SimulationParameter("height", "Non existent parameter", Integer.class));
    }

    @Override
    public int getMinAgentCount() {
        return 0;
    }

    @Override
    public int getMaxAgentCount() {
        return 0;
    }

    @Override
    public String[] getAgentNames(int agentCount) {
        return new String[0];
    }

    @Override
    public Simulation build(List<Agent> agents, Theme theme, Random random) {
        return null;
    }
}
