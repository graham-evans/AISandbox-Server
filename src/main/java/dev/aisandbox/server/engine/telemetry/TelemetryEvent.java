/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

import java.util.List;
import io.opentelemetry.api.logs.Logger;

public interface TelemetryEvent {
    public List<String> toJSON();
    public void emit(Logger logger);
}
