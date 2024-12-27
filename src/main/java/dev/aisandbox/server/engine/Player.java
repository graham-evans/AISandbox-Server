package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;

public interface Player {
    void send(GeneratedMessage o);

    GeneratedMessage receive(GeneratedMessage state);

    void close();
}
