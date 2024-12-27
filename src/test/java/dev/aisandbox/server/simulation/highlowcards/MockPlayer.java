package dev.aisandbox.server.simulation.highlowcards;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.simulation.highlowcards.proto.ClientAction;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowChoice;

import java.util.Random;

public class MockPlayer implements Player {

    private Random random = new Random();

    @Override
    public String getPlayerName() {
        return "Mock Player";
    }

    @Override
    public void send(GeneratedMessage o) {
        // send message to player - ignore it
    }

    @Override
    public GeneratedMessage receive(GeneratedMessage state) {
        // send a message to the player, recieve either 'higher' or 'lower'
        if (random.nextBoolean()) {
            return ClientAction.newBuilder().setAction(HighLowChoice.HIGH).build();
        } else {
            return ClientAction.newBuilder().setAction(HighLowChoice.LOW).build();
        }
    }

    @Override
    public void close() {

    }
}
