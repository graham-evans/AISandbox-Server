package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;

import java.util.Optional;

public record NetworkAgentMessage(GeneratedMessage message,
                                  Optional<Class<? extends GeneratedMessage>> expectedResponse) {
}
