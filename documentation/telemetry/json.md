# Telemetry JSON Output Format

This document describes the JSON log format produced by the file-based telemetry module. The format is **JSON Lines (JSONL)**: one JSON object per line, terminated by `\n`, with no enclosing array and no commas between records. This is the de facto standard for structured log files and is consumed natively by Filebeat, Fluent Bit, Vector, Logstash, Promtail, and most other log shippers.

Field names follow the [Elastic Common Schema (ECS)](https://www.elastic.co/guide/en/ecs/current/index.html) where an appropriate ECS field exists. ECS is the schema used by Elasticsearch, OpenSearch, Kibana, and the Elastic Agent ecosystem; aligning with it means records can be ingested into those systems with minimal or zero custom mapping. Fields that have no natural ECS equivalent are placed under domain-specific namespaces (`simulation.*`, `player.*`, `episode.*`) to keep them clearly separated from ECS-governed fields.

> **Note for OpenTelemetry users:** this is the file-output format. A [separate telemetry module emits OTLP over HTTP](otel.md) for OpenTelemetry-based pipelines. The two formats are not byte-identical — each is optimised for its native ecosystem.

## Record structure

Records use nested JSON objects rather than dotted flat keys (i.e. `{"event": {"action": "..."}}` rather than `{"event.action": "..."}`). This matches ECS documentation conventions and is handled cleanly by all major log processors.

### Example record

```json
{
  "@timestamp": "2026-05-18T21:26:00.258677246Z",
  "message": "Agent 1 LOSE in episode f44c55e5",
  "log": { "level": "info" },
  "ecs": { "version": "8.11" },
  "event": {
    "action": "episode_agent_win_loss",
    "dataset": "aisandbox.episodes",
    "kind": "event",
    "category": ["process"]
  },
  "service": { "name": "AISandbox" },
  "simulation": { "name": "CoinGame", "result": "LOSE" },
  "session": { "id": "c84387b5-683f-4074-8376-4e62dcaef3b3" },
  "episode": { "id": "f44c55e5-ca50-4584-89db-344512215b62" },
  "player": { "name": "Agent 1" }
}
```

On disk, this record appears on a single line. Pretty-printing above is for readability only.

## Field reference

| Field | Type | ECS? | Cardinality | Description |
|---|---|---|---|---|
| `@timestamp` | string (RFC 3339 / ISO 8601, nanosecond precision, UTC) | ECS | n/a | Time the event occurred. Always UTC, always `Z`-suffixed. |
| `message` | string | ECS | n/a | Human-readable summary of the event. Used by default views in Kibana, Discover, and similar UIs. Redundant with structured fields by design. |
| `log.level` | string | ECS | low | Severity. One of `debug`, `info`, `warn`, `error`. Routine simulation events are `info`; failed or aborted episodes are `error`. |
| `ecs.version` | string | ECS | low | Version of the ECS schema this record targets. Allows ECS-aware processors to apply correct field mappings. |
| `event.action` | string | ECS | low | The specific action that occurred. Stable identifier suitable for filtering and grouping (e.g. `episode_agent_win_loss`, `episode_start`, `episode_abort`). |
| `event.dataset` | string | ECS | low | Namespaces events by source, conventionally `<service>.<source>`. Used by Kibana to auto-build dataset views. |
| `event.kind` | string | ECS | low | ECS taxonomy. Always `event` for this dataset (discrete things that happened). |
| `event.category` | array of strings | ECS | low | ECS taxonomy bucket. Set to `["process"]` to indicate discrete units of work. |
| `service.name` | string | ECS | low | Name of the application producing the telemetry. Identifies the *runner*, not the scenario being run. Constant across all simulations executed by the same application (e.g. `AISandbox`). |
| `simulation.name` | string | custom | low | Name of the scenario being simulated (e.g. `CoinGame`). One application may run many scenarios; this field distinguishes them. |
| `session.id` | string (UUID) | ECS | medium | Identifier for a single simulation run. All events from one run share this ID. |
| `episode.id` | string (UUID) | custom | **high** | Identifier for an individual episode within a session. New value per episode. |
| `player.name` | string | custom | low | Name of the in-simulation agent the event concerns. Deliberately *not* `agent.name`, which is reserved in ECS for the telemetry collection agent (Filebeat, etc.). |
| `simulation.result` | string | custom | low | Domain-specific outcome from the perspective of the `player` named in the same record. Values: `WIN`, `LOSE`, `DRAW`. Distinct from `event.outcome` (which would describe whether the episode itself ran correctly, not who won). |

### Cardinality notes

The cardinality column indicates how many distinct values a field is likely to take. This matters when configuring downstream systems:

- **Low-cardinality fields** are safe to use as labels, dimensions, or facets in any system.
- **Medium-cardinality fields** (one value per session) are fine as labels in Elasticsearch but may be too high-cardinality for Loki labels — use them in the log line, not as a stream label.
- **High-cardinality fields** (one value per episode) should never be used as Loki labels and should be indexed thoughtfully in Elasticsearch. They are searchable but not suitable for aggregation dimensions.

### Custom fields and ECS

The `simulation`, `episode`, and `player` namespaces are not part of ECS. They are domain-specific and use top-level nested objects rather than `labels.*` to keep them ergonomic for querying (`simulation.result:WIN` reads better than `labels.result:WIN`). ECS-strict environments should map these fields explicitly in their ingest pipeline; in practice, all major consumers accept additional namespaces without complaint.

## File format details

### Encoding

UTF-8, no BOM.

### Line termination

`\n` (LF) after every record, including the last one. CRLF is not used.

### File naming

Default pattern: `<service>-<simulation>-<session_id>.jsonl`, e.g. `aisandbox-coingame-c84387b5-683f-4074-8376-4e62dcaef3b3.jsonl`. One file per session.

### Rotation

Files are written append-only and are not modified in place after rotation. New sessions produce new files. Log shippers tailing the directory should use filename-based tailing (the default for Filebeat, Fluent Bit, and Vector) rather than inode-based.

### Compression

Files may be written with `.jsonl.gz` extension if compression is enabled. Gzip is supported transparently by all major log shippers. Typical compression ratio for this schema is 10-20x.

## Consuming the format

### Quick validation

```bash
# Validate every line is parseable JSON:
while IFS= read -r line; do echo "$line" | jq . > /dev/null || echo "BAD: $line"; done < aisandbox.jsonl

# Count WINs by player:
jq -r 'select(.simulation.result == "WIN") | .player.name' aisandbox.jsonl | sort | uniq -c
```

### Filebeat

No custom configuration needed beyond pointing at the file:

```yaml
filebeat.inputs:
  - type: filestream
    paths:
      - /var/log/aisandbox/*.jsonl
    parsers:
      - ndjson:
          target: ""
          overwrite_keys: true
          add_error_key: true
```

The `target: ""` setting tells Filebeat to merge parsed JSON fields into the root of the event, so ECS fields land where Elasticsearch expects them.

### Vector

```toml
[sources.aisandbox]
type = "file"
include = ["/var/log/aisandbox/*.jsonl"]

[transforms.parse]
type = "remap"
inputs = ["aisandbox"]
source = '. = parse_json!(.message)'
```

### Fluent Bit

```ini
[INPUT]
    Name        tail
    Path        /var/log/aisandbox/*.jsonl
    Parser      json
    Tag         aisandbox.*
```

## Versioning

Schema changes follow these rules:

- **Adding fields** is non-breaking and may happen in any release.
- **Removing or renaming fields** is breaking and will be reflected in `ecs.version` and the module's own version number.
- **Changing the type of an existing field** is breaking and will be avoided where possible.

Consumers should ignore unknown fields rather than failing on them.