package dev.aisandbox.server.simulation.twisty;

import dev.aisandbox.server.engine.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * TwistyScenario class.
 *
 * @author gde
 * @version $Id: $Id
 */

@Slf4j
public class TwistyScenario implements SimulationBuilder {

    @Getter
    @Setter
    private PuzzleType puzzleType = PuzzleType.CUBE3;

    @Getter
    @Setter
    private boolean startSolved = false;

    @Override
    public String getSimulationName() {
        return "Twisty";
    }

    @Override
    public String getDescription() {
        return "Solve various twisting puzzles.";
    }

    @Override
    public List<SimulationParameter> getParameters() {
        return List.of(
                new SimulationParameter("puzzleType", "The design of the puzzle", PuzzleType.class),
                new SimulationParameter("startSolved", "Start with a solved puzzle", Boolean.class));
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
        return new String[]{"Agent 1"};
    }

    @Override
    public Simulation build(List<Agent> agents, Theme theme) {
        try {
            return new TwistyRuntime(agents.getFirst(), puzzleType.getTwistyPuzzle(), startSolved, theme);
        } catch (Exception e) {
            log.error("Error while building Twisty Runtime.", e);
            return null;
        }
    }

}
