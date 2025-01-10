package dev.aisandbox.server.engine;

import dev.aisandbox.server.engine.output.OutputRenderer;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class SimulationSetup {

    public static SimulationRunner setupSimulation(SimulationBuilder builder,int agentCount,int defaultPort,OutputRenderer renderer) {
        AtomicInteger port = new AtomicInteger(defaultPort);
        List<Player> agents = Arrays.stream(builder.getAgentNames(agentCount))
                .map(s -> (Player) new NetworkPlayer(s, port.getAndIncrement())).toList();
        return setupSimulation(builder,agents,renderer);
    }

    public static SimulationRunner setupSimulation(SimulationBuilder builder, List<Player> agents, OutputRenderer renderer) {
        // create simulation
        Simulation sim = builder.build(agents, Theme.DEFAULT);
        // start output
        renderer.setup(sim);
        // create simulation thread
        return new SimulationRunner(sim,renderer,agents);
    }

}
