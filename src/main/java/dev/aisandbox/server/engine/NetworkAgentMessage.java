/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;
import java.util.Optional;

public record NetworkAgentMessage(GeneratedMessage message,
                                  Optional<Class<? extends GeneratedMessage>> expectedResponse) {

}
