# Telemetry

AISandbox-Server emits structured telemetry about every simulation it runs. Each event records *what happened* (an episode started, an agent won, a run failed) along with enough identifying context (session, episode, agent, simulation name) to correlate that event across an entire run or across many runs. The intent is that any external observability stack — log search, dashboards, training-data extraction — can consume these events without bespoke parsing.

## Output formats

Two telemetry implementations ship with the server. They emit the same underlying events but differ in transport, schema, and the ecosystem they slot into. They can be used independently or together.

| Aspect | [**JSONL file output**](json.md) | [**OTLP/HTTP stream**](otel.md) |
|---|---|---|
| Transport | Append-only file on disk | HTTP POST to an OTLP receiver |
| Schema | [Elastic Common Schema (ECS)](https://www.elastic.co/guide/en/ecs/current/index.html) | [OpenTelemetry Semantic Conventions](https://opentelemetry.io/docs/specs/semconv/) |
| Encoding | UTF-8 JSON Lines (`.jsonl`) | Protobuf or JSON over HTTP |
| Typical consumers | Filebeat, Fluent Bit, Vector, Logstash, Promtail | OTel Collector, Datadog, Honeycomb, Grafana Cloud, New Relic, SigNoz |
| Best when | Logs land on disk first; offline analysis; air-gapped runs | A collector or vendor backend already exists; live streaming |

The two formats are **not byte-identical**. Field names and nesting differ because each follows the conventions of its own ecosystem (for example, the in-simulation agent is `player.name` in ECS but `simulation.agent.name` in OTel, to avoid colliding with each schema's reserved `agent.*` namespace). Pick whichever matches the pipeline you already run — there is no need to align them.

## Event model

Every event carries the same core context: a `session.id` (one per run of the server), a `simulation.name` (which scenario), an `episode.id` (one per episode within a session), and where applicable a `player` / `simulation.agent.name`. Events are emitted at episode boundaries — episode start, episode outcome (win/loss/draw, rank, or score), and episode failure — rather than at every simulation step. This keeps record volume proportional to gameplay rather than to tick rate.

Both implementations share the same set of event classes, defined under `dev.aisandbox.server.engine.telemetry`:

- `EpisodeWinEvent` — an episode finished with a clear winner
- `EpisodeAgentWinLossEvent` — per-agent WIN / LOSE / DRAW outcome
- `EpisodeAgentRankEvent` — per-agent finishing rank in a multi-agent episode
- `EpisodeAgentLongScoreEvent` / `EpisodeAgentDoubleScoreEvent` — per-agent numeric score
- `EpisodeLongScoreEvent` / `EpisodeDoubleScoreEvent` — episode-level numeric score
- `EpisodeFailureEvent` — the episode aborted or failed to complete

The `TelemetryEngine` interface is the integration point: simulations call into it and the configured engine (`FileTelemetryEngine`, `OtelTelemetryEngine`, or `NullTelemetryEngine` to disable) decides how the event is serialised and delivered.

## Choosing a format

- If you already operate Elasticsearch, OpenSearch, or any Beats/Vector/Fluent Bit pipeline, use the **[JSONL file output](json.md)**. The ECS field names will map straight in with no custom ingest configuration.
- If you already operate an OpenTelemetry Collector, or send telemetry to a vendor backend that speaks OTLP, use the **[OTLP/HTTP stream](otel.md)**. It plugs in without an intermediate file-tailing step.
- If neither applies, the JSONL output is the simpler starting point: it requires no network configuration and the resulting files are trivially inspectable with `jq` or a text editor.

Both implementations are documented in full, including field references, cardinality guidance, and worked examples for the most common consumers:

- [JSON Lines format reference](json.md)
- [OTLP/HTTP format reference](otel.md)
