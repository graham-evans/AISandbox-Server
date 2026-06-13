/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry.event;

import dev.aisandbox.server.engine.telemetry.TelemetryEvent;
import java.time.Instant;

/**
 * Telemetry event to denote a simulation episode completing with a double precision score.
 *
 * @param simulationName      The name of the simulation
 * @param sessionID           The session identifier
 * @param episodeID           The episode identifier
 * @param timestamp The time the event was created
 * @param score               The score for the episode
 */
public record EpisodeDoubleScoreEvent(String simulationName,
                                      String sessionID,
                                      String episodeID,
                                      Instant timestamp,
                                      double score) implements TelemetryEvent {

    @Override
    public String description() {
        return "Episode ends with score " + score;
    }
}
