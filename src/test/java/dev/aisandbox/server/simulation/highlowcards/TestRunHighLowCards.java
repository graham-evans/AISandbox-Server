package dev.aisandbox.server.simulation.highlowcards;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.output.BitmapOutputRenderer;
import dev.aisandbox.server.engine.output.OutputRenderer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TestRunHighLowCards {

    @Test
    public void testRunHighLowCards() {
        assertDoesNotThrow(() -> {
            // create simulation
            SimulationBuilder simulationBuilder = new HighLowCardsBuilder();
            // create players
            List<Player> players = List.of(new MockPlayer());
            // create simulation
            Simulation sim = simulationBuilder.build(players);
            // create output directory
            File outputDirectory = new File("build/test/highLowCards");
            outputDirectory.mkdirs();
            // create output
            OutputRenderer out = new BitmapOutputRenderer(sim);
            out.setSkipFrames(100);
            out.setOutputDirectory(outputDirectory);
            out.setup();
            // start simulation
            for (int step = 0; step < 1000; step++) {
                sim.step(out);
            }
            // finish simulation
            sim.close();
            players.forEach(Player::close);
        });
    }
}
