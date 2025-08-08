/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

/**
 * Represents a configurable parameter for a simulation.
 * <p>
 * This record encapsulates the metadata for a single simulation parameter, including its name,
 * human-readable description, and expected data type. These parameters are used by the UI and CLI
 * to dynamically generate configuration interfaces and validate user input.
 * </p>
 * <p>
 * Parameters defined through this class can be automatically exposed in:
 * </p>
 * <ul>
 *   <li>JavaFX UI parameter forms</li>
 *   <li>Command-line argument parsing</li>
 *   <li>Configuration file loading</li>
 * </ul>
 *
 * @param name The internal name of the parameter (should match the getter/setter method names)
 * @param description Human-readable description shown in the UI and help text
 * @param parameterType The Java class type expected for this parameter's value
 *
 * @see SimulationBuilder#getParameters()
 */
public record SimulationParameter(String name, String description, Class<?> parameterType) {

}
