package dev.aisandbox.server.simulation.bandit;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.exception.IllegalActionException;
import dev.aisandbox.server.engine.exception.SimulationException;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.widget.RollingValueChartWidget;
import dev.aisandbox.server.engine.widget.TextWidget;
import dev.aisandbox.server.engine.widget.TitleWidget;
import dev.aisandbox.server.simulation.bandit.model.Bandit;
import dev.aisandbox.server.simulation.bandit.model.BanditNormalEnumeration;
import dev.aisandbox.server.simulation.bandit.model.BanditStdEnumeration;
import dev.aisandbox.server.simulation.bandit.model.BanditUpdateEnumeration;
import dev.aisandbox.server.simulation.bandit.proto.BanditAction;
import dev.aisandbox.server.simulation.bandit.proto.BanditResult;
import dev.aisandbox.server.simulation.bandit.proto.BanditState;
import dev.aisandbox.server.simulation.bandit.proto.Signal;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

import static dev.aisandbox.server.engine.output.OutputConstants.*;

@Slf4j
public final class BanditRuntime implements Simulation {

    //    private AverageRewardGraph averageRewardGraph;
//    private OptimalActionGraph optimalActionGraph;
    //   private BanditGraph banditGraph;
    // initial parameters
    private final Agent agent;
    private final Random random;
    private final int banditCount;
    private final int pullCount;
    private final BanditNormalEnumeration normal;
    private final BanditStdEnumeration std;
    private final BanditUpdateEnumeration updateRule;
    private final Theme theme;
    private final String sessionID = UUID.randomUUID().toString();
    private final List<Bandit> bandits = new ArrayList<>();
    private final TextWidget logWidget;
    private final BanditWidget banditWidget;
    private final RollingValueChartWidget episodeScoreWidget;
    private final RollingValueChartWidget episodeSuccessWidget;
    private final TitleWidget titleWidget;
    private int sessionStep = 0;
    private double episodeScore = 0;
    private double episodeBestMoveCount = 0;
    private String episodeID = UUID.randomUUID().toString();
    // UI Constants
    private static final int LOG_WIDTH = 700;
    private static final int LOG_HEIGHT = 320;
    private static final int BANDIT_WIDTH = HD_WIDTH-LEFT_MARGIN-RIGHT_MARGIN-WIDGET_SPACING-LOG_WIDTH;
    private static final int BANDIT_HEIGHT = LOG_HEIGHT;


    public BanditRuntime(Agent agent, Random random, int banditCount, int pullCount, BanditNormalEnumeration normal, BanditStdEnumeration std, BanditUpdateEnumeration updateRule, Theme theme) {
        // store parameters
        this.agent = agent;
        this.random = random;
        this.banditCount = banditCount;
        this.pullCount = pullCount;
        this.normal = normal;
        this.std = std;
        this.updateRule = updateRule;
        this.theme = theme;
        // initialise bandits
        initialise();
        // initialise widgets
        titleWidget = TitleWidget.builder().theme(theme).title("Multi-armed Bandit").build();
        logWidget = TextWidget.builder().width(LOG_WIDTH).height(LOG_HEIGHT).theme(theme).build();
        banditWidget = BanditWidget.builder().width(BANDIT_WIDTH).height(BANDIT_HEIGHT).theme(theme).build();
        episodeScoreWidget = RollingValueChartWidget.builder().width(400).height(300).window(200).theme(theme).build();
        episodeSuccessWidget = RollingValueChartWidget.builder().width(400).height(300).window(200).theme(theme).build();
    }

