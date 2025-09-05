/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.launcher;

import dev.aisandbox.launcher.options.RuntimeOptions;
import dev.aisandbox.launcher.options.RuntimeUtils;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.SimulationParameter;
import dev.aisandbox.server.engine.SimulationRunner;
import dev.aisandbox.server.engine.SimulationSetup;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.exception.SimulationSetupException;
import dev.aisandbox.server.engine.output.BitmapOutputRenderer;
import dev.aisandbox.server.engine.output.NullOutputRenderer;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.simulation.SimulationEnumeration;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.HelpFormatter;

/**
 * Command-line interface application for running AI Sandbox simulations.
 * <p>
 * This class provides a command-line interface for configuring and running simulations
 * without requiring a graphical user interface. It supports:
 * </p>
 * <ul>
 *   <li>Listing available simulations and their parameters</li>
 *   <li>Configuring simulation parameters via command-line arguments</li>
 *   <li>Setting up network agents and port configuration</li>
 *   <li>Running simulations with various output options (bitmap, null)</li>
 *   <li>Providing help and documentation for each simulation</li>
 * </ul>
 * <p>
 * The CLI application is ideal for:
 * </p>
 * <ul>
 *   <li>Server deployments without GUI support</li>
 *   <li>Automated testing and batch processing</li>
 *   <li>Integration with CI/CD pipelines</li>
 *   <li>Remote execution scenarios</li>
 * </ul>
 *
 * @see SandboxServerLauncher
 * @see RuntimeOptions
 * @see SimulationBuilder
 */
@Slf4j
public class SandboxServerCLIApplication {

  /**
   * List of all available simulation builders loaded from the enumeration.
   */
  private final List<SimulationBuilder> simulationBuilders;

  /**
   * Constructs a new CLI application instance and initializes the available simulations.
   */
  public SandboxServerCLIApplication() {
    simulationBuilders = Arrays.stream(SimulationEnumeration.values())
        .map(SimulationEnumeration::getBuilder).toList();
  }

  /**
   * Main execution method for the CLI application.
   * <p>
   * Parses command-line arguments and determines whether to display help information
   * or execute a simulation based on the provided options.
   * </p>
   *
   * @param args command-line arguments passed from the launcher
   */
  public void run(String... args) {
    System.out.println("AISandbox Server CLI Application");
    System.out.println();
    // parse the command line
    RuntimeOptions runtimeOptions = RuntimeUtils.parseCommandLine(args);
    // are we asking for help or trying to run a simulation
    if (runtimeOptions.help()) {
      help(runtimeOptions);
    } else {
      runSimulation(runtimeOptions);
    }
  }

  /**
   * Displays help information based on the runtime options provided.
   * <p>
   * If a specific simulation is specified, shows detailed help for that simulation
   * including parameters and options. Otherwise, displays general application help.
   * </p>
   *
   * @param runtimeOptions the parsed runtime options containing help request details
   */
  private void help(RuntimeOptions runtimeOptions) {
    if (runtimeOptions.simulation() != null) {
      // we are looking for help on a particular simulation?
      helpSimulation(runtimeOptions.simulation());
    } else {
      // show generic help
      HelpFormatter formatter = new HelpFormatter();
      System.out.println();
      formatter.printHelp("AISandboxServer", RuntimeUtils.getOptions());
    }
  }

