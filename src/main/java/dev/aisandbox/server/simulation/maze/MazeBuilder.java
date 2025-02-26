package dev.aisandbox.server.simulation.maze;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.Theme;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

public class MazeBuilder implements SimulationBuilder {

    @Getter @Setter
    private MazeSize mazeSize = MazeSize.MEDIUM;
    @Getter @Setter
    private MazeType mazeType = MazeType.BINARYTREE;

    @Override
    public String getSimulationName() {
        return "Maze";
    }

    @Override
    public String getDescription() {
        return  "Navigate the maze and find the exit, then optimise the path to find the shortest route. "+
        "The AI agent will be placed in a Maze and tasked with finding its way to the exit."
                + " Once there it will be rewarded and sent to a random position. "
                + "At each turn the AI agent is given information about the maze (dimensions,"
                + " directions etc),"
                + " the result of the last move (any reward) and asked for the next move. This repeats"
                + " until the episode finished.";
    }

    @Override
    public Map<String, String> getParameters() {
        return Map.of("mazeSize","The size of the maze","mazeType","The style of the maze");
    }

    @Override
    public int getMinAgentCount() {
        return 1;
    }

    @Override
    public int getMaxAgentCount() {
        return 1;
    }

    @Override
    public String[] getAgentNames(int playerCount) {
        return new String[] {"Agent 1"};
    }

    @Override
    public Simulation build(List<Agent> agents, Theme theme) {
        return new MazeRunner(agents.getFirst(), mazeSize, mazeType, theme);
    }
}