    @Override
    public void step(OutputRenderer output) throws SimulationException {
        sessionStep++;
        log.debug("Starting step {}", sessionStep);
        // work out the 'best' bandit to pull
        int bestPull = IntStream.range(0, bandits.size()).boxed().max(Comparator.comparingDouble(i -> bandits.get(i).getStd())).orElse(-1);
        // ask user which bandit to pull
        BanditAction action = agent.receive(getState(), BanditAction.class);
        int arm = action.getArm();
        log.debug("Received request to pull arm {}", arm);
        // was this the best move
        if (arm == bestPull) {
            episodeBestMoveCount += 1.0;
        }
        // test for invalid request
        if ((arm < 0) || (arm >= bandits.size())) {
            throw new IllegalActionException("Invalid arm.");
        }
        // get the score
        double score = bandits.get(arm).pull(random);
        episodeScore += score;
        // log the action
        logWidget.addText(agent.getAgentName() + " selects bandit " + arm + " gets reward " + String.format("%.4f", score));
        // should we reset
        boolean reset = sessionStep == pullCount;
        // update the widgets
        banditWidget.setBandits(bandits, arm);
        // if last pull, update the episode widgets
        if (reset) {
            episodeScoreWidget.addValue(episodeScore);
            episodeSuccessWidget.addValue(episodeBestMoveCount / pullCount);
        }
        // update the screen
        output.display();
        // tell the user the result
        agent.send(BanditResult.newBuilder().setArm(arm).setScore(score).setSignal(reset ? Signal.RESET : Signal.CONTINUE).build());
        // move to next episode if needed.
        if (reset) {
            logWidget.addText("pull count reached, starting a new game.");

        }
        // update the bandits
        switch (updateRule) {
            case RANDOM:
                updateRandom();
                break;
            case EQUALISE:
                updateEqualise(arm);
                break;
            case FADE:
                updateFade(arm);
                break;
            default: // FIXED
                // no action
        }
        // reset?
        if (reset) {
            initialise();
        }
    }

    /**
     * Generate a state (protobuf) object based on the current state.
     *
     * @return the state of the current game
     */
    private BanditState getState() {
        BanditState.Builder builder = BanditState.newBuilder();
        builder.setSessionID(sessionID);
        builder.setEpisodeID(episodeID);
        builder.setBanditCount(bandits.size());
        builder.setPull(sessionStep);
        builder.setPullCount(pullCount);
        return builder.build();
    }

    /**
     * initialise or reset the bandit state, creating new bandits as needed.
     */
    public void initialise() {
        // clear the bandits
        bandits.clear();
        for (int i = 0; i < banditCount; i++) {
            bandits.add(new Bandit(normal.getNormalValue(random), std.getValue()));
        }
        sessionStep = 0;
        episodeID = UUID.randomUUID().toString();
        episodeScore = 0;
        episodeBestMoveCount = 0;
    }

    /**
     * Update the bandits by moving each mean N(0,0.1)
     */
    public void updateRandom() {
        for (Bandit b : bandits) {
            b.setMean(b.getMean() + random.nextGaussian() * 0.001);
        }
    }

    /**
     * Update the bandits by moving the chosen bandit by -0.001
     *
     * @param chosen the index of the chosen bandit
     */
    public void updateFade(int chosen) {
        Bandit target = bandits.get(chosen);
        target.setMean(target.getMean() - 0.001);
    }

    /**
     * Update the bandits by moving the mean of the chosen bandit by -0.001 and all others by +0.001/k
     *
     * @param chosen the index of the chosen bandit
     */
    public void updateEqualise(int chosen) {
        double reward = 0.001 / bandits.size();
        for (int i = 0; i < bandits.size(); i++) {
            Bandit b = bandits.get(i);
            if (i == chosen) {
                b.setMean(b.getMean() - 0.001);
            } else {
                b.setMean(b.getMean() + reward);
            }
        }
    }

    @Override
    public void visualise(Graphics2D graphics2D) {
        graphics2D.setColor(theme.getBackground());
        graphics2D.fillRect(0, 0, HD_WIDTH, HD_HEIGHT);
        // draw title
        graphics2D.drawImage(titleWidget.getImage(), 0,TOP_MARGIN,null);
        // draw logo
        graphics2D.drawImage(LOGO, HD_WIDTH - LOGO_WIDTH - RIGHT_MARGIN, HD_HEIGHT - LOGO_HEIGHT - BOTTOM_MARGIN, null);
        // draw log window
        graphics2D.drawImage(logWidget.getImage(), HD_WIDTH-RIGHT_MARGIN-LOG_WIDTH,TOP_MARGIN+TITLE_HEIGHT+WIDGET_SPACING, null);
        // draw bandits
        graphics2D.drawImage(banditWidget.getImage(), LEFT_MARGIN, TOP_MARGIN+TITLE_HEIGHT+WIDGET_SPACING, null);
        // draw episode scores
        graphics2D.drawImage(episodeScoreWidget.getImage(), MARGIN, MARGIN * 2 + 300, null);
        // draw episode success
        graphics2D.drawImage(episodeSuccessWidget.getImage(), MARGIN * 2 + 500, MARGIN * 2 + 300, null);
    }
}
