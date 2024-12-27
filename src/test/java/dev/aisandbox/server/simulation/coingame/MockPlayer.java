package dev.aisandbox.server.simulation.coingame;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameAction;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardAction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class MockPlayer implements Player {

    @Getter
    private final String playerName;

    @Override
    public void send(GeneratedMessage o) {
        // ignore send messages
    }

    @Override
    public <T extends GeneratedMessage> T recieve(GeneratedMessage state, Class<T> responseType) {
        if (responseType != CoinGameAction.class) {
            log.error("Asking for {} but I can only respond with CoinGameAction", responseType.getName());
            return null;
        } else {
            // TODO create valid moves
            return (T) CoinGameAction.newBuilder().setSelectedRow(0).setRemoveCount(1).build();
        }
    }

    @Override
    public void close() {

    }
}