  /**
   * Executes a simulation based on the provided runtime options.
   * <p>
   * This method handles the complete simulation lifecycle including:
   * </p>
   * <ul>
   *   <li>Finding and configuring the specified simulation builder</li>
   *   <li>Applying command-line parameters to the simulation</li>
   *   <li>Setting up output renderers (bitmap, null)</li>
   *   <li>Configuring network agents and ports</li>
   *   <li>Starting the simulation execution</li>
   * </ul>
   *
   * @param options the parsed runtime options containing simulation configuration
   */
  private void runSimulation(RuntimeOptions options) {
    // create simulation
    Optional<SimulationBuilder> oBuilder = simulationBuilders.stream().filter(
        simulationBuilder -> simulationBuilder.getSimulationName()
            .equalsIgnoreCase(options.simulation())).findFirst();
    if (oBuilder.isEmpty()) {
      System.err.println(
          "Can't find simulation with that name, use --help to show all simulations");
    } else {
      SimulationBuilder simulationBuilder = oBuilder.get();
      // apply parameters (if any)
      for (String parameter : options.parameters()) {
        String[] keyValue = parameter.split("[=:]");
        if (keyValue.length != 2) {
          System.err.printf("Invalid parameter: '%s', use format key:value\n", parameter);
        } else {
          String key = keyValue[0];
          String value = keyValue[1];
          // map this to a simulation parameter
          Optional<SimulationParameter> oParam = simulationBuilder.getParameters().stream()
              .filter(param -> param.name().equalsIgnoreCase(key)).findFirst();
          if (oParam.isPresent()) {
            RuntimeUtils.setParameterValue(simulationBuilder, oParam.get(), value);
          } else {
            System.err.printf("Can't set '%s' to '%s'%n\n", key, value);
          }
        }
      }
      int agents = simulationBuilder.getMaxAgentCount();
      if ((options.agents() >= simulationBuilder.getMinAgentCount()) && (options.agents()
          <= simulationBuilder.getMaxAgentCount())) {
        agents = options.agents();
      }
      // create output
      OutputRenderer out = new NullOutputRenderer();
      if (options.outputImage()) {
        out = new BitmapOutputRenderer();
        out.setOutputDirectory(new File(options.outputDirectory()));
      }
      if (options.skip() > 0) {
        out.setSkipFrames(options.skip());
      }
      // write summary
      System.out.println(
          "Running simulation '" + simulationBuilder.getSimulationName() + "' with " + agents
              + " agents.");
      System.out.println("Output sent to " + out.getName());
      System.out.println("Listening on " + (options.openExternal() ? " all interfaces"
          : "loopback interface" + " starting on port " + options.startPort()));
      // setup simulation & runner
      try {
        SimulationRunner runner = SimulationSetup.setupSimulation(simulationBuilder, agents,
            options.startPort(), options.openExternal(), out, Theme.LIGHT, options.maxStepCount());
        // start simulation
        runner.start();
      } catch (SimulationSetupException e) {
        log.error("Error setting up simulation", e);
      }
    }
  }

  private void helpSimulation(String simulationName) {
    System.out.printf("Help for simulation %s\n\n", simulationName);
    Optional<SimulationBuilder> oSim = findBuilder(simulationName);
    if (oSim.isPresent()) {
      SimulationBuilder sim = oSim.get();
      System.out.printf(" Minimum agents: %d\n", sim.getMinAgentCount());
      System.out.printf(" Maximum agents: %d\n", sim.getMaxAgentCount());
      try {
        List<SimulationParameter> parameters = sim.getParameters();
        if (!parameters.isEmpty()) {
          System.out.println("Options (use -o key:value to set)");
          for (SimulationParameter parameter : parameters) {
            // test if this is an enum
            if (parameter.parameterType().isEnum()) {
              System.out.printf(" %s (%s) - %s %s", parameter.name(),
                  RuntimeUtils.getParameterValue(sim, parameter), parameter.description(),
                  Arrays.toString(parameter.parameterType().getEnumConstants()));
            } else {
              System.out.printf(" %s (%s) - %s", parameter.name(),
                  RuntimeUtils.getParameterValue(sim, parameter), parameter.description());
            }
          }
        }
      } catch (Exception e) {
        log.error("Error during describe simulation", e);
      }
    } else {
      System.out.println("Can't find simulation with that name");
    }
    System.out.println();
  }

  private Optional<SimulationBuilder> findBuilder(String name) {
    for (SimulationBuilder simulationBuilder : simulationBuilders) {
      if (simulationBuilder.getSimulationName().equalsIgnoreCase(name)) {
        return Optional.of(simulationBuilder);
      }
    }
    System.err.println(
        "No simulation of that name exists, use the --help option to list all simulations");
    return Optional.empty();
  }

  private void listOptions(RuntimeOptions options) {
    if (options.simulation() == null) {
      // list the available simulations
      System.out.println("Available simulations:");
      for (SimulationBuilder simulationBuilder : simulationBuilders) {
        if (simulationBuilder.getMinAgentCount() == simulationBuilder.getMaxAgentCount()) {
          System.out.printf("%s (%d agents, %s)", simulationBuilder.getSimulationName(),
              simulationBuilder.getMinAgentCount(), simulationBuilder.getDescription());
        } else {
          System.out.printf("%s (%d-%d agents, %s)", simulationBuilder.getSimulationName(),
              simulationBuilder.getMinAgentCount(), simulationBuilder.getMaxAgentCount(),
              simulationBuilder.getDescription());
        }
      }
    } else {
      // show the help for the simulation
      helpSimulation(options.simulation());
    }
  }

}
