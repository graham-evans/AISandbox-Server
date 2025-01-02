package dev.aisandbox.server.options;

import dev.aisandbox.launcher.SandboxServerCLIApplication;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.simulation.coingame.CoinGameBuilder;
import dev.aisandbox.server.simulation.highlowcards.HighLowCardsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static dev.aisandbox.server.simulation.coingame.CoinScenario.DOUBLE_21_3;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = SandboxServerCLIApplication.class)
public class SimulationOptionReaderTest {

    @Autowired
    private SimulationOptionReader simulationOptionReader;

    @Test
    public void highLowCardsTest() {
        RuntimeOptions options = RuntimeUtils.parseCommandLine("-s HighLowCards".split(" "));
        SimulationBuilder sim = simulationOptionReader.readOptions(options);
        assertNotNull(sim, "builder is null");
        assertEquals(HighLowCardsBuilder.class, sim.getClass(), "Builder is wrong class");
    }

    @Test
    public void coinGameTest() {
        RuntimeOptions options = RuntimeUtils.parseCommandLine("-s CoinGame -p scenario=double_21_3".split(" "));
        SimulationBuilder sim = simulationOptionReader.readOptions(options);
        assertNotNull(sim, "builder is null");
        assertEquals(CoinGameBuilder.class, sim.getClass(), "Builder is wrong class");
        CoinGameBuilder cSim = (CoinGameBuilder) sim;
        assertEquals(DOUBLE_21_3, cSim.getScenario(), "wrong scenario");
    }

}
