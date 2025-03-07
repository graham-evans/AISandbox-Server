package dev.aisandbox.server.options;

import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.SimulationParameter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.EnumUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class RuntimeUtils {

    /***
     * Get the options available for the CLI.
     *
     * @return the populated Options class from Apache Commons CLI
     */
    public static Options getOptions() {
        final Options options = new Options();
        // add help option
        options.addOption("h", "help", false, "Print an overview");
        // add simulation selector
        options.addOption("s", "simulation", true, "Simulation to run");
        // add list option
        options.addOption("l", "list", false, "List the simulations, or the options available for a selected simulation");
        // add output
        options.addOption("o", "output", true, "Output format [NONE(Default),SCREEN,PNG]");
        options.addOption("d", "dir", true, "Output directory");
        // Agent Count
        options.addOption("a", "agents", true, "Number of agents (within the range for a simulation)");
        // simulation options
        options.addOption("p", "parameter", true, "Simulation specific parameter in the format key:value, use the list options to show all available parameters");
        return options;
    }

    /**
     * Parse a set of strings from the command line and populate a RuntimeOptions POJO.
     *
     * @param args the strings from the command line
     * @return the RuntimeOptions POJO
     */
    public static RuntimeOptions parseCommandLine(String[] args) {
        RuntimeOptions options = null;
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(getOptions(), args);
            // build runtime options
            RuntimeOptions.RuntimeOptionsBuilder workBuilder = RuntimeOptions.builder();
            // assume we should run simulation
            workBuilder.command(RuntimeOptions.RuntimeCommand.RUN);
            // help
            if (cmd.hasOption('h')) {
                workBuilder.command(RuntimeOptions.RuntimeCommand.HELP);
            }
            // list
            if (cmd.hasOption('l')) {
                workBuilder.command(RuntimeOptions.RuntimeCommand.LIST);
            }
            // choose simulation
            if (cmd.hasOption('s')) {
                workBuilder.simulation(cmd.getOptionValue("s"));
            }
            // assume no output
            workBuilder.output(RuntimeOptions.OutputOptions.NONE);
            // show screen
            if (cmd.hasOption('o') && "screen".equalsIgnoreCase(cmd.getOptionValue('o'))) {
                workBuilder.output(RuntimeOptions.OutputOptions.SCREEN);
            }
            // write images
            if (cmd.hasOption('o') && "png".equalsIgnoreCase(cmd.getOptionValue('o'))) {
                workBuilder.output(RuntimeOptions.OutputOptions.IMAGE);
            }
            // choose the output directory
            workBuilder.outputDirectory(".");
            if (cmd.hasOption('d')) {
                workBuilder.outputDirectory(cmd.getOptionValue('d'));
            }
            // choose the number of  agents
            if (cmd.hasOption('a')) {
                workBuilder.agents(Integer.parseInt(cmd.getOptionValue('a')));
            }
            // read parameters
            if (cmd.hasOption('p')) {
                Arrays.stream(cmd.getOptionValues('p')).forEach(workBuilder::parameter);
            }
            options = workBuilder.build();
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            System.exit(-1);
        }
        return options;
    }

    @Deprecated
    public Optional<String> getParameterEnumOptions(Object bean, String parameter) {
        try {
            String methodName = "get" + Character.toUpperCase(parameter.charAt(0)) + parameter.substring(1);
            Method getMethod = bean.getClass().getMethod(methodName);
            Object returnValue = getMethod.invoke(bean);
            if (returnValue.getClass().isEnum()) {
                StringBuilder stringBuilder = new StringBuilder();
                return Optional.of(Arrays.stream(returnValue.getClass().getEnumConstants()).map(Object::toString).collect(Collectors.joining(",")));
            } else {
                return Optional.empty();
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return Optional.empty();
        }
    }

    public String getParameterValue(SimulationBuilder simulationBuilder, SimulationParameter parameter) {
        try {
            String methodName = "get" + Character.toUpperCase(parameter.name().charAt(0)) + parameter.name().substring(1);
            Method getMethod = simulationBuilder.getClass().getMethod(methodName);
            return getMethod.invoke(simulationBuilder).toString();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Error getting method information for parameter '{}' on class {}", parameter, simulationBuilder.getClass().getName(), e);
            return null;
        }
    }

    public void setParameterValue(SimulationBuilder simulationBuilder, String name, String value) {
        Optional<SimulationParameter> oParam = simulationBuilder.getParameters().stream().filter(param -> param.name().equalsIgnoreCase(name)).findFirst();
        if (oParam.isPresent()) {
            SimulationParameter param = oParam.get();
            setParameterValue(simulationBuilder, param, value);
        }
    }

    /**
     * Use reflection to set a bean's field. Do not throw an exception if this is not possible.
     *
     * @param simulationBuilder      The POJO to update
     * @param parameter the name of the field
     * @param value     the new value
     */
    public void setParameterValue(SimulationBuilder simulationBuilder, SimulationParameter parameter, String value) {
        try {
            // get the first setter method which is public and has one parameter of the right type
            String methodName = "set" + Character.toUpperCase(parameter.name().charAt(0)) + parameter.name().substring(1);
            Optional<Method> oMethod = Arrays.stream(simulationBuilder.getClass().getMethods())
                    .filter((m) -> m.getName().equals(methodName))
                    .filter(method -> Modifier.isPublic(method.getModifiers()))
                    .filter(method -> method.getParameterCount() == 1)
                    .filter(method -> method.getParameterTypes()[0] == parameter.parameterType())
                    .findFirst();
            if (oMethod.isEmpty()) {
                log.error("Method {} not found", methodName);
            } else {
                Method m = oMethod.get();
                if (parameter.parameterType().isEnum()) {
                    // this is an enum
                    if (EnumUtils.isValidEnumIgnoreCase((Class<Enum>) parameter.parameterType(), value)) {
                        m.invoke(simulationBuilder, EnumUtils.getEnumIgnoreCase((Class<Enum>) parameter.parameterType(), value));
                    } else {
                        log.warn("Can't set enum '{}' to '{}' as it isn't a valid option", parameter.parameterType().getName(), value);
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
