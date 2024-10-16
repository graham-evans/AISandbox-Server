package dev.aisandbox.server;

import dev.aisandbox.server.engine.*;
import dev.aisandbox.server.options.RuntimeOptions;
import dev.aisandbox.server.options.RuntimeUtils;
import dev.aisandbox.server.simulation.highlowcards.HighLowCardsBuilder;
import dev.aisandbox.server.simulation.highlowcards.proto.ClientAction;
import lombok.RequiredArgsConstructor;
import org.apache.commons.cli.HelpFormatter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class SandboxServerLauncher implements CommandLineRunner {

    private final List<SimulationBuilder> simulationBuilders;

    @Override
    public void run(String... args) throws Exception {
        // parse the command line
        RuntimeOptions runtimeOptions = RuntimeUtils.parseCommandLine(args);
        // decide which of the main three actions to perform
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
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar sandbox-server.jar", RuntimeUtils.getOptions());
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
        Optional<SimulationBuilder> oBuilder = findBuilder(options.simulation());
        if (oBuilder.isPresent()) {
            // show the options for the simulation
            SimulationBuilder sim = oBuilder.get();
            System.out.println(sim.getName());
            System.out.println("Minimum # clients "+sim.getMinPlayerCount());
            System.out.println("Maximum # clients "+sim.getMaxPlayerCount());
            System.out.println("Options:");
            for (Method method:sim.getClass().getDeclaredMethods()) {
                if (method.getName().startsWith("set") && method.getParameterCount() == 1) {
                    System.out.print(" ");
                    System.out.print(method.getName().substring(3));
                    System.out.print(":");
                    System.out.println(method.getParameters()[0].getType().getName());
                }
            }
        } else {
            // list the available simulations
            System.out.println("Available simulations:");
            for (SimulationBuilder simulationBuilder : simulationBuilders) {
                System.out.print(simulationBuilder.getName());
                System.out.print(" (");
                System.out.print(simulationBuilder.getMinPlayerCount());
                if (simulationBuilder.getMinPlayerCount() == simulationBuilder.getMaxPlayerCount()) {
                    System.out.println(simulationBuilder.getMinPlayerCount() == 1 ? " client)" : " clients)");
                } else {
                    System.out.print(" to ");
                    System.out.print(simulationBuilder.getMaxPlayerCount());
                    System.out.println(" clients)");
                }
            }
        }
    }

    private Optional<SimulationBuilder> findBuilder(String name) {
        Optional<SimulationBuilder> result = Optional.empty();
        for (SimulationBuilder simulationBuilder : simulationBuilders) {
            if (simulationBuilder.getName().equalsIgnoreCase(name)) {
                result = Optional.of(simulationBuilder);
            }
        }
        return result;
    }


}
