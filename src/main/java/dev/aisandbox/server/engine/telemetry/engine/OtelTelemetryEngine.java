/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry.engine;

import dev.aisandbox.server.engine.SimulationVersion;
import dev.aisandbox.server.engine.telemetry.TelemetryEngine;
import dev.aisandbox.server.engine.telemetry.TelemetryEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentDoubleScoreEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentDoubleScoreEvent.AgentDoubleScore;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentLongScoreEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentLongScoreEvent.AgentLongScore;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentRankEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentRankEvent.AgentRank;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentWinLossEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentWinLossEvent.AgentResult;
import dev.aisandbox.server.engine.telemetry.event.EpisodeDoubleScoreEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeLongScoreEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeWinEvent;
import dev.aisandbox.server.engine.telemetry.event.SessionFailureEvent;
import dev.aisandbox.server.engine.telemetry.event.SessionStartEvent;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
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
    log.info("Setting up connection to {}",collectorUrl);
    exporter = OtlpHttpLogRecordExporter.builder()
        .setEndpoint(collectorUrl)
        // .addHeader("Authorization", "Bearer <token>")
        .build();

    Resource resource = Resource.getDefault().merge(
        Resource.create(Attributes.builder()
            .put("service.name", "AISandbox")
            .put("service.version", SimulationVersion.get())
            .build()));

    loggerProvider = SdkLoggerProvider.builder()
        .setResource(resource)
        .addLogRecordProcessor(BatchLogRecordProcessor.builder(exporter).build())
        .build();

    openTelemetry = OpenTelemetrySdk.builder()
        .setLoggerProvider(loggerProvider)
        .build();

    logger = openTelemetry.getLogsBridge().loggerBuilder("aisandbox.telemetry")
        .setInstrumentationVersion(SimulationVersion.get())
        .build();
  }

  private LogRecordBuilder createCommon(TelemetryEvent event) {
    return logger.logRecordBuilder()
        .setTimestamp(event.timestamp())
        .setBody(event.description())
        .setSeverity(Severity.INFO)
        .setSeverityText("INFO")
        .setEventName(event.getClass().getSimpleName())
        .setAttribute("simulation.name", event.simulationName())
        .setAttribute("simulation.session.id", event.sessionID())
        .setAttribute("simulation.episode.id", event.episodeID())
        .setBody(event.description());
  }

  @Override
  public void initialise(String sessionID) {
  }

  @Override
  public void writeTelemetryEvent(TelemetryEvent event) {
    switch (event) {
      case SessionStartEvent ignored -> createCommon(event).emit();
      case SessionFailureEvent ignored -> createCommon(event)
          .setSeverity(Severity.ERROR)
          .setSeverityText("ERROR")
          .emit();
      case EpisodeWinEvent win -> createCommon(event)
          .setAttribute("simulation.win", win.win())
          .emit();
      case EpisodeDoubleScoreEvent score -> createCommon(event)
          .setAttribute("simulation.score", score.score())
          .emit();
      case EpisodeLongScoreEvent score -> createCommon(event)
          .setAttribute("simulation.score", score.score())
          .emit();
      case EpisodeAgentDoubleScoreEvent agentScores -> {
        for (AgentDoubleScore agentScore : agentScores.agentScoreList()) {
          createCommon(event)
              .setAttribute("simulation.agent.name", agentScore.agentName())
              .setAttribute("simulation.score", agentScore.score())
              .emit();
        }
      }
      case EpisodeAgentLongScoreEvent agentScores -> {
        for (AgentLongScore agentScore : agentScores.agentScoreList()) {
          createCommon(event)
              .setAttribute("simulation.agent.name", agentScore.agentName())
              .setAttribute("simulation.score", agentScore.score())
              .emit();
        }
      }
      case EpisodeAgentRankEvent agentRanks -> {
        for (AgentRank agentRank : agentRanks.agentRankList()) {
          createCommon(event)
              .setAttribute("simulation.agent.name", agentRank.agentName())
              .setAttribute("simulation.rank", agentRank.rank())
              .emit();
        }
      }
      case EpisodeAgentWinLossEvent agentWin -> {
        for (AgentResult agentResult : agentWin.agentResultList()) {
          createCommon(event)
              .setBody(agentResult.description())
              .setAttribute("simulation.agent.name", agentResult.agentName())
              .setAttribute("simulation.result", agentResult.result().name())
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
}