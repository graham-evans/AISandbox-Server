/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

import dev.aisandbox.server.engine.telemetry.EpisodeAgentDoubleScoreEvent.AgentDoubleScore;
import dev.aisandbox.server.engine.telemetry.EpisodeAgentLongScoreEvent.AgentLongScore;
import dev.aisandbox.server.engine.telemetry.EpisodeAgentRankEvent.AgentRank;
import dev.aisandbox.server.engine.telemetry.EpisodeAgentWinLossEvent.AgentResult;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Telemetry engine that writes events as JSON to a session-specific file.
 */
@Slf4j
@RequiredArgsConstructor
public class FileTelemetryEngine implements TelemetryEngine {

  private final File directory;
  private BufferedWriter writer;

  @Override
  public void initialise(String sessionID) {
    File outputFile = new File(directory, sessionID + ".json");
    try {
      writer = new BufferedWriter(new FileWriter(outputFile));
    } catch (IOException e) {
      log.error("Failed to open telemetry file {}", outputFile, e);
    }
  }

  @Override
  public void writeTelemetryEvent(TelemetryEvent event) {
    // check for broken writer
    if (writer == null) {
      return;
    }
  }



  @Override
  public void close() {
    if (writer == null) {
      return;
    }
    try {
      writer.close();
    } catch (IOException e) {
      log.error("Failed to close telemetry file", e);
    }
  }

  private List<String> format(TelemetryEvent event) {
    return switch (event) {
      case EpisodeDoubleScoreEvent e -> List.of(
          common(e, "episode_double_score") + ",\"score\":" + e.score() + "}");
      case EpisodeLongScoreEvent e -> List.of(
          common(e, "episode_long_score") + ",\"score\":" + e.score() + "}");
      case EpisodeWinEvent e -> List.of(
          common(e, "episode_win") + ",\"win\":" + e.win() + "}");
      case SessionFailureEvent e -> List.of(
          common(e, "episode_failure") + "}");
      case EpisodeAgentDoubleScoreEvent e -> {
        List<String> lines = new ArrayList<>(e.agentScoreList().size());
        String prefix = common(e, "episode_agent_double_score");
        for (AgentDoubleScore agent : e.agentScoreList()) {
          lines.add(prefix + ",\"labels.agent_name\":\"" + agent.agentName()
              + "\",\"score\":" + agent.score() + "}");
        }
        yield lines;
      }
      case EpisodeAgentLongScoreEvent e -> {
        List<String> lines = new ArrayList<>(e.agentScoreList().size());
        String prefix = common(e, "episode_agent_long_score");
        for (AgentLongScore agent : e.agentScoreList()) {
          lines.add(prefix + ",\"labels.agent_name\":\"" + agent.agentName()
              + "\",\"score\":" + agent.score() + "}");
        }
        yield lines;
      }
      case EpisodeAgentRankEvent e -> {
        List<String> lines = new ArrayList<>(e.agentRankList().size());
        String prefix = common(e, "episode_agent_rank");
        for (AgentRank agent : e.agentRankList()) {
          lines.add(prefix + ",\"labels.agent_name\":\"" + agent.agentName()
              + "\",\"rank\":" + agent.rank() + "}");
        }
        yield lines;
      }
      case EpisodeAgentWinLossEvent e -> {
        List<String> lines = new ArrayList<>(e.agentResultList().size());
        String prefix = common(e, "episode_agent_win_loss");
        for (AgentResult agent : e.agentResultList()) {
          lines.add(prefix + ",\"labels.agent_name\":\"" + agent.agentName()
              + "\",\"result\":\"" + agent.result().name() + "\"}");
        }
        yield lines;
      }
    };
  }

  private String common(TelemetryEvent event, String eventAction) {
    return "{\"@timestamp\":\"" + event.timestamp().toString()
        + "\",\"log.level\":\"info\""
        + ",\"event.action\":\"" + eventAction + "\""
        + ",\"service.name\":\"" + event.simulationName() + "\""
        + ",\"session_id\":\"" + event.sessionID() + "\""
        + ",\"episode_id\":\"" + event.episodeID() + "\"";
  }
}
