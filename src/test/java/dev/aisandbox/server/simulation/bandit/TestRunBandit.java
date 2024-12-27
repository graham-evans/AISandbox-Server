package dev.aisandbox.server.simulation.bandit;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.BitmapOutputRenderer;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.simulation.bandit.model.BanditUpdateEnumeration;
import dev.aisandbox.server.simulation.coingame.CoinGameBuilder;
import dev.aisandbox.server.simulation.coingame.MockPlayer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TestRunBandit {

    @Test
    public void testRunBanditGame() {
        assertDoesNotThrow(() -> {
            // create simulation
            BanditScenario banditBuilder = new BanditScenario();
            banditBuilder.setBanditUpdate(BanditUpdateEnumeration.EQUALISE);
            // create players
            List<Player> players = Arrays.stream(banditBuilder.getPlayerNames(1)).map(s -> (Player) new MockBanditPlayer(s)).toList();
            // create simulation
            Simulation sim = banditBuilder.build(players, Theme.DEFAULT);
            // create output directory
            File outputDirectory = new File("build/test/bandit");
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
