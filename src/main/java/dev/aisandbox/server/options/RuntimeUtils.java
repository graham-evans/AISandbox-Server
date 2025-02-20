package dev.aisandbox.server.options;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.EnumUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

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

    /**
     * Use reflection to get the type (class) of a POJO's field using the normal get method.
     *
     * @param bean      The POJO to read
     * @param parameter The name of the field
     * @return the Class of the field type.
     */
    public Class<?> getParameterClass(Object bean, String parameter) {
        try {
            String methodName = "get" + Character.toUpperCase(parameter.charAt(0)) + parameter.substring(1);
            Method getMethod = bean.getClass().getMethod(methodName);
            Object result = getMethod.invoke(bean);
            return result.getClass();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Error getting method information for parameter '{}' on class {}", parameter, bean.getClass().getName(), e);
            return null;
        }
    }

    public Object getParameterValue(Object bean, String parameter) throws IllegalArgumentException {
        try {
            String methodName = "get" + Character.toUpperCase(parameter.charAt(0)) + parameter.substring(1);
            Method getMethod = bean.getClass().getMethod(methodName);
            return getMethod.invoke(bean);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Error getting method information for parameter '{}' on class {}", parameter, bean.getClass().getName(), e);
            throw new IllegalArgumentException("No get method found for parameter '" + parameter + "'");
        }
    }

    /**
     * Use reflection to set a bean's field. Do not throw an exception if this is not possible.
     *
     * @param bean      The POJO to update
     * @param parameter the name of the field
     * @param value     the new value
     */
    public void setParameterValue(Object bean, String parameter, String value) {
        try {
            Class<?> parameterClass = getParameterClass(bean, parameter);
            String methodName = "set" + Character.toUpperCase(parameter.charAt(0)) + parameter.substring(1);
            Method setMethod = bean.getClass().getMethod(methodName, parameterClass);
            if (parameterClass.isEnum()) {
                // set enum value if it's value
                if (EnumUtils.isValidEnumIgnoreCase((Class<Enum>) parameterClass, value)) {
                    setMethod.invoke(bean, EnumUtils.getEnumIgnoreCase((Class<Enum>) parameterClass, value));
                } else {
                    log.warn("Can't set enum '{}' to '{}' as it isn't a valid option", parameterClass.getName(), value);
                }
            } else {
                log.error("Dont know how to set parameter '{}' to '{}' in bean '{}'", parameter, value, bean.getClass().getName());
            }
        } catch (Exception e) {
            log.warn("Error setting property {} to '{}'", parameter, value);
        }
    }


}
