/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
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

  private static final String jsonTemplate = "{\"@timestamp\":\"%s\",\"log.level\":\"info\",\"event.action\":\"episode_agent_win_loss\",\"service.name\":\"%s\",\"session_id\":\"%s\",\"episode_id\":\"%s\",\"labels.agent_name\":\"%s\",\"result\":\"%s\"}";

  @Override
  public List<String> toJSON() {
    return agentResultList.stream()
        .map(a -> String.format(jsonTemplate, timestamp.toString(), simulationName,
            sessionID, episodeID, a.agentName(), a.result().name()))
        .toList();
  }

  @Override
  public void emit(Logger logger) {
    for (AgentResult agent : agentResultList) {
      logger.logRecordBuilder()
          .setBody("Episode Agent Win/Loss")
          .setSeverity(Severity.INFO)
          .setAttribute("simulation_name", simulationName)
          .setAttribute("session_id", sessionID)
          .setAttribute("episode_id", episodeID)
          .setAttribute("agent_name", agent.agentName())
          .setAttribute("result", agent.result().name())
          .setTimestamp(timestamp)
          .emit();
    }
  }

  public enum Result {
    WIN,
    LOSE,
    DRAW
  }

  public record AgentResult(String agentName, Result result) {

  }
}