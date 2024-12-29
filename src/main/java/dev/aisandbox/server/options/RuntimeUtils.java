package dev.aisandbox.server.options;

import lombok.experimental.UtilityClass;
import org.apache.commons.cli.*;

import java.util.Arrays;

@UtilityClass
public class RuntimeUtils {

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
                workBuilder.output(RuntimeOptions.OutputOptions.PNG);
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

}
