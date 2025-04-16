package dev.aisandbox.server.simulation.coingame;

import static dev.aisandbox.server.engine.output.OutputConstants.BOTTOM_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.HD_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.HD_WIDTH;
import static dev.aisandbox.server.engine.output.OutputConstants.LEFT_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO_WIDTH;
import static dev.aisandbox.server.engine.output.OutputConstants.LOG_FONT;
import static dev.aisandbox.server.engine.output.OutputConstants.RIGHT_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.TITLE_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.TOP_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.WIDGET_SPACING;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.widget.PieChartWidget;
import dev.aisandbox.server.engine.widget.TextWidget;
import dev.aisandbox.server.engine.widget.TitleWidget;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameAction;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameState;
import dev.aisandbox.server.simulation.coingame.proto.Signal;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CoinGame implements Simulation {


  // UI measurements
  private static final int LOG_WIDTH = 700;
  private static final int LOG_HEIGHT = 320;
  private static final int BAIZE_WIDTH =
      HD_WIDTH - LEFT_MARGIN - RIGHT_MARGIN - WIDGET_SPACING - LOG_WIDTH;
  private static final int BAIZE_HEIGHT =
      HD_HEIGHT - BOTTOM_MARGIN - TOP_MARGIN - TITLE_HEIGHT - WIDGET_SPACING;
  private final List<Agent> agents;
  private final CoinScenario scenario;
  private final Theme theme;
  private final TitleWidget titleWidget;
  private final TextWidget logWidget;
  private final PieChartWidget pieChartWidget;
  private final String sessionID = UUID.randomUUID().toString();
  private final int maxPic = 2;
  private final List<Double> agentScores = new ArrayList<>();
  private int[] coins;
  private int currentPlayer = 0;
  private BufferedImage[] rowImages;
  private BufferedImage[] coinImages;
  private String episodeID;

  public CoinGame(final List<Agent> agents, final CoinScenario scenario, final Theme theme) {
    this.agents = agents;
    this.scenario = scenario;
    this.theme = theme;
    // build the coin pile
    coins = new int[scenario.getRows().length];
    // set up the widgets
    titleWidget = TitleWidget.builder().title("The Coin Game").theme(theme).build();
    logWidget = TextWidget.builder().width(LOG_WIDTH).height(LOG_HEIGHT).font(LOG_FONT).theme(theme)
        .build();
    pieChartWidget = PieChartWidget.builder().width(LOG_WIDTH).height(
            HD_HEIGHT - LOG_HEIGHT - TITLE_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN - WIDGET_SPACING * 2)
        .build();
    // generate the images
    try {
      rowImages = CoinIcons.getRowImages(scenario.getRows().length);
      coinImages = CoinIcons.getCoinImages(Arrays.stream(scenario.getRows()).max().getAsInt());
    } catch (IOException e) {
      log.error("Error loading images", e);
    }
    // set the scores to 0
    agents.forEach(a -> agentScores.add(0.0));
    // reset the game to the initial stage
    reset();
  }

  private void reset() {
    // reset the number of coins in each pile
    System.arraycopy(scenario.getRows(), 0, coins, 0, scenario.getRows().length);
    // change the episode ID
    episodeID = UUID.randomUUID().toString();
  }

  @Override
  public void step(OutputRenderer output) {
    // draw the current state
    output.display();
    log.debug("ask client {} to move", currentPlayer);
    CoinGameState currentState = generateCurrentState(Signal.PLAY);
    CoinGameAction action = agents.get(currentPlayer).receive(currentState, CoinGameAction.class);
    // try and make the move
    try {
      coins = makeMove(action.getSelectedRow(), action.getRemoveCount());
      logWidget.addText(
          agents.get(currentPlayer).getAgentName() + " takes " + action.getRemoveCount()
              + " from row " + action.getSelectedRow() + " leaving "
              + coins[action.getSelectedRow()] + ".");
      if (isGameFinished()) {
        // current player lost
        logWidget.addText(agents.get(currentPlayer).getAgentName() + " lost");
        informResult((currentPlayer + 1) % 2);
        output.display();
        reset();
      }
    } catch (IllegalCoinAction e) {
      log.error(e.getMessage());
      logWidget.addText(agents.get(currentPlayer).getAgentName() + " makes an illegal move.");
      // player has tried an illegal move - end the game
      informResult((currentPlayer + 1) % 2);
      output.display();
      reset();
    }
    currentPlayer++;
    if (currentPlayer >= agents.size()) {
      currentPlayer = 0;
    }
  }

  private void informResult(int winner) {
    Agent winAgent = agents.get(winner);
    Agent loseAgent = agents.get((winner + 1) % 2);
    winAgent.send(generateCurrentState(Signal.WIN));
    loseAgent.send(generateCurrentState(Signal.LOSE));
    logWidget.addText("Player [" + winner + "] wins");
    agentScores.set(winner, agentScores.get(winner) + 1);
    pieChartWidget.setPie(IntStream.range(0, agents.size() - 1).mapToObj(
        operand -> new PieChartWidget.Slice(agents.get(operand).getAgentName(),
            agentScores.get(operand), theme.getAgentMain(operand))).toList());
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
    // is the agent taking too many coins
    if (amount > maxPic) {
      throw new IllegalCoinAction("Taking more than the maximum amount of coins.");
    }
    // make the move
    int[] newCoins = Arrays.copyOf(coins, coins.length);
    newCoins[row] -= amount;
    return newCoins;
  }

  private boolean isGameFinished() {
    for (int row = 0; row < scenario.getRows().length; row++) {
      if (coins[row] > 0) {
        return false;
      }
    }
    return true;
  }

  private CoinGameState generateCurrentState(Signal signal) {
    return CoinGameState.newBuilder().addAllCoinCount(Arrays.stream(coins).boxed().toList())
        .setRowCount(coins.length).setMaxPick(maxPic).setSignal(signal).setSessionID(sessionID)
        .setEpisodeID(episodeID).build();
  }

  @Override
  public void visualise(Graphics2D graphics2D) {
    graphics2D.setColor(theme.getBackground());
    graphics2D.fillRect(0, 0, HD_WIDTH, HD_HEIGHT);
    // draw widgets
    graphics2D.drawImage(titleWidget.getImage(), 0, TOP_MARGIN, null);
    graphics2D.drawImage(logWidget.getImage(), HD_WIDTH - RIGHT_MARGIN - LOG_WIDTH,
        TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, null);
    graphics2D.drawImage(pieChartWidget.getImage(), HD_WIDTH - RIGHT_MARGIN - LOG_WIDTH,
        TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + LOG_HEIGHT + WIDGET_SPACING, null);
    // draw baize
    graphics2D.setColor(theme.getBaize());
    graphics2D.fillRect(LEFT_MARGIN, TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING, BAIZE_WIDTH,
        BAIZE_HEIGHT);
    graphics2D.setColor(theme.getText());
    for (int i = 0; i < coins.length; i++) {
      graphics2D.drawImage(rowImages[i], LEFT_MARGIN,
          TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + i * CoinIcons.ROW_HEIGHT, null);
      graphics2D.drawImage(coinImages[coins[i]], LEFT_MARGIN + CoinIcons.ROW_WIDTH,
          TOP_MARGIN + TITLE_HEIGHT + WIDGET_SPACING + i * CoinIcons.ROW_HEIGHT, null);
    }

    // draw logo
    graphics2D.drawImage(LOGO, HD_WIDTH - LOGO_WIDTH - RIGHT_MARGIN,
        HD_HEIGHT - LOGO_HEIGHT - BOTTOM_MARGIN, null);

  }
}
