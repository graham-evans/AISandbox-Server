package dev.aisandbox.server.simulation.bandit;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputConstants;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.widget.RollingValueChartWidget;
import dev.aisandbox.server.engine.widget.TextWidget;
import dev.aisandbox.server.simulation.bandit.model.Bandit;
import dev.aisandbox.server.simulation.bandit.model.BanditNormalEnumeration;
import dev.aisandbox.server.simulation.bandit.model.BanditStdEnumeration;
import dev.aisandbox.server.simulation.bandit.model.BanditUpdateEnumeration;
import dev.aisandbox.server.simulation.bandit.proto.BanditAction;
import dev.aisandbox.server.simulation.bandit.proto.BanditResult;
import dev.aisandbox.server.simulation.bandit.proto.BanditState;
import dev.aisandbox.server.simulation.bandit.proto.Signal;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
public class BanditRuntime implements Simulation {

    //    private AverageRewardGraph averageRewardGraph;
//    private OptimalActionGraph optimalActionGraph;
    //   private BanditGraph banditGraph;
    private static final int MARGIN = 100;
    // initial parameters
    private final Agent agent;
    private final Random rand;
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
    private int sessionStep = 0;
    private double episodeScore = 0;
    private double episodeBestMoveCount = 0;
    private String episodeID = UUID.randomUUID().toString();
    private BufferedImage logo;

    public BanditRuntime(Agent agent, Random rand, int banditCount, int pullCount, BanditNormalEnumeration normal, BanditStdEnumeration std, BanditUpdateEnumeration updateRule, Theme theme) {
        // store parameters
        this.agent = agent;
        this.rand = rand;
        this.banditCount = banditCount;
        this.pullCount = pullCount;
        this.normal = normal;
        this.std = std;
        this.updateRule = updateRule;
        this.theme = theme;
        // load logo
        try {
            logo = ImageIO.read(BanditRuntime.class.getResourceAsStream("/images/AILogo.png"));
        } catch (Exception e) {
            log.error("Error loading logo", e);
            logo = new BufferedImage(OutputConstants.LOGO_WIDTH, OutputConstants.LOGO_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        }
        // initialise bandits
        initialise();
        // initialise widgets
        logWidget = TextWidget.builder().width(400).height(300).theme(theme).build();
        banditWidget = BanditWidget.builder().width(400).height(300).theme(theme).build();
        episodeScoreWidget = RollingValueChartWidget.builder().width(400).height(300).window(200).theme(theme).build();
        episodeSuccessWidget = RollingValueChartWidget.builder().width(400).height(300).window(200).theme(theme).build();
    }

    @Override
    public void step(OutputRenderer output) {
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
        // todo - test for invalid request
        // get the score
        double score = bandits.get(arm).pull(rand);
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
            bandits.add(new Bandit(normal.getNormalValue(rand), std.getValue()));
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
            b.setMean(b.getMean() + rand.nextGaussian() * 0.001);
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
        graphics2D.fillRect(0, 0, OutputConstants.HD_WIDTH, OutputConstants.HD_HEIGHT);
        // draw logo
        graphics2D.drawImage(logo, OutputConstants.HD_WIDTH - OutputConstants.LOGO_WIDTH - MARGIN, OutputConstants.HD_HEIGHT - OutputConstants.LOGO_HEIGHT - MARGIN, null);
        // draw log window
        graphics2D.drawImage(logWidget.getImage(), 800, MARGIN, null);
        // draw bandits
        graphics2D.drawImage(banditWidget.getImage(), MARGIN, MARGIN, null);
        // draw episode scores
        graphics2D.drawImage(episodeScoreWidget.getImage(), MARGIN, MARGIN * 2 + 300, null);
        // draw episode success
        graphics2D.drawImage(episodeSuccessWidget.getImage(), MARGIN * 2 + 500, MARGIN * 2 + 300, null);
    }
}
