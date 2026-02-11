/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.launcher.options;

import java.util.List;
import lombok.Builder;
import lombok.Singular;

/**
 * Runtime options for the AI Sandbox Server.
 *
 * <p>Immutable record containing all configuration options for server startup, including
 * simulation selection, output settings, agent configuration, and network parameters.
 */
@Builder
public record RuntimeOptions(boolean help, // has the user asked for help
                             String simulation, // the name of the simulation
                             boolean outputImage, // output to images
                             String outputDirectory, // output dir (if images)
                             int skip, // frames to skip when rendering
                             int agents, // number of agents
                             boolean openExternal, // allow external connections
                             @Singular List<String> parameters, // extra parameters
                             long maxStepCount, int startPort) {

}
