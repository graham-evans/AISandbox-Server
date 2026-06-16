/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry.event;

import dev.aisandbox.server.engine.telemetry.TelemetryEvent;

import java.time.Instant;

/**
 * Telemetry event to denote a single agent failing to complete a task.
 *
 * @param simulationName The name of the simulation
 * @param sessionId      The session identifier
 * @param timestamp      The time the event was created
 * @param reason         The reason the simulation terminated
 */
public record SessionFailureEvent(String simulationName,
                                  String sessionId,
                                  Instant timestamp,
                                  String reason) implements TelemetryEvent {
    @Override
    public String description() {
        return "Simulation failure - " + reason;
    }
}
