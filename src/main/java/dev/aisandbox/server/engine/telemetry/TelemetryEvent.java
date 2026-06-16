/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

import dev.aisandbox.server.engine.telemetry.event.SessionFailureEvent;
import dev.aisandbox.server.engine.telemetry.event.SessionStartEvent;
import java.time.Instant;

/**
 * Base telemetry interface, defines fields that all events must implement.
 */
public sealed interface TelemetryEvent permits SessionStartEvent, SessionFailureEvent, TelemetryEpisodeEvent {

  // define the common fields, so they can be accessed without casting the event.

  String simulationName();

  String sessionId();

  Instant timestamp();

  String description();

  default String eventName() {
    return getClass().getSimpleName();
  }
}
