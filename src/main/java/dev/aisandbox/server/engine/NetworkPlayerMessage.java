package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;

public record NetworkPlayerMessage(GeneratedMessage message, boolean expectResponse) {}
