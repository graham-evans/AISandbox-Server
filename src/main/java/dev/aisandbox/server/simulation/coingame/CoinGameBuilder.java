package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.Theme;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CoinGameBuilder implements SimulationBuilder {

    @Override
    public String getName() {
        return "CoinGame";
    }

    @Override
    public int getMinPlayerCount() {
        return 2;
    }

    @Override
    public int getMaxPlayerCount() {
        return 2;
    }

    @Override
    public String[] getPlayerNames(int playerCount) {
        return new String[]{"Player 1", "Player 2"};
    }

    @Getter
    @Setter
    private CoinScenario scenario = CoinScenario.SINGLE_21_2;

    @Override
    public Simulation build(List<Player> players, Theme theme) {
        return new CoinGame(players,scenario, theme);
    }



}
