package dev.aisandbox.server.options;

import dev.aisandbox.server.engine.SimulationBuilder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.EnumUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    /***
     * List all the enum parameters exposed by a SimulationBuilder.
     *
     * @param simulationBuilder the builder to return information about.
     * @return A list of ParameterEnumInfo records.
     */
    public static List<ParameterEnumInfo> listEnumParameters(SimulationBuilder simulationBuilder) {
        try {
            Map<String, String> props = BeanUtils.describe(simulationBuilder);
            List<ParameterEnumInfo> enums = new ArrayList<>();
            for (Map.Entry<String, String> entry : props.entrySet()) {
                Class<?> pType = PropertyUtils.getPropertyType(simulationBuilder, entry.getKey());
                if (pType.isEnum()) {
                    ParameterEnumInfo p = new ParameterEnumInfo(entry.getKey(), entry.getValue(), Arrays.stream(pType.getEnumConstants()).map(Object::toString).toList() );
                    enums.add(p);
                }
            }
            return enums;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Error getting parameter information from {}",simulationBuilder.getClass().getName());
            return List.of();
        }
    }

    /***
     * Set an Enum parameter on a SimulationBuilder
     * @param builder the SimulationBuilder being configured
     * @param key the parameter to update
     * @param value the text value to create a new enumeration object from
     */
    public static void setEnumParameter(SimulationBuilder builder, String key, String value) {
        BeanUtilsBean beanUtilsBean = new BeanUtilsBean(new ConvertUtilsBean() {
            @Override
            public Object convert(String value, Class clazz) {
                if (clazz.isEnum()) {
                    @SuppressWarnings("unchecked")
                    Object e = EnumUtils.getEnumIgnoreCase(clazz, value);
                    if (e==null) {
                        log.warn("Can't set enum constant {} to non existent value {}", key, value);
                        return clazz.getEnumConstants()[0];
                    } else {
                        return e;
                    }
                } else {
                    return super.convert(value, clazz);
                }
            }
        });
        try {
            beanUtilsBean.setProperty(builder, key, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("Error setting enum constant {} to {}", key, value);
        }
    }

}
