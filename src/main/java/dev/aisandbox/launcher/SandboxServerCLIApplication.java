/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.launcher;

import dev.aisandbox.launcher.options.RuntimeUtils;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.SimulationParameter;
import dev.aisandbox.server.engine.SimulationRandomNumberGenerator;
import dev.aisandbox.server.engine.SimulationRunner;
import dev.aisandbox.server.engine.SimulationSetup;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.exception.SimulationSetupException;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.setup.SimulationSettings;
import dev.aisandbox.server.engine.telemetry.NullTelemetryEngine;
import dev.aisandbox.server.engine.telemetry.TelemetryEngine;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;

/**
 * Command-line interface application for running AI Sandbox simulations.
 *
 * <p>This class provides a command-line interface for configuring and running simulations without
 * requiring a graphical user interface. It supports:
 * <ul>
 *   <li>Listing available simulations and their parameters</li>
 *   <li>Configuring simulation parameters via command-line arguments</li>
 *   <li>Setting up network agents and port configuration</li>
 *   <li>Running simulations with various output options (bitmap, null)</li>
 *   <li>Providing help and documentation for each simulation</li>
 * </ul>
 *
 * <p>The CLI application is ideal for:
 * <ul>
 *   <li>Server deployments without GUI support</li>
 *   <li>Automated testing and batch processing</li>
 *   <li>Integration with CI/CD pipelines</li>
 *   <li>Remote execution scenarios</li>
 * </ul>
 *
 * @see SandboxServerLauncher
 * @see dev.aisandbox.server.engine.setup.SimulationSettings
 * @see SimulationBuilder
 */
@Slf4j
public class SandboxServerCLIApplication {

  /**
   * Cap an integer between two min/max values
   *
   * @param value The number to be capped.
   * @param min   The minimum value
   * @param max   The maximum value
   * @return number between min and max, exclusive.
   */
  static int capInteger(int value, int min, int max) {
    return Math.min(Math.max(value, min), max);
  }

  /**
   * Main execution method for the CLI application.
   *
   * <p>Parses command-line arguments and determines whether to display help information or execute
   * a simulation based on the provided options.
   *
   * @param args command-line arguments passed from the launcher
   */
  public void run(String... args) {
    System.out.println("AISandbox Server CLI Application");
    System.out.println();
    try {
      if (RuntimeUtils.isParseHelp(args)) { // is the user asking for help
        // is the user asking for help for a specific simulation or just general
        help(RuntimeUtils.getSimulation(args));
      } else {
        // try to build and run the simulation
        SimulationSettings settings = RuntimeUtils.parseCommandLine(args);
        // has a simulation been selected
        if (settings.selectedSimulationBuilder().get() != null) {
          // try and run the simulation
          runSimulation(settings);
        } else {
          // tell the user they MUST select a simulation
          System.err.println(
              "Please select a simulation with -s <name>. Use -h to show help and list simulations.");
        }
      }
    } catch (ParseException e) {
      System.err.println("Error parsing command line. Use -h for help.");
    }
  }

  /**
   * Displays help information based on the runtime options provided.
   *
   * <p>If a specific simulation is specified, shows detailed help for that simulation including
   * parameters and options. Otherwise, displays general application help.
   *
   * @param helpSimulation the simulation to show help for
   */
  private void help(Optional<SimulationBuilder> helpSimulation) {
    if (helpSimulation.isPresent()) {
      SimulationBuilder sim = helpSimulation.get();
      System.out.printf("Help for simulation %s\n\n", sim.getSimulationName());
      System.out.printf(" Minimum agents: %d\n", sim.getMinAgentCount());
      System.out.printf(" Maximum agents: %d\n", sim.getMaxAgentCount());

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

    } else {
      // show generic help
      HelpFormatter formatter = HelpFormatter.builder().get();
      System.out.println();
      try {
        formatter.printHelp("AISandboxServer", null, RuntimeUtils.getOptions(), null, true);
      } catch (IOException e) {
        log.error("Error printing help", e);
      }
    }
  }

  /**
   * Executes a simulation based on the provided runtime options.
   *
   * <p>This method handles the complete simulation lifecycle including:
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
  private void runSimulation(SimulationSettings options) {
    // create simulation
    SimulationBuilder builder = options.selectedSimulationBuilder().get();
    if (builder == null) {
      System.err.println(
          "You must select a simulation to run, use --help to show all simulations or -s <name> to select one.");
    } else {

 /*     // apply parameters (if any)
      for (String parameter : options.parameters()) {
        String[] keyValue = parameter.split("[=:]");
        if (keyValue.length != 2) { // NOPMD - AvoidLiteralsInIfCondition: clear in context
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
   */

      int agents = capInteger(options.agentCount().get(), builder.getMinAgentCount(),
          builder.getMaxAgentCount());
      // create output
      OutputRenderer out = options.createRenderer();
      // create telemetry
      TelemetryEngine telemetryEngine = options.createTelemetryEngine();
      // create random
      SimulationRandomNumberGenerator randomProvider = options.createRandom();
      // write summary
      System.out.println(
          "Running simulation '" + builder.getSimulationName() + "' with " + agents
              + " agents.");
      System.out.println("Output sent to " + out.getName());
      System.out.println("Listening on " + (options.externalNetwork().get() ? " all interfaces"
          : "loopback interface" + " starting on port " + options.defaultPort().get()));
      // setup simulation & runner
      try {
        SimulationRunner runner = SimulationSetup.setupSimulation(builder, agents,
            options.defaultPort().get(), options.externalNetwork().get(), out, Theme.LIGHT,
            options.maxStepCount().get(), randomProvider,new NullTelemetryEngine());
        // start simulation
        runner.start();
      } catch (SimulationSetupException e) {
        log.error("Error setting up simulation", e);
      }
    }
  }

/*

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
*/
}
