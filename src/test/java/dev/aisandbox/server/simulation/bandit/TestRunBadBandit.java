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
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the Bandit simulation - testing bad responses are dealt with.
 */
public class TestRunBadBandit {

    /**
     * Test that sending an invalid arm request (too high number) throws an exception.
     */
    @Test
    public void testRunBadHighBanditGame() {
        // Verify that attempting to run the simulation with a bad player implementation raises an IllegalActionException.
        assertThrows(IllegalActionException.class, () -> {
            // Create a new Bandit scenario builder.
            BanditScenario banditBuilder = new BanditScenario();

            // Set the update strategy for the bandit to EQUALISE.
            banditBuilder.setBanditUpdate(BanditUpdateEnumeration.EQUALISE);

            // Create a list of players with bad implementations.
            List<Agent> agents = Arrays.stream(banditBuilder.getAgentNames(1))
                    .map(s -> (Agent) new BadBanditPlayer(s, true)) // ask for an arm which doesn't exist.
                    .toList();

            // Build the simulation using the bad player implementations.
            Simulation sim = banditBuilder.build(agents, Theme.LIGHT, new Random());

            // Set up a null output renderer for the simulation.
            OutputRenderer out = new NullOutputRenderer();
            out.setup(sim);

            // Attempt to run the simulation 100 steps.
            for (int step = 0; step < 100; step++) {
                sim.step(out);
            }

            // Close the simulation and its agents.
            sim.close();
            agents.forEach(Agent::close);
        });
    }


    /**
     * Test that sending a invalid arm request (minus number) throws an exception.
     */
    @Test
    public void testRunBadLowBanditGame() {
        // Verify that attempting to run the simulation with a bad player implementation raises an IllegalActionException.
        assertThrows(IllegalActionException.class, () -> {
            // Create a new Bandit scenario builder.
            BanditScenario banditBuilder = new BanditScenario();

            // Set the update strategy for the bandit to EQUALISE.
            banditBuilder.setBanditUpdate(BanditUpdateEnumeration.EQUALISE);

            // Create a list of players with bad implementations.
            List<Agent> agents = Arrays.stream(banditBuilder.getAgentNames(1))
                    .map(s -> (Agent) new BadBanditPlayer(s, false)) // negative number strategy
                    .toList();

            // Build the simulation using the bad player implementations.
            Simulation sim = banditBuilder.build(agents, Theme.LIGHT, new Random());

            // Set up a null output renderer for the simulation.
            OutputRenderer out = new NullOutputRenderer();
            out.setup(sim);

            // Attempt to run the simulation 100 steps.
            for (int step = 0; step < 100; step++) {
                sim.step(out);
            }

            // Close the simulation and its agents.
            sim.close();
            agents.forEach(Agent::close);
        });
    }

}
