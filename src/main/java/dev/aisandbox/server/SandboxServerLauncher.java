package dev.aisandbox.server;

import dev.aisandbox.server.engine.*;
import dev.aisandbox.server.engine.output.BitmapOutputRenderer;
import dev.aisandbox.server.engine.output.NullOutputRenderer;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.output.ScreenOutputRenderer;
import dev.aisandbox.server.options.RuntimeOptions;
import dev.aisandbox.server.options.RuntimeUtils;
import dev.aisandbox.server.simulation.highlowcards.HighLowCardsBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.HelpFormatter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
public class SandboxServerLauncher implements CommandLineRunner {

    private final List<SimulationBuilder> simulationBuilders;

    private boolean halted = false;

    @Override
    public void run(String... args) throws Exception {
        // parse the command line
        RuntimeOptions runtimeOptions = RuntimeUtils.parseCommandLine(args);
        // do we need to force 'head' mode?
        if (runtimeOptions.output().equals(RuntimeOptions.OutputOptions.SCREEN)) {
            System.setProperty("java.awt.headless", "false");
        }
        // decide which of the main three actions to perform
        switch (runtimeOptions.command()) {
            case HELP -> help(runtimeOptions);
            case RUN -> runSimulation(runtimeOptions);
            case LIST -> listOptions(runtimeOptions);
        }

    }

    private void help(RuntimeOptions runtimeOptions) {
        if (runtimeOptions.simulation() != null) { // are we looking for help on a particular simulation?
            helpSimulation(runtimeOptions.simulation());
        } else {
            // show generic help
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar sandbox-server.jar", RuntimeUtils.getOptions());
        }
    }

    private void helpSimulation(String simulationName) {
        log.info("Simulation name: {}", simulationName);
        Optional<SimulationBuilder> optionalSimulationBuilder = findBuilder(simulationName);
        if (optionalSimulationBuilder.isPresent()) {
            SimulationBuilder simulationBuilder = optionalSimulationBuilder.get();
            log.info("Minimum players: {}", simulationBuilder.getMinPlayerCount());
            log.info("Maximum players: {}", simulationBuilder.getMaxPlayerCount());
            log.info("Options (use -o key:value to set)");
        } else {
            log.error("Error - No simulation of that name exists");
        }
    }

    private void runSimulation(RuntimeOptions options) {
        if (options.simulation() == null) {
            log.info("Simulation name has not been set, use the '-s name' to choose the simulation or '--help' for more information.");
        } else {
            // create simulation
            SimulationBuilder simulationBuilder = new HighLowCardsBuilder();
            // create players
            List<Player> players = List.of(new NetworkPlayer("Player", 9000));
            // create simulation
            Simulation sim = simulationBuilder.build(players, Theme.DEFAULT);
            // create output
            OutputRenderer out = switch (options.output()) {
                case PNG -> new BitmapOutputRenderer(sim);
                case SCREEN -> new ScreenOutputRenderer(sim);
                default -> new NullOutputRenderer();
            };
            out.setup();
            log.info("Writing output to {}", out.getName());
            log.info("Starting simulation (ctrl-c to exit)...");
            // start simulation
            while (!halted) {
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
            log.info(sim.getName());
            log.info("Minimum # clients {}", sim.getMinPlayerCount());
            log.info("Maximum # clients {}", sim.getMaxPlayerCount());
            log.info("Options:");
            for (Method method : sim.getClass().getDeclaredMethods()) {
                if (method.getName().startsWith("set") && method.getParameterCount() == 1) {
                    log.info(" {}:{}", method.getName().substring(3), method.getParameters()[0].getType().getName());
                }
            }
        } else {
            // list the available simulations
            log.info("Available simulations:");
            for (SimulationBuilder simulationBuilder : simulationBuilders) {
                if (simulationBuilder.getMinPlayerCount() == simulationBuilder.getMaxPlayerCount()) {
                    log.info("{} ({} clients)", simulationBuilder.getName(), simulationBuilder.getMinPlayerCount());
                } else {
                    log.info("{} ({} to {} clients)", simulationBuilder.getName(), simulationBuilder.getMinPlayerCount(), simulationBuilder.getMaxPlayerCount());
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
