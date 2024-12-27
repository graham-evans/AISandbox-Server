package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.BitmapOutputRenderer;
import dev.aisandbox.server.engine.output.OutputRenderer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TestRunCoinGame {

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    public void testRunHighLowCards(int playerCount) {
        assertDoesNotThrow(() -> {
            // create simulation
            SimulationBuilder simulationBuilder = new CoinGameBuilder();
            // create players
            List<Player> players = Arrays.stream(simulationBuilder.getPlayerNames(playerCount)).map(s -> (Player) new MockPlayer(s)).toList();
            // create simulation
            Simulation sim = simulationBuilder.build(players, Theme.DEFAULT);
            // create output directory
            File outputDirectory = new File("build/test/coingame/" + playerCount + "/");
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
