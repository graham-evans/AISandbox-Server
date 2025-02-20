package dev.aisandbox.server.engine;

import java.util.List;
import java.util.Map;

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
    public Map<String, String> getParameters() {
        return Map.of("height","Non existent parameter");
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
    public String[] getAgentNames(int playerCount) {
        return new String[0];
    }

    @Override
    public Simulation build(List<Agent> agents, Theme theme) {
        return null;
    }
}
