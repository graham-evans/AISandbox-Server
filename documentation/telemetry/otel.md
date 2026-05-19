# Telemetry OTLP/HTTP Output Format

This document describes the OpenTelemetry telemetry stream produced by the HTTP-based telemetry module. The module emits log records over **OTLP/HTTP** — the standard OpenTelemetry Protocol transported over HTTP — directly to any OTLP-compatible receiver (the OpenTelemetry Collector, vendor backends such as Datadog, Honeycomb, Grafana Cloud, New Relic, Splunk, AWS CloudWatch's OTLP endpoint, or self-hosted systems like OpenObserve, Seq, and SigNoz).

Field names follow the [OpenTelemetry Semantic Conventions](https://opentelemetry.io/docs/specs/semconv/) where an appropriate convention exists. The data model itself is the [OpenTelemetry Logs Data Model](https://opentelemetry.io/docs/specs/otel/logs/data-model/), which has a small set of named top-level fields and an open attribute collection for everything else. Attributes that have no natural semantic convention use domain-specific namespaces (`simulation.*`, `simulation.episode.*`, `simulation.agent.*`).

> **Note for users of the [JSONL file output](json.md):** the file-based module produces ECS-aligned JSON Lines documented separately. Both modules carry the same underlying event data, but field naming and structure differ to suit each ecosystem. Choose the format that matches your ingestion pipeline.

## Transport

### Endpoint

Records are sent to the standard OTLP/HTTP logs endpoint:

```
POST {OTEL_EXPORTER_OTLP_ENDPOINT}/v1/logs
```

The default collector port for OTLP/HTTP is **4318**. A typical local development endpoint is `http://localhost:4318/v1/logs`.

### Protocol and encoding

Two encodings are supported, selectable via configuration:

- **`http/protobuf`** (default) — binary-encoded Protobuf payloads. `Content-Type: application/x-protobuf`. Smaller on the wire (~60–70% smaller than JSON), faster to serialise, schema-enforced. Recommended for production.
- **`http/json`** — JSON-encoded Protobuf payloads. `Content-Type: application/json`. Easier to inspect with standard HTTP debugging tools (`curl`, Wireshark, browser DevTools). Useful in development.

Both encodings carry the same Protobuf schema; only the wire representation differs. Receivers determine which decoder to use from the `Content-Type` header.

### Compression

`gzip` compression is supported and recommended for production. When enabled, the client sets `Content-Encoding: gzip` on the request. Compression is negotiable per request — the server is required to accept both compressed and uncompressed payloads.

### Authentication

Authentication is configured per deployment via HTTP headers. The OTLP specification does not mandate a particular auth scheme; the most common choices are:

- `Authorization: Bearer <token>` for bearer tokens (most vendor backends)
- `Authorization: Basic <base64>` for username/password
- Vendor-specific headers (e.g. `api-key: <key>`)

Headers are configured via `OTEL_EXPORTER_OTLP_HEADERS` or the equivalent SDK configuration.

### Delivery semantics

The transport implements OTLP's standard reliability guarantees: at-least-once delivery with exponential backoff and jitter on transient errors. On success the server returns HTTP 200 with an `ExportLogsServiceResponse` body. Partial success is signalled by a `partialSuccess` field in the response indicating how many records were rejected.

## Record structure

Each emitted event becomes one **LogRecord** in the OTLP data model. The model splits data across three levels:

- **Resource** — attributes that describe *what is producing* the telemetry. The application, host, deployment environment. Sent once per batch.
- **InstrumentationScope** — attributes that describe *what code is emitting* the records within the application (the telemetry module itself).
- **LogRecord** — the individual event, with its own timestamp, severity, optional body, and attribute collection.

This separation is the most important conceptual difference from a flat JSON log format: identifying information about the producer (`service.name`, `service.version`) is hoisted out of every record and attached to the enclosing batch.

### Example OTLP/JSON payload

A single batch carrying two log records (one episode, two agents):

```json
{
  "resourceLogs": [
    {
      "resource": {
        "attributes": [
          { "key": "service.name", "value": { "stringValue": "AISandbox" } },
          { "key": "service.version", "value": { "stringValue": "1.4.0" } },
          { "key": "service.instance.id", "value": { "stringValue": "host-42.pid-9001" } },
          { "key": "deployment.environment.name", "value": { "stringValue": "production" } }
        ]
      },
      "scopeLogs": [
        {
          "scope": {
            "name": "aisandbox.telemetry",
            "version": "1.4.0"
          },
          "logRecords": [
            {
              "timeUnixNano": "1747603560258677246",
              "observedTimeUnixNano": "1747603560258680000",
              "severityNumber": 9,
              "severityText": "INFO",
              "eventName": "simulation.episode.agent_win_loss",
              "body": { "stringValue": "Agent 1 LOSE in episode f44c55e5" },
              "attributes": [
                { "key": "simulation.name", "value": { "stringValue": "CoinGame" } },
                { "key": "simulation.session.id", "value": { "stringValue": "c84387b5-683f-4074-8376-4e62dcaef3b3" } },
                { "key": "simulation.episode.id", "value": { "stringValue": "f44c55e5-ca50-4584-89db-344512215b62" } },
                { "key": "simulation.agent.name", "value": { "stringValue": "Agent 1" } },
                { "key": "simulation.result", "value": { "stringValue": "LOSE" } }
              ]
            },
            {
              "timeUnixNano": "1747603560258677246",
              "observedTimeUnixNano": "1747603560258680000",
              "severityNumber": 9,
              "severityText": "INFO",
              "eventName": "simulation.episode.agent_win_loss",
              "body": { "stringValue": "Agent 2 WIN in episode f44c55e5" },
              "attributes": [
                { "key": "simulation.name", "value": { "stringValue": "CoinGame" } },
                { "key": "simulation.session.id", "value": { "stringValue": "c84387b5-683f-4074-8376-4e62dcaef3b3" } },
                { "key": "simulation.episode.id", "value": { "stringValue": "f44c55e5-ca50-4584-89db-344512215b62" } },
                { "key": "simulation.agent.name", "value": { "stringValue": "Agent 2" } },
                { "key": "simulation.result", "value": { "stringValue": "WIN" } }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

Production traffic uses the Protobuf encoding of this same structure.

## Field reference

### Resource attributes

Attached once per batch. These describe the application producing the telemetry.

| Attribute | Type | Convention | Description |
|---|---|---|---|
| `service.name` | string | OTel (required) | Logical name of the application. Identifies the *runner*, not the scenario being run. Set to `AISandbox`. Required by the OpenTelemetry Resource Semantic Conventions and is the single most important attribute — every observability backend uses it to group telemetry. |
| `service.version` | string | OTel | Version of the application emitting the telemetry. Useful for correlating telemetry shape changes with releases. |
| `service.instance.id` | string | OTel | Unique identifier for this running instance of the service. Allows distinguishing telemetry from parallel instances. |
| `deployment.environment.name` | string | OTel | Deployment environment (`production`, `staging`, `development`). Helps separate runs across environments in shared backends. |

### LogRecord top-level fields

These are defined by the OTel logs data model itself, not by attributes.

| Field | Type | Description |
|---|---|---|
| `timeUnixNano` | uint64 (nanoseconds since Unix epoch, as a JSON string) | Time the event occurred according to the application. |
| `observedTimeUnixNano` | uint64 | Time the telemetry pipeline observed the record. Equal to or slightly later than `timeUnixNano`. |
| `severityNumber` | int (1–24) | Numeric severity. `9` = `INFO`, `13` = `WARN`, `17` = `ERROR`. Routine events use `9`. |
| `severityText` | string | Human-readable severity. Matches `severityNumber`. |
| `eventName` | string | Identifies the event class. Stable identifier suitable for filtering and routing. Uses dotted hierarchical naming per OTel conventions: `simulation.episode.agent_win_loss`. |
| `body` | AnyValue | Human-readable description of the event. Populated for `application/json` ergonomics and UI display. Structured data lives in attributes, not in the body. |

### LogRecord attributes

Per-record attributes carrying the event payload.

| Attribute | Type | Cardinality | Description |
|---|---|---|---|
| `simulation.name` | string | low | Name of the scenario being simulated (e.g. `CoinGame`). |
| `simulation.session.id` | string (UUID) | medium | Identifier for a single simulation run. All events from one run share this ID. |
| `simulation.episode.id` | string (UUID) | **high** | Identifier for an individual episode within a session. New value per episode. |
| `simulation.agent.name` | string | low | Name of the in-simulation agent the event concerns. |
| `simulation.result` | string | low | Outcome from the perspective of the `simulation.agent.name` named in the same record. Values: `WIN`, `LOSE`, `DRAW`. |

### Why all custom attributes are nested under `simulation.*`

Unlike the ECS-aligned JSONL format, OTel does not provide ready-made namespaces for episode IDs, agent names, or simulation results. Rather than scatter custom attributes across multiple bare top-level namespaces (`episode.*`, `agent.*`), they are nested under a single `simulation.*` root. This keeps the schema self-contained, makes filter expressions easier to write (`simulation.result = "WIN"` and `simulation.agent.name = "Agent 1"` line up cleanly), and signals at a glance which attributes belong to this application's domain versus the OTel conventions.

### Cardinality notes

OTLP and downstream backends differ in their handling of high-cardinality attributes:

- **Low-cardinality attributes** are universally safe as filter, group-by, or dashboard dimensions.
- **Medium-cardinality attributes** (one value per session, ~hundreds-thousands of values) are fine for filtering and grouping in most backends.
- **High-cardinality attributes** (one value per episode) are searchable but should not be used as primary aggregation dimensions. They are also unsuitable as metric labels if metric pipelines are derived from these logs.

### Why `simulation.agent.*` and not bare `agent.*`

OpenTelemetry reserves the top-level `agent.*` namespace for attributes describing the OpenTelemetry collection agent itself (the Collector, Filebeat, Fluent Bit, etc. that picks up and forwards telemetry). Using `agent.name` at the top level for an in-simulation agent would collide with that convention and cause type conflicts in backends that pre-define `agent.*` schemas. Nesting under `simulation.agent.*` resolves the conflict entirely: backends parse the full dotted path, so `simulation.agent.name` is unambiguously distinct from `agent.name` and there is no risk of misinterpretation.

## Severity mapping

| Application condition | `severityNumber` | `severityText` |
|---|---|---|
| Routine event (win/loss recorded) | 9 | `INFO` |
| Episode produced an unexpected but recoverable result | 13 | `WARN` |
| Episode failed to complete (timeout, crash, invalid state) | 17 | `ERROR` |
| Internal diagnostic | 5 | `DEBUG` |

Win/loss outcomes themselves do **not** affect severity. A LOSE result is a successful telemetry record about a normal occurrence; it is `INFO`. Episode-level failures (the simulation itself misbehaving) are `ERROR`.

## Batching and timing

The HTTP module batches records before transmission to amortise per-request overhead. Default behaviour:

- Records are buffered for up to 5 seconds, or until the batch reaches 512 records, whichever comes first.
- Records within a batch share a single `Resource` block.
- A batch is one HTTP POST. Successful batches receive HTTP 200; failed batches are retried with exponential backoff (initial 1s, max 30s, max 5 retries).
- On shutdown, the buffer is flushed synchronously before the process exits.

Batching parameters are tunable via standard OTel SDK environment variables (`OTEL_BLRP_*`).

## Configuration

The module honours the standard OpenTelemetry environment variables:

| Variable | Purpose | Example |
|---|---|---|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | Base endpoint URL | `https://collector.example.com:4318` |
| `OTEL_EXPORTER_OTLP_LOGS_ENDPOINT` | Logs-specific endpoint (overrides base) | `https://logs.example.com/v1/logs` |
| `OTEL_EXPORTER_OTLP_PROTOCOL` | `http/protobuf` or `http/json` | `http/protobuf` |
| `OTEL_EXPORTER_OTLP_HEADERS` | Headers as comma-separated `key=value` pairs | `Authorization=Bearer abc123` |
| `OTEL_EXPORTER_OTLP_COMPRESSION` | `gzip` or `none` | `gzip` |
| `OTEL_SERVICE_NAME` | Overrides `service.name` resource attribute | `AISandbox` |
| `OTEL_RESOURCE_ATTRIBUTES` | Additional resource attributes | `deployment.environment.name=production,service.version=1.4.0` |

## Consuming the format

### OpenTelemetry Collector

A minimal pipeline receiving the module's output and forwarding it to an external backend:

```yaml
receivers:
  otlp:
    protocols:
      http:
        endpoint: 0.0.0.0:4318

processors:
  batch:
    timeout: 5s
    send_batch_size: 1024

exporters:
  otlphttp/backend:
    endpoint: https://backend.example.com
    headers:
      Authorization: "Bearer ${env:BACKEND_TOKEN}"

service:
  pipelines:
    logs:
      receivers: [otlp]
      processors: [batch]
      exporters: [otlphttp/backend]
```

### Quick test with curl

Verify a collector is accepting OTLP/HTTP/JSON traffic:

```bash
curl -X POST http://localhost:4318/v1/logs \
  -H "Content-Type: application/json" \
  -d @sample-batch.json
```

A 200 response with `{}` or `{"partialSuccess":{}}` indicates the batch was accepted.

### Querying examples

Equivalent queries across common backends, finding all WIN results for `Agent 1`:

```
# Grafana Loki (via OTel-to-Loki exporter)
{service_name="AISandbox"} | simulation_result="WIN" | simulation_agent_name="Agent 1"

# Honeycomb / Datadog (filter expression)
service.name = "AISandbox" AND simulation.result = "WIN" AND simulation.agent.name = "Agent 1"

# Elasticsearch (via OTel-to-Elastic mapping)
service.name:"AISandbox" AND simulation.result:"WIN" AND simulation.agent.name:"Agent 1"
```

Note that backends that flatten OTel attributes into JSON keys typically replace `.` with `_` (Loki) or keep `.` (Elastic, Honeycomb, Datadog). The module emits canonical OTel dotted names; transformation is the backend's responsibility.

## Versioning

Schema changes follow these rules:

- **Adding attributes** is non-breaking and may happen in any release.
- **Removing or renaming attributes** is breaking and is reflected in `service.version`.
- **Changing the type of an existing attribute** is breaking and is avoided where possible.
- **Changing OTLP transport version** is documented separately; the module targets OTLP 1.x and follows the spec's compatibility rules.

Consumers should ignore unknown attributes rather than failing on them.