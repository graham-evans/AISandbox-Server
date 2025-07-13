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
    // are we asking for help or trying to run a simulation
    if (runtimeOptions.help()) {
      help(runtimeOptions);
    } else {
      runSimulation(runtimeOptions);
    }
  }

  private void help(RuntimeOptions runtimeOptions) {
    System.out.println("AISandbox Server CLI Application");
    System.out.println();
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
      // setup simulation & runner
      try {
        SimulationRunner runner = SimulationSetup.setupSimulation(simulationBuilder, agents, 9000,
            options.openExternal(), out, options.endEarly());
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
