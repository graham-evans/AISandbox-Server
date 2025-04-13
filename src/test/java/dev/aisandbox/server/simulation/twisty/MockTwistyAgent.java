package dev.aisandbox.server.simulation.twisty;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.simulation.twisty.proto.TwistyAction;
import dev.aisandbox.server.simulation.twisty.proto.TwistyState;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MockTwistyAgent implements Agent {

  @Getter
  private final String agentName;
  Random random = new Random();

  @Override
  public void send(GeneratedMessage o) {

  }

  @Override
  public <T extends GeneratedMessage> T receive(GeneratedMessage state, Class<T> responseType) {
    TwistyState twistyState = (TwistyState) state;
    if (responseType != TwistyAction.class) {
      log.error("Asking for {} but I can only respond with TwistyAction", responseType.getName());
      return null;
    } else {
      log.debug("Generating random action from {} options", twistyState.getValidMovesCount());
      return (T) TwistyAction.newBuilder()
          .setMove(twistyState.getValidMoves(random.nextInt(twistyState.getValidMovesCount())))
          .build();
    }
  }

  @Override
  public void close() {

  }
}
