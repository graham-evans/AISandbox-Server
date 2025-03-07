package dev.aisandbox.server.simulation.bandit;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.exception.IllegalActionException;
import dev.aisandbox.server.engine.output.NullOutputRenderer;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.simulation.bandit.model.BanditUpdateEnumeration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestRunBadBandit {
    @Test
    public void testRunBadHighBanditGame() {
        assertThrows(IllegalActionException.class, () -> {
            // create simulation
            BanditScenario banditBuilder = new BanditScenario();
            banditBuilder.setBanditUpdate(BanditUpdateEnumeration.EQUALISE);
            // create players
            List<Agent> agents = Arrays.stream(banditBuilder.getAgentNames(1)).map(s -> (Agent) new BadBanditPlayer(s, true)).toList();
            // create simulation
            Simulation sim = banditBuilder.build(agents, Theme.DEFAULT);
            // create output
            OutputRenderer out = new NullOutputRenderer();
            out.setup(sim);
            // start simulation
            for (int step = 0; step < 100; step++) {
                sim.step(out);
            }
            // finish simulation
            sim.close();
            agents.forEach(Agent::close);
        });
    }

    @Test
    public void testRunBadLowBanditGame() {
        assertThrows(IllegalActionException.class, () -> {
            // create simulation
            BanditScenario banditBuilder = new BanditScenario();
            banditBuilder.setBanditUpdate(BanditUpdateEnumeration.EQUALISE);
            // create players
            List<Agent> agents = Arrays.stream(banditBuilder.getAgentNames(1)).map(s -> (Agent) new BadBanditPlayer(s, false)).toList();
            // create simulation
            Simulation sim = banditBuilder.build(agents, Theme.DEFAULT);
            // create output
            OutputRenderer out = new NullOutputRenderer();
            out.setup(sim);
            // start simulation
            for (int step = 0; step < 100; step++) {
                sim.step(out);
            }
            // finish simulation
            sim.close();
            agents.forEach(Agent::close);
        });
    }

}
