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
 * @param timestamp      The time the event was created
 * @param stepNumber     The simulation step this phase ran within
 * @param phaseName      The name of the phase being timed
 * @param duration       The time spent in this phase, in nanoseconds
 */
public record StepProfileEvent(String simulationName,
                                String sessionId,
                                Instant timestamp,
                                long stepNumber,
                                String phaseName,
                                long duration) implements TelemetryEvent {

  @Override
  public String description() {
    return "Step " + stepNumber + " phase '" + phaseName + "' took "
        + Duration.ofNanos(duration).toMillis() + "ms";
  }

  // common phases as constants

  public final static String PHASE_STEP = "step";
  public final static String PHASE_RENDER = "render";
  public final static String PHASE_AGENT_ASK = "agent_ask";
  public final static String PHASE_AGENT_REPORT = "agent_report";
}
