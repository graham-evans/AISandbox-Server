/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry.event;

import dev.aisandbox.server.engine.telemetry.TelemetryEpisodeEvent;

import java.time.Instant;

/**
 * Telemetry event to denote a simulation episode completing with a long integer score.
 *
 * @param simulationName The name of the simulation
 * @param sessionId      The session identifier
 * @param episodeId      The episode identifier
 * @param episodeNumber  The number of the episode in this run
 * @param timestamp      The time the event was created
 * @param score          The score for the episode
 */
public record EpisodeLongScoreEvent(String simulationName,
                                    String sessionId,
                                    String episodeId,
                                    int episodeNumber,
                                    Instant timestamp,
                                    long score) implements TelemetryEpisodeEvent {
    @Override
    public String description() {
        return "Episode ends with score " + score;
    }
}
