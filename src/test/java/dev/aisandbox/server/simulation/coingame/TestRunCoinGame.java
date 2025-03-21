package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.BitmapOutputRenderer;
import dev.aisandbox.server.engine.output.OutputRenderer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TestRunCoinGame {

    @Test
    public void testRunCoinGame() {
        assertDoesNotThrow(() -> {
            // create simulation
            CoinGameBuilder simulationBuilder = new CoinGameBuilder();
            simulationBuilder.setScenario(CoinScenario.NIM);
            // create players
            List<Agent> agents = Arrays.stream(simulationBuilder.getAgentNames(2)).map(s -> (Agent) new MockPlayer(s)).toList();
            // create simulation
            Simulation sim = simulationBuilder.build(agents, Theme.LIGHT, new Random());
            // create output directory
            File outputDirectory = new File("build/test/coingame");
            outputDirectory.mkdirs();
            // create output
            OutputRenderer out = new BitmapOutputRenderer();
            out.setOutputDirectory(outputDirectory);
            out.setup(sim);
            out.setSkipFrames(100);
            // start simulation
            for (int step = 0; step < 1200; step++) {
                sim.step(out);
            }
            // finish simulation
            sim.close();
            agents.forEach(Agent::close);
        });
    }
}
