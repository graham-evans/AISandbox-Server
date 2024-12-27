package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkPlayer implements Player {

    PlayerThread playerThread;
    @Getter
    String playerName;

    public NetworkPlayer(String playerName, int defaultPort, Class responseClass) {
        this.playerName = playerName;
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
