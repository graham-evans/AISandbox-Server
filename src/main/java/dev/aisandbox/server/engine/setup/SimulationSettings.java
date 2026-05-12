/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.setup;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.SimulationRandomNumberGenerator;
import dev.aisandbox.server.engine.SimulationRunner;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.exception.SimulationSetupException;
import dev.aisandbox.server.engine.network.NetworkAgent;
import dev.aisandbox.server.engine.output.FXRenderer;
import dev.aisandbox.server.engine.output.NullOutputRenderer;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.telemetry.NullTelemetryEngine;
import dev.aisandbox.server.engine.telemetry.TelemetryEngine;
import dev.aisandbox.server.fx.RuntimeController;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Class that represents the requested settings for a simulation.
 *
 * <p>This is based on the Builder pattern, with a few differences:</p>
 * <ol>
 *     <li>fields are exposed as fluent properties without chaining so they can be linked into a JavaFX UI as well as updated with CLI flags.</li>
 *     <li>Calling build with a RuntimeController as an attribute will create a FXRenderer and use this, ignoring the renderer settings.</li>
 *     <li>SimulationSettings.build creates a SimulationRunner</li>
 * </ol>
 */
@Slf4j
@Accessors(fluent = true, chain = false)
@Getter
public class SimulationSettings {

  // The selected simulation to build - this simulation may already be customised
  private final ObjectProperty<SimulationBuilder> selectedSimulationBuilder =
      new SimpleObjectProperty<>();
  // The number of network agents to use
  private final IntegerProperty agentCount = new SimpleIntegerProperty(1);
  // The starting port to use
  private final IntegerProperty defaultPort = new SimpleIntegerProperty(9000);
  // The colour Scheme
  private final ObjectProperty<Theme> selectedTheme = new SimpleObjectProperty<>(Theme.LIGHT);
  // allow connections from external computers
  private final BooleanProperty externalNetwork = new SimpleBooleanProperty(false);
  // allow overwriting agent names
  private final List<String> agentNameList = new ArrayList<>();
  // allow maximum steps (-1 = infinite)
  private final LongProperty maxStepCount = new SimpleLongProperty(-1);
  // telemetry types - these will be linked in the constructor
  private final BooleanProperty selectedTelemetryNone = new SimpleBooleanProperty(true);
  private final BooleanProperty selectedTelemetryJon = new SimpleBooleanProperty(false);
  private final BooleanProperty selectedTelemetryOtel = new SimpleBooleanProperty(false);
  // output types - linked in constructor
  private final BooleanProperty outputNone = new SimpleBooleanProperty(false);
  private final BooleanProperty outputScreen = new SimpleBooleanProperty(true);
  private final BooleanProperty outputPNG = new SimpleBooleanProperty(false);
  // extra output information
  private final StringProperty outputPNGPath = new SimpleStringProperty(".");
  private final IntegerProperty outputSkipFrames = new SimpleIntegerProperty(-1);

  public SimulationSettings() {
    // link mutually exclusive
    linkMutuallyExclusive(selectedTelemetryNone, selectedTelemetryJon, selectedTelemetryOtel);
    linkMutuallyExclusive(outputNone, outputPNG, outputScreen);
  }

  private static void linkMutuallyExclusive(BooleanProperty... properties) {
    for (BooleanProperty prop : properties) {
      prop.addListener((obs, oldVal, newVal) -> {
        if (newVal) {
          for (BooleanProperty other : properties) {
            if (other != prop) {
              other.set(false);
            }
          }
        }
      });
    }
  }

  /**
   * Create a SimulationRunner based on the currently held settings, overriding the renderer with a
   * FXRenderer based on the runtimeController.
   *
   * <p>This is used by the FX launcher.
   *
   * @param runtimeController The existing FX runtime controller, which can be used to setup the
   *                          FXRenderer.
   * @return The simulation runner to control the simulation
   * @throws SimulationSetupException an error occurred while setting up the simulation.
   */
  public SimulationRunner build(RuntimeController runtimeController)
      throws SimulationSetupException {
    // generate renderer based on FX and ignore current settings
    OutputRenderer renderer = new FXRenderer(runtimeController);
    // generate agents
    List<Agent> agentList = createAgents(renderer, List.of());
    // return simulation based on these renderer and agents
    return build(renderer, agentList);
  }

