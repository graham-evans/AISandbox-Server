/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry.event;

import dev.aisandbox.server.engine.telemetry.TelemetryEvent;
import java.time.Duration;
import java.time.Instant;

/**
 * Telemetry event to record how long a named phase of a simulation step took to execute.
 *
 * @param simulationName The name of the simulation
 * @param sessionId      The session identifier
 * @param stepNumber     The simulation step this phase ran within
 * @param phaseName      The name of the phase being timed
 * @param startTime      The time the phase started
 * @param stopTime       The time the phase completed
 */
public record StepProfileEvent(String simulationName,
                                String sessionId,
                                long stepNumber,
                                String phaseName,
                                Instant startTime,
                                Instant stopTime) implements TelemetryEvent {

  @Override
  public Instant timestamp() {
    return stopTime;
  }

  public long durationMillis() {
    return Duration.between(startTime, stopTime).toMillis();
  }

  @Override
  public String description() {
    return "Step " + stepNumber + " phase '" + phaseName + "' took " + durationMillis() + "ms";
  }

  // common phases as constants

  public final static String PHASE_STEP = "step";
  public final static String PHASE_RENDER = "render";
  public final static String PHASE_AGENT_WAIT = "agent_wait";
}
