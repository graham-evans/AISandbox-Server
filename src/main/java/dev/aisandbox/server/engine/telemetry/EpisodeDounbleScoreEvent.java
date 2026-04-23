/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

public record EpisodeDounbleScoreEvent(String simulationName,
                                       String sessionID,
                                       String episodeID,
                                       double score) implements TelemetryEvent {
}