  /**
   * Create a SimulationRunner based on the current settings.
   *
   * <p>This is used by the CLI launcher
   *
   * @return The simulation runner to control the simulation
   * @throws SimulationSetupException an error occurred while setting up the simulation.
   */
  public SimulationRunner build() throws SimulationSetupException {
    return build(createRenderer(), List.of());
  }

  /**
   * Create a SimulationRunner based on the current setting but forcing the renderer to be used.
   *
   * <p>This is used by the JavaFX launcher
   *
   * @param renderer the renderer to use (overrides the class choice).
   * @return The simulation runner to control the simulation
   * @throws SimulationSetupException an error occurred while setting up the simulation.
   */
  public SimulationRunner build(OutputRenderer renderer) throws SimulationSetupException {
    return build(renderer, List.of());
  }

  /**
   * Create a SimulationRunner based on the current settings but skipping the creation of agents
   * (using the supplied ones instead).
   *
   * <p>This is used by the testing methods.
   *
   * @return The simulation runner to control the simulation
   * @throws SimulationSetupException an error occurred while setting up the simulation.
   */
  public SimulationRunner build(List<Agent> agentList) throws SimulationSetupException {
    return build(createRenderer(), agentList);
  }

  /**
   * Create a SimulationRunner based on the given renderer and prebuilt agents.
   *
   * <p>This is the main method that all versions calls
   *
   * @param renderer the renderer to use
   * @param prebuiltAgents a list of prebuilt agents
   * @return the SimulationRunner plumbed into the renderer / agents / telemetry etc.
   * @throws SimulationSetupException when there is an error setting up the simulation.
   */
  private SimulationRunner build(OutputRenderer renderer, List<Agent> prebuiltAgents)
      throws SimulationSetupException {
    // create the list of agents
    List<Agent> agentList = createAgents(renderer, prebuiltAgents);
    // generate the telemetry
    TelemetryEngine telemetryEngine = new NullTelemetryEngine();
    // create simulation
    Simulation sim = selectedSimulationBuilder.get()
        .build(agentList, selectedTheme.get(), createRandom(), new NullTelemetryEngine());
    // start output
    renderer.setup(sim);
    // create simulation runner thread
    return new SimulationRunner(sim, renderer, agentList, maxStepCount.get(), telemetryEngine);
  }

  public TelemetryEngine createTelemetryEngine() {
    return new NullTelemetryEngine();
  }

  public OutputRenderer createRenderer() {
    return new NullOutputRenderer();
  }

  public SimulationRandomNumberGenerator createRandom() {
    // TODO get seed from settings
    long randomSeed = System.currentTimeMillis();
    return new SimulationRandomNumberGenerator(randomSeed);
  }

  private List<Agent> createAgents(OutputRenderer renderer, List<Agent> prebuiltAgents)
      throws SimulationSetupException {
    log.info("Creating {} agents, starting at network port {}", agentCount.get(),
        defaultPort.get());
    // get next port to use
    AtomicInteger port = new AtomicInteger(defaultPort.get());
    // create default agent names
    String[] agentNames = selectedSimulationBuilder.get().getAgentNames(agentCount.get());
    // create the list of agents
    List<Agent> agentList = new ArrayList<>();
    // add any prebuilt ones
    agentList.addAll(prebuiltAgents);
    log.info("Added {} prebuilt agents", prebuiltAgents.size());
    // remove extras
    while (agentList.size() > agentCount.get()) {
      agentList.removeLast();
    }
    // add missing agents
    while (agentList.size() < agentCount.get()) {
      int agentNumber = agentList.size();
      log.info("Adding network agent based on port {}", port.get());
      agentList.add(
          new NetworkAgent(agentNames[agentNumber], port.getAndIncrement(), externalNetwork.get(),
              renderer));
    }
    return agentList;
  }


}
