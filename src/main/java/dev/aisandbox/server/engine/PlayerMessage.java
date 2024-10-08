package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;

public record PlayerMessage(GeneratedMessage message,boolean expectResponse) {}
