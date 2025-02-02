package dev.aisandbox.launcher;

import dev.aisandbox.server.engine.*;
import dev.aisandbox.server.engine.output.*;
import dev.aisandbox.server.options.ParameterEnumInfo;
import dev.aisandbox.server.options.RuntimeOptions;
import dev.aisandbox.server.options.RuntimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.HelpFormatter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication(scanBasePackages = "dev.aisandbox.server")
public class SandboxServerCLIApplication implements CommandLineRunner {

    private final List<SimulationBuilder> simulationBuilders;

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
        if (runtimeOptions.simulation() != null) {
            // we are looking for help on a particular simulation?
            helpSimulation(runtimeOptions.simulation());
        } else {
            // show generic help
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar sandbox-server.jar", RuntimeUtils.getOptions());
        }
    }

    private void helpSimulation(String simulationName) {
        log.info("Simulation name: {}", simulationName);
        SimulationBuilder simulationBuilder = findBuilder(simulationName);
        log.info(" Minimum players: {}", simulationBuilder.getMinAgentCount());
        log.info(" Maximum players: {}", simulationBuilder.getMaxAgentCount());
        try {
            List<ParameterEnumInfo> parameters= RuntimeUtils.listEnumParameters(simulationBuilder);
            // show parameters
            if (!parameters.isEmpty()) {
                log.info("Options (use -o key:value to set)");
                for (ParameterEnumInfo parameter : parameters) {
                    log.info(" {}", parameter);
                }
            }
        } catch (Exception e) {
            log.error("Error during describe simulation", e);
        }
    }

    private void runSimulation(RuntimeOptions options) {
        if (options.simulation() == null) {
            log.info("Simulation name has not been set, use the '-s name' to choose the simulation or '--help' for more information.");
        } else {
            // create simulation
            Optional<SimulationBuilder> oBuilder = simulationBuilders.stream().filter(simulationBuilder -> simulationBuilder.getSimulationName().equalsIgnoreCase(options.simulation())).findFirst();
            if (oBuilder.isEmpty()) {
                log.warn("Can't find simulation with that name, use --list to show all simulations");
            } else {
                SimulationBuilder simulationBuilder = oBuilder.get();
                // apply parameters (if any)
                for (String parameter : options.parameters()) {
                    String[] keyValue = parameter.split("[=:]");
                    if (keyValue.length != 2) {
                        log.warn("Invalid parameter: '{}', use format key:value ", parameter);
                    } else {
                        String key = keyValue[0];
                        String value = keyValue[1];
                        try {
                            RuntimeUtils.setEnumParameter(simulationBuilder, key, value);
                        } catch (Exception e) {
                            log.warn("Can't set {} to {}", key, value);
                        }
                    }
                }
                // TODO : set the number of agents
                int agents = simulationBuilder.getMaxAgentCount();
                // create output
                OutputRenderer out = switch (options.output()) {
                    case IMAGE -> new BitmapOutputRenderer();
                    case VIDEO -> new MP4Output( new File("./test.mp4"));
                    case SCREEN -> new ScreenOutputRenderer();
                    default -> new NullOutputRenderer();
                };
                // setup simulation & runner
                SimulationRunner runner = SimulationSetup.setupSimulation(simulationBuilder,agents,9000,out);
                // start simulation
                runner.start();
            }
        }
    }

    private void listOptions(RuntimeOptions options) {
        if (options.simulation() == null) {
            // list the available simulations
            log.info("Available simulations:");
            for (SimulationBuilder simulationBuilder : simulationBuilders) {
                if (simulationBuilder.getMinAgentCount() == simulationBuilder.getMaxAgentCount()) {
                    log.info("{} ({} agents)", simulationBuilder.getSimulationName(), simulationBuilder.getMinAgentCount());
                } else {
                    log.info("{} ({} to {} agents)", simulationBuilder.getSimulationName(), simulationBuilder.getMinAgentCount(), simulationBuilder.getMaxAgentCount());
                }
            }
        } else {
            // show the help for the simulation
            helpSimulation(options.simulation());
        }
    }

    private SimulationBuilder findBuilder(String name) {
        for (SimulationBuilder simulationBuilder : simulationBuilders) {
            if (simulationBuilder.getSimulationName().equalsIgnoreCase(name)) {
                return simulationBuilder;
            }
        }
        log.error("No simulation of that name exists, use the --list option to list all simulations");
        System.exit(0);
        return null;
    }


}
