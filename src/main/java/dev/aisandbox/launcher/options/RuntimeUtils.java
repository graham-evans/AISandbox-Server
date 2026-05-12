/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.launcher.options;

import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.SimulationParameter;
import dev.aisandbox.server.engine.setup.SimulationSettings;
import dev.aisandbox.server.simulation.SimulationEnumeration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.EnumUtils;

/**
 * Utility class providing command-line parsing and parameter handling for the AI Sandbox CLI.
 *
 * <p>This utility class handles all aspects of command-line argument processing, parameter
 * validation,
 * and runtime option management for the AI Sandbox CLI application. It provides methods for:
 * <ul>
 *   <li>Parsing command-line arguments into structured options</li>
 *   <li>Validating and applying simulation parameters</li>
 *   <li>Dynamic parameter value setting using reflection</li>
 *   <li>Help text generation and formatting</li>
 * </ul>
 *
 * <p>The utility supports complex parameter types including enumerations, primitive types,
 * and custom parameter validation. It uses Apache Commons CLI for robust argument parsing
 * and provides comprehensive error handling and user feedback.
 *
 * @see SimulationSettings
 * @see SimulationParameter
 */
@Slf4j
@UtilityClass
public class RuntimeUtils {

  @Getter
  private static final Options options;

  static {
    options = new Options();
    // add help option
    options.addOption("h", "help", false, "Print an overview");
    // add simulation selector
    options.addOption("s", "simulation", true,
        "Simulation to run [" + Arrays.stream(SimulationEnumeration.values())
            .map(simulationEnumeration -> simulationEnumeration.getBuilder().getSimulationName())
            .collect(Collectors.joining(" | ")) + "]");
    // add output
    options.addOption("i", "png", false, "Write output to PNG files");
    options.addOption("d", "dir", true, "Output directory");
    options.addOption("k", "skip", true, "Frames to skip when rendering");
    // end early
    options.addOption("e", "end", true, "End simulation after n steps");
    // Agent Count
    options.addOption("a", "agents", true, "Number of agents (within the range for a simulation)");
    // simulation options
    options.addOption("p", "parameter", true,
        "Simulation specific parameter in the format key:value");
    // network options
    options.addOption("n", "network", false,
        "Allow connections from the network (default localhost only)");
    options.addOption("t", "port", true, "Starting port (default 9000)");
  }

