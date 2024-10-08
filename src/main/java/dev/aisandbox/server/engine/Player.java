package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.simulation.highlowcards.proto.ClientAction;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowChoice;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;

@Slf4j
public class Player {

    PlayerThread playerThread;


    public Player(String playerName, int defaultPort,Class responseClass) {
        playerThread = new PlayerThread(playerName, defaultPort,responseClass);
        playerThread.start();
    }

    public void send(GeneratedMessage o) {
        playerThread.sendMessage(o);
    }

    public GeneratedMessage receive(GeneratedMessage state) {
        return playerThread.sendMessageGetResponse(state);
    }

    public void close() {

    }

}
