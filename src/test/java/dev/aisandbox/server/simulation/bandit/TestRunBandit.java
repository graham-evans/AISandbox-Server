package dev.aisandbox.server.simulation.bandit;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.BitmapOutputRenderer;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.simulation.bandit.model.BanditUpdateEnumeration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TestRunBandit {

    @Test
    public void testRunBanditGame() {
        assertDoesNotThrow(() -> {
            // create simulation
            BanditScenario banditBuilder = new BanditScenario();
            banditBuilder.setBanditUpdate(BanditUpdateEnumeration.EQUALISE);
            // create players
            List<Agent> agents = Arrays.stream(banditBuilder.getAgentNames(1)).map(s -> (Agent) new MockBanditPlayer(s)).toList();
            // create simulation
            Simulation sim = banditBuilder.build(agents, Theme.LIGHT, new Random());
            // create output directory
            File outputDirectory = new File("build/test/bandit");
            outputDirectory.mkdirs();
            // create output
            OutputRenderer out = new BitmapOutputRenderer();
            out.setSkipFrames(100);
            out.setOutputDirectory(outputDirectory);
            out.setup(sim);
            // start simulation
            for (int step = 0; step < 1000; step++) {
                sim.step(out);
            }
            // finish simulation
            sim.close();
            agents.forEach(Agent::close);
        });
    }

}
