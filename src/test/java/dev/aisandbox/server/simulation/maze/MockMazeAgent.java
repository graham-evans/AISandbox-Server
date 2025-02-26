package dev.aisandbox.server.simulation.maze;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.simulation.maze.proto.MazeAction;
import dev.aisandbox.server.simulation.maze.proto.MazeState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
@RequiredArgsConstructor
public class MockMazeAgent implements Agent {

    Random random = new Random();

    @Getter
    private final String agentName;

    @Override
    public void send(GeneratedMessage o) {

    }

    @Override
    public <T extends GeneratedMessage> T receive(GeneratedMessage state, Class<T> responseType) {
        MazeState mazeState = (MazeState) state;
        if (responseType != MazeAction.class) {
            log.error("Asking for {} but I can only respond with MazeAction", responseType.getName());
            return null;
        } else {
            return (T) MazeAction.newBuilder()
                    .setDirectionValue(random.nextInt(4)) // we can do this as NSEW are 0,1,2,3
                    .build();
        }
    }

    @Override
    public void close() {

    }
}
