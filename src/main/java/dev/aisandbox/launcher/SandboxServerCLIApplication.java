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
    System.out.println();
    System.out.println("AISandbox Server CLI Application");
    System.out.println("================================");
    System.out.println();
    try {
      if (RuntimeUtils.isParseHelp(args)) { // is the user asking for help
        // is the user asking for help for a specific simulation or just general
        Optional<SimulationBuilder> oSim = RuntimeUtils.getSimulation(args);
        if (oSim.isPresent()) {
          help(oSim.get());
        } else {
          help();
        }

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
   * <p>Shows detailed help for a specific simulation
   *
   * @param sim the simulation to show help for
   */
  private void help(SimulationBuilder sim) {
    System.out.printf(" Help for simulation %s\n\n", sim.getSimulationName());
    System.out.printf(" Minimum agents: %d\n", sim.getMinAgentCount());
    System.out.printf(" Maximum agents: %d\n", sim.getMaxAgentCount());
    System.out.println();
    List<SimulationParameter> parameters = sim.getParameters();
    if (!parameters.isEmpty()) {
      System.out.println(" Options (use -o key:value to set)");
      System.out.println();
      for (SimulationParameter parameter : parameters) {
        // test if this is an enum
        if (parameter.parameterType().isEnum()) {
          System.out.printf("   %s (%s) - %s %s\n", parameter.name(),
              RuntimeUtils.getParameterValue(sim, parameter), parameter.description(),
              Arrays.toString(parameter.parameterType().getEnumConstants()));
        } else {
          System.out.printf("   %s (%s) - %s\n", parameter.name(),
              RuntimeUtils.getParameterValue(sim, parameter), parameter.description());
        }
      }
      System.out.println();
    }
  }

  /**
   * Displays help information based on the runtime options provided.
   *
   * <p>Shows generalised help.
   */
  private void help() {
    // show generic help
    HelpFormatter formatter = HelpFormatter.builder().setShowSince(false).get();
    try {
      formatter.printHelp("AISandboxServer", null, RuntimeUtils.getOptions(), null, true);
    } catch (IOException e) {
      log.error("Error printing help", e);
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
      try {
        // report setup
        System.out.println(options.toReport());
        // setup simulation & runner
        System.out.println(
            "Running simulation '" + options.selectedSimulationBuilder().get().getSimulationName() + "' with " + options.agentCount()
                + " agents.");
        SimulationRunner runner = options.build();

        System.out.println("Listening on " + (options.externalNetwork().get() ? " all interfaces"
            : "loopback interface" + " starting on port " + options.defaultPort().get()));
        // start simulation
        runner.start();
      } catch (SimulationSetupException e) {
        log.error("Error setting up simulation", e);
      }
    }
  }

}
