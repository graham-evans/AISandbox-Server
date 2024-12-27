package dev.aisandbox.server;

import dev.aisandbox.server.engine.*;
import dev.aisandbox.server.options.RuntimeOptions;
import dev.aisandbox.server.options.RuntimeUtils;
import dev.aisandbox.server.simulation.highlowcards.HighLowCardsBuilder;
import dev.aisandbox.server.simulation.highlowcards.proto.ClientAction;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class SandboxServerLauncher implements CommandLineRunner {

    private final List<SimulationBuilder> simulationBuilders;

    @Override
    public void run(String... args) throws Exception {
        // parse the command line
        RuntimeOptions runtimeOptions = RuntimeUtils.parseCommandLine(args);
        switch (runtimeOptions.command()) {
            case HELP -> help(runtimeOptions);
            case RUN -> runSimulation(runtimeOptions);
            case LIST -> listOptions(runtimeOptions);
        }

    }

    private void help(RuntimeOptions runtimeOptions) {
        if (runtimeOptions.simulation() != null) {
            helpSimulation(runtimeOptions.simulation());
        } else {
            System.out.println("Usage: java -jar sandbox-server.jar [options]");
        }
    }

    private void helpSimulation(String simulationName) {
        System.out.println("Simulation name: " + simulationName);
    }

    private void runSimulation(RuntimeOptions options) {
        if (options.simulation() == null) {
            System.out.println("Simulation name has not been set, use the '-s name' to choose the simulation or '--help' for more information.");
        } else {
            // create simulation
            SimulationBuilder simulationBuilder = new HighLowCardsBuilder();
            // create players
            List<Player> players = List.of(new Player("Player", 9000, ClientAction.class));
            // create simulation
            Simulation sim = simulationBuilder.build(players);
            // create output
            OutputRenderer out = new NullOutputRenderer();
            System.out.println("Starting simulation (ctrl-c to exit)...");
            // start simulation
            for (int i = 0; i < 10; i++) {
                sim.step(out);
            }
            // finish simulation
            sim.close();
            players.forEach(Player::close);
        }
    }

    private void listOptions(RuntimeOptions options) {
        if (options.simulation() == null) {
            System.out.println("Simulation name: " + options.simulation());
        } else {
            System.out.println("listing simulations:");
            for (SimulationBuilder simulationBuilder : simulationBuilders) {
                System.out.println(simulationBuilder.getName());
            }
        }
    }

}
