package dev.aisandbox.launcher;

import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.SimulationParameter;
import dev.aisandbox.server.engine.SimulationRunner;
import dev.aisandbox.server.engine.SimulationSetup;
import dev.aisandbox.server.engine.output.BitmapOutputRenderer;
import dev.aisandbox.server.engine.output.NullOutputRenderer;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.output.ScreenOutputRenderer;
import dev.aisandbox.server.options.RuntimeOptions;
import dev.aisandbox.server.options.RuntimeUtils;
import dev.aisandbox.server.simulation.SimulationEnumeration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.HelpFormatter;

@Slf4j
public class SandboxServerCLIApplication {

  private final List<SimulationBuilder> simulationBuilders;

  public SandboxServerCLIApplication() {
    simulationBuilders = Arrays.stream(SimulationEnumeration.values())
        .map(SimulationEnumeration::getBuilder).toList();
  }

  public void run(String... args) {
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
    Optional<SimulationBuilder> oSim = findBuilder(simulationName);
    if (oSim.isPresent()) {
      SimulationBuilder sim = oSim.get();
      log.info(" Minimum agents: {}", sim.getMinAgentCount());
      log.info(" Maximum agents: {}", sim.getMaxAgentCount());
      try {
        List<SimulationParameter> parameters = sim.getParameters();
        if (!parameters.isEmpty()) {
          log.info("Options (use -o key:value to set)");
          for (SimulationParameter parameter : parameters) {
            // test if this is an enum
            if (parameter.parameterType().isEnum()) {
              log.info(" {} ({}) - {} {}", parameter.name(),
                  RuntimeUtils.getParameterValue(sim, parameter), parameter.description(),
                  parameter.parameterType().getEnumConstants());
            } else {
              log.info(" {} ({}) - {}", parameter.name(),
                  RuntimeUtils.getParameterValue(sim, parameter), parameter.description());
            }
          }
        }
      } catch (Exception e) {
        log.error("Error during describe simulation", e);
      }
    }
  }

  private void runSimulation(RuntimeOptions options) {
    if (options.simulation() == null) {
      log.info("Simulation name has not been set, use the '-s name' to choose the simulation or "
          + "'--help' for more information.");
    } else {
      // create simulation
      Optional<SimulationBuilder> oBuilder = simulationBuilders.stream().filter(
          simulationBuilder -> simulationBuilder.getSimulationName()
              .equalsIgnoreCase(options.simulation())).findFirst();
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
            // map this to a simulation parameter
            Optional<SimulationParameter> oParam = simulationBuilder.getParameters().stream()
                .filter(param -> param.name().equalsIgnoreCase(key)).findFirst();
            if (oParam.isPresent()) {
              RuntimeUtils.setParameterValue(simulationBuilder, oParam.get(), value);
            } else {
              log.warn("Can't set {} to {}", key, value);
            }
          }
        }
        // TODO : set the number of agents
        int agents = simulationBuilder.getMaxAgentCount();
        // create output
        OutputRenderer out = switch (options.output()) {
          case IMAGE -> new BitmapOutputRenderer();
          case SCREEN -> new ScreenOutputRenderer();
          default -> new NullOutputRenderer();
        };
        // setup simulation & runner
        SimulationRunner runner = SimulationSetup.setupSimulation(simulationBuilder, agents, 9000,
            out);
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
          log.info("{} ({} agents, {})", simulationBuilder.getSimulationName(),
              simulationBuilder.getMinAgentCount(), simulationBuilder.getDescription());
        } else {
          log.info("{} ({}-{} agents, {})", simulationBuilder.getSimulationName(),
              simulationBuilder.getMinAgentCount(), simulationBuilder.getMaxAgentCount(),
              simulationBuilder.getDescription());
        }
      }
    } else {
      // show the help for the simulation
      helpSimulation(options.simulation());
    }
  }

  private Optional<SimulationBuilder> findBuilder(String name) {
    for (SimulationBuilder simulationBuilder : simulationBuilders) {
      if (simulationBuilder.getSimulationName().equalsIgnoreCase(name)) {
        return Optional.of(simulationBuilder);
      }
    }
    log.error("No simulation of that name exists, use the --list option to list all simulations");
    return Optional.empty();
  }


}
