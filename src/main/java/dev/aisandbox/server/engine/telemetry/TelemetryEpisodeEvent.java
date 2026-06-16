/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

import dev.aisandbox.server.engine.telemetry.event.*;

/**
 * Interface for telemetry events that are reporting on part of an episode.
 */
public sealed interface TelemetryEpisodeEvent extends TelemetryEvent
        permits EpisodeAgentScoreEvent, EpisodeAgentRankEvent,
        EpisodeAgentWinLossEvent, EpisodeScoreEvent, EpisodeWinEvent {
    String episodeId();

    int episodeNumber();
}