  /**
   * Check if the user is asking for help.
   *
   * @param args the command line
   * @return
   */
  public static boolean isParseHelp(String[] args) throws ParseException {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);
    return cmd.hasOption('h');
  }

  /**
   * Get the simulation the user is calling (-s option).
   *
   * @param args the command line
   * @return the simulation or empty if none or invalid.
   */
  public static Optional<SimulationBuilder> getSimulation(String[] args) throws ParseException {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);
    if (!cmd.hasOption('s')) {
      return Optional.empty();
    } else {
      return getSimulation(cmd.getOptionValue('s'));
    }
  }

  public static Optional<SimulationBuilder> getSimulation(String simulationName) {
    return Arrays.stream(SimulationEnumeration.values())
        .map(SimulationEnumeration::getBuilder).filter(
            simulationBuilder -> simulationBuilder.getSimulationName()
                .equalsIgnoreCase(simulationName)).findFirst();
  }

  /**
   * Parse a set of strings from the command line and populate a SimulationSettings objectO.
   *
   * <p>Converts raw command-line arguments into a structured {@link SimulationSettings} object
   * with validated parameters, default values, and error handling for invalid arguments.
   *
   * @param args the strings from the command line
   * @return the RuntimeOptions POJO containing parsed command-line options
   */
  public static SimulationSettings parseCommandLine(String[] args) {
    SimulationSettings simulation = new SimulationSettings();
    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(options, args);
      // choose simulation
      if (cmd.hasOption('s')) {
        simulation.selectedSimulationBuilder()
            .set(getSimulation(cmd.getOptionValue('s')).orElse(null));
      }
      // setup output - Default to none for CLI
      simulation.outputNone().set(true);
      // write images to PNG
      if (cmd.hasOption('i')) {
        simulation.outputPNG().set(true);
      }
      // write images to dir
      if (cmd.hasOption('d')) {
        simulation.outputPNGPath().set(cmd.getOptionValue("d"));
      }
      // skip frames
      if (cmd.hasOption('k')) {
        simulation.outputSkipFrames().set(Integer.parseInt(cmd.getOptionValue("k")));
      }
      // choose the number of  agents
      if (cmd.hasOption('a')) {
        simulation.agentCount().set(Integer.parseInt(cmd.getOptionValue('a')));
      }
      // how long to run the simluation
      if (cmd.hasOption('e')) {
        simulation.maxStepCount().set(Long.parseLong(cmd.getOptionValue('e')));
      }
      // open to network connections
      if (cmd.hasOption('n')) {
        simulation.externalNetwork().set(true);
      }
      // choose default port
      if (cmd.hasOption('t')) {
        simulation.defaultPort().set(Integer.parseInt(cmd.getOptionValue('t')));
      }
      // read parameters
      if (cmd.hasOption('p')) {
        Arrays.stream(cmd.getOptionValues('p'))
            .forEach(s -> setParameterValue(simulation.selectedSimulationBuilder().get(), s));
      }
    } catch (ParseException e) {
      System.err.println("Error parsing command line arguments: " + e.getMessage());
      System.exit(-1); // NOPMD: allowed as we're running from the command line.
    }
    return simulation;
  }

  /**
   * Retrieves enumeration options for a given parameter from a bean object.
   *
   * @param bean      the object containing the parameter
   * @param parameter the parameter name to retrieve options for
   * @return an Optional containing comma-separated enum values if the parameter is an enum, or
   * empty Optional if not
   * @deprecated Use simulation parameter validation through SimulationBuilder instead
   */
  @Deprecated
  public Optional<String> getParameterEnumOptions(Object bean, String parameter) {
    try {
      String methodName =
          "get" + Character.toUpperCase(parameter.charAt(0)) + parameter.substring(1);
      Method getMethod = bean.getClass().getMethod(methodName);
      Object returnValue = getMethod.invoke(bean);
      if (returnValue.getClass().isEnum()) {
        return Optional.of(
            Arrays.stream(returnValue.getClass().getEnumConstants()).map(Object::toString)
                .collect(Collectors.joining(",")));
      } else {
        return Optional.empty();
      }
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      return Optional.empty();
    }
  }

  /**
   * Retrieves the current value of a simulation parameter from a SimulationBuilder.
   *
   * @param simulationBuilder the simulation builder instance
   * @param parameter         the parameter to retrieve
   * @return the string representation of the parameter value, or null if retrieval fails
   */
  public String getParameterValue(SimulationBuilder simulationBuilder,
      SimulationParameter parameter) {
    try {
      String methodName =
          "get" + Character.toUpperCase(parameter.name().charAt(0)) + parameter.name().substring(1);
      Method getMethod = simulationBuilder.getClass().getMethod(methodName);
      return getMethod.invoke(simulationBuilder).toString();
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      log.warn("Error getting method information for parameter '{}' on class {}", parameter,
          simulationBuilder.getClass().getName(), e);
      return null;
    }
  }

  /**
   * Sets a simulation parameter using a parameter name:value string.
   *
   * @param simulationBuilder the builder to set the parameter on
   * @param parameter         the name of the parameter and value in the form "name:value" or
   *                          "name=value"
   */
  public void setParameterValue(SimulationBuilder simulationBuilder, String parameter) {
    String[] keyValue = parameter.split("[=:]");
    if (keyValue.length != 2) { // NOPMD - AvoidLiteralsInIfCondition: clear in context
      System.err.printf("Invalid parameter: '%s', use format key:value\n", parameter);
    } else {
      setParameterValue(simulationBuilder, keyValue[0], keyValue[1]);
    }
  }

  /**
   * Sets a simulation parameter value by parameter name.
   *
   * @param simulationBuilder the simulation builder instance to update
   * @param name              the name of the parameter to set
   * @param value             the string value to set
   */
  public void setParameterValue(SimulationBuilder simulationBuilder, String name, String value) {
    Optional<SimulationParameter> oParam = simulationBuilder.getParameters().stream()
        .filter(param -> param.name().equalsIgnoreCase(name)).findFirst();
    if (oParam.isPresent()) {
      SimulationParameter param = oParam.get();
      setParameterValue(simulationBuilder, param, value);
    }
  }

  /**
   * Use reflection to set a bean's field. Do not throw an exception if this is not possible.
   *
   * @param simulationBuilder The POJO to update
   * @param parameter         the name of the field
   * @param value             the new value
   */
  public void setParameterValue(SimulationBuilder simulationBuilder, SimulationParameter parameter,
      String value) {
    try {
      // get the first setter method which is public and has one parameter of the right type
      String methodName =
          "set" + Character.toUpperCase(parameter.name().charAt(0)) + parameter.name().substring(1);
      Optional<Method> oMethod = Arrays.stream(simulationBuilder.getClass().getMethods())
          .filter((m) -> m.getName().equals(methodName))
          .filter(method -> Modifier.isPublic(method.getModifiers()))
          .filter(method -> method.getParameterCount() == 1)
          .filter(method -> method.getParameterTypes()[0] == parameter.parameterType()).findFirst();
      if (oMethod.isEmpty()) {
        log.error("Method {} not found", methodName);
      } else {
        Method m = oMethod.get();
        if (parameter.parameterType().isEnum()) {
          // this is an enum - we know it's safe because we checked isEnum() above
          @SuppressWarnings({"unchecked",
              "rawtypes"}) Class<Enum> enumClass = (Class<Enum>) parameter.parameterType();
          @SuppressWarnings("unchecked") boolean isValid = EnumUtils.isValidEnumIgnoreCase(
              enumClass, value);
          if (isValid) {
            @SuppressWarnings({"unchecked",
                "rawtypes"}) Enum enumValue = EnumUtils.getEnumIgnoreCase(enumClass, value);
            m.invoke(simulationBuilder, enumValue);
          } else {
            log.warn("Can't set enum '{}' to '{}' as it isn't a valid option",
                parameter.parameterType().getName(), value);
          }
        } else if (parameter.parameterType() == Boolean.class) {
          m.invoke(simulationBuilder, Boolean.parseBoolean(value));
        } else if (parameter.parameterType() == Integer.class) {
          m.invoke(simulationBuilder, Integer.parseInt(value));
        } else {
          log.error("Dont know how to set parameter of type {} to '{}'", parameter.name(), value);
        }
      }
    } catch (Exception e) {
      log.warn("Error setting property {} to '{}'", parameter, value);
    }
  }

}
