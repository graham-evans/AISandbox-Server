package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.widget.WinnerStatistics;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameAction;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameState;
import dev.aisandbox.server.simulation.coingame.proto.Signal;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class CoinGame implements Simulation {

    private final List<Player> players;
    private final CoinScenario scenario;
    private final Theme theme;
    private int[] coins;
    private int currentPlayer = 0;
    private WinnerStatistics statistics = new WinnerStatistics(100);

    public CoinGame(final List<Player> players, final CoinScenario scenario, final Theme theme) {
        this.players = players;
        this.scenario = scenario;
        this.theme = theme;
        coins = new int[scenario.getRows().length];
        reset();
    }

    private void reset() {
        // reset the number of coins in each pile
        System.arraycopy(scenario.getRows(), 0, coins, 0, scenario.getRows().length);
    }

    @Override
    public void step(OutputRenderer output) {
        log.debug("ask client {} to move", currentPlayer);
        CoinGameState currentState = generateCurrentState(Signal.PLAY);
        CoinGameAction action = players.get(currentPlayer).recieve(currentState, CoinGameAction.class);
        // try and make the move
        try {
            coins = makeMove(action.getSelectedRow(), action.getRemoveCount());
            if (isGameFinished()) {
                // current player lost
                informResult((currentPlayer + 1) % 2);
            }
        } catch (IllegalCoinAction e) {
            log.error(e.getMessage());
            // player has tried an illegal move - end the game
            informResult((currentPlayer + 1) % 2);
        }
        currentPlayer++;
        if (currentPlayer >= players.size()) {
            currentPlayer = 0;
        }
    }

    private void informResult(int winner) {
        Player winPlayer = players.get(winner);
        Player losePlayer = players.get((winner + 1) % 2);
        winPlayer.send(generateCurrentState(Signal.WIN));
        losePlayer.send(generateCurrentState(Signal.LOSE));
        statistics.addWinner(winPlayer.getPlayerName());
    }

    private int[] makeMove(int row, int amount) throws IllegalCoinAction {
        // is the amount out of the allowed range
        if (amount < 1 || amount > scenario.getMax()) {
            throw new IllegalCoinAction("Must remove between 1 and " + scenario.getMax() + " coins.");
        }
        // is the row out of the allowed indexing range
        if (row < 0 || row >= coins.length) {
            if (coins.length == 1) {
                throw new IllegalCoinAction("Must select row 0.");
            } else {
                throw new IllegalCoinAction("Must select row from 0 to " + (coins.length - 1) + ".");
            }
        }
        // does the selected row have enough coins
        if (coins[row] < amount) {
            throw new IllegalCoinAction("Not enough coins in the selected row.");
        }
        // make the move
        int[] newCoins = Arrays.copyOf(coins, coins.length);
        newCoins[row] -= amount;
        return newCoins;
    }

    private boolean isGameFinished() {
        for (int row = 0; row < scenario.getRows().length; row++) {
            if (coins[row] == 0) {
                return false;
            }
        }
        return true;
    }

    private CoinGameState generateCurrentState(Signal signal) {
        return CoinGameState.newBuilder().setSignal(signal).build();
    }


    @Override
    public void close() {

    }

    @Override
    public void visualise(Graphics2D graphics2D) {

    }
}
