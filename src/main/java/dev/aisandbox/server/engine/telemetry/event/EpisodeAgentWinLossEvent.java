/*
 *
 *  * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 *  * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 *  * more information.
 *
 */

/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry.event;

import dev.aisandbox.server.engine.telemetry.TelemetryEvent;
import java.time.Instant;
import java.util.List;

/**
 * Telemetry event to denote a simulation episode completing with a win, loss, or draw per agent.
 *
 * @param simulationName      The name of the simulation
 * @param sessionID           The session identifier
 * @param episodeID           The episode identifier
 * @param timestamp           The time the event was created
 * @param agentResultList     The list of agents and their results
 */
public record EpisodeAgentWinLossEvent(String simulationName,
                                       String sessionID,
                                       String episodeID,
                                       Instant timestamp,
                                       List<AgentResult> agentResultList) implements
    TelemetryEvent {

  public enum Result {
    WIN,
    LOSE,
    DRAW
  }

  public record AgentResult(String agentName, Result result) {

    public String description() {
      return "Agent " + agentName + " " + result.name();
    }
  }
}
