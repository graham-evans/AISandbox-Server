package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkPlayer implements Player {

    PlayerThread playerThread;

    public NetworkPlayer(String playerName, int defaultPort, Class responseClass) {
        playerThread = new PlayerThread(playerName, defaultPort,responseClass);
        playerThread.start();
    }

    @Override
    public void send(GeneratedMessage o) {
        playerThread.sendMessage(o);
    }

    @Override
    public GeneratedMessage receive(GeneratedMessage state) {
        return playerThread.sendMessageGetResponse(state);
    }

    @Override
    public void close() {

    }

}
