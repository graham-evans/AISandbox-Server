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
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * Telemetry engine that forwards events to an OpenTelemetry collector via OTLP/HTTP.
 */
@Slf4j
public class OtelTelemetryEngine implements TelemetryEngine {

  private final OtlpHttpLogRecordExporter exporter;
  private final OpenTelemetrySdk openTelemetry;
  private final SdkLoggerProvider loggerProvider;
  private final Logger logger;

  public OtelTelemetryEngine(String collectorUrl) {
    exporter = OtlpHttpLogRecordExporter.builder()
        .setEndpoint(collectorUrl)
        // .addHeader("Authorization", "Bearer <token>")
        .build();

    loggerProvider = SdkLoggerProvider.builder()
        .addLogRecordProcessor(BatchLogRecordProcessor.builder(exporter).build())
        .build();

    openTelemetry = OpenTelemetrySdk.builder()
        .setLoggerProvider(loggerProvider)
        .build();

    logger = openTelemetry.getLogsBridge().get("dev.aisandbox.server");
  }

  @Override
  public void initialise(String sessionID) {
  }

  @Override
  public void writeTelemetryEvent(TelemetryEvent event) {
    switch (event) {
      case EpisodeDoubleScoreEvent e -> base(e, "Episode Double Score")
          .setAttribute("score", String.valueOf(e.score()))
          .emit();
      case EpisodeLongScoreEvent e -> base(e, "Episode Long Score")
          .setAttribute("score", String.valueOf(e.score()))
          .emit();
      case EpisodeWinEvent e -> base(e, "Episode Win")
          .setAttribute("win", String.valueOf(e.win()))
          .emit();
      case SessionFailureEvent e -> base(e, "Episode Failure").emit();
      case EpisodeAgentDoubleScoreEvent e -> {
        for (AgentDoubleScore agent : e.agentScoreList()) {
          base(e, "Episode Agent Double Score")
              .setAttribute("agent_name", agent.agentName())
              .setAttribute("score", String.valueOf(agent.score()))
              .emit();
        }
      }
      case EpisodeAgentLongScoreEvent e -> {
        for (AgentLongScore agent : e.agentScoreList()) {
          base(e, "Episode Agent Long Score")
              .setAttribute("agent_name", agent.agentName())
              .setAttribute("score", String.valueOf(agent.score()))
              .emit();
        }
      }
      case EpisodeAgentRankEvent e -> {
        for (AgentRank agent : e.agentRankList()) {
          base(e, "Episode Agent Rank")
              .setAttribute("agent_name", agent.agentName())
              .setAttribute("rank", String.valueOf(agent.rank()))
              .emit();
        }
      }
      case EpisodeAgentWinLossEvent e -> {
        for (AgentResult agent : e.agentResultList()) {
          base(e, "Episode Agent Win/Loss")
              .setAttribute("agent_name", agent.agentName())
              .setAttribute("result", agent.result().name())
              .emit();
        }
      }
    }
  }

  @Override
  public void close() {
    loggerProvider.forceFlush().join(10, java.util.concurrent.TimeUnit.SECONDS);
    openTelemetry.close();
    exporter.close();
  }

  private LogRecordBuilder base(TelemetryEvent event, String body) {
    return logger.logRecordBuilder()
        .setBody(body)
        .setSeverity(Severity.INFO)
        .setAttribute("simulation_name", event.simulationName())
        .setAttribute("session_id", event.sessionID())
        .setAttribute("episode_id", event.episodeID())
        .setTimestamp(event.timestamp());
  }
}
