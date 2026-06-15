package dev.aisandbox.server.engine.telemetry;

public interface TelemetryEpisodeEvent extends TelemetryEvent {
    String episodeId();
    int episodeNumber();
}
