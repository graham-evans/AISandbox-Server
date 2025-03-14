package dev.aisandbox.server.simulation.bandit;

import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.maths.StatisticsUtils;
import dev.aisandbox.server.engine.widget.BaseGraph;
import dev.aisandbox.server.engine.widget.GraphicsUtils;
import dev.aisandbox.server.engine.widget.axis.AxisScale;
import dev.aisandbox.server.engine.widget.axis.NiceAxisScale;
import dev.aisandbox.server.engine.widget.axis.TightAxisScale;
import dev.aisandbox.server.simulation.bandit.model.Bandit;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class BanditWidget {
    private final int width;
    private final int height;
    private final Theme theme;

    private List<Bandit> bandits = new ArrayList<>();
    private int activeBandit = -1;
    private BufferedImage image = null;

    public static BanditWidgetBuilder builder() {
        return new BanditWidgetBuilder();
    }

    public void setBandits(List<Bandit> bandits, int activeBandit) {
        this.bandits = bandits;
        this.activeBandit = activeBandit;
        // reset image
        image = null;
    }

    public BufferedImage getImage() {
        if (image == null) {
            // render image
            if (bandits.isEmpty()) {
                image = GraphicsUtils.createBlankImage(width, height, theme.getBackground());
            } else {
                // work out min and max bandit bounds
                Pair<Double, Double> minMax = StatisticsUtils.getMinMax(
                        Stream.concat(
                                bandits.stream().flatMap(bandit -> Stream.of(bandit.getMean() - bandit.getStd(), bandit.getMean() + bandit.getStd())),
                                Stream.of(0.0)).toList()
                );
                AxisScale yAxis = new NiceAxisScale(
                        minMax.getLeft(),
                        minMax.getRight(),
                        height / 40);
                AxisScale xAxis = new TightAxisScale(
                        -0.4,
                        bandits.size() - 0.4,
                        bandits.size()
                );
                BaseGraph graph = new BaseGraph(width, height, "Multi-Arm Bandits", "Bandits", "Output", theme, xAxis, yAxis);
                for (int i=0; i<bandits.size(); i++) {
                    Color banditColor = theme.getAgent1Main();
                    Color banditOutlinr = theme.getAgent1Highlight();
                    if (activeBandit == i) {
                        banditColor = theme.getAgentSelectedMain();
                        banditOutlinr = theme.getAgentSelectedHighlight();
                    }
                    Bandit bandit = bandits.get(i);
                    graph.addBox(i-0.2,bandit.getMean()-bandit.getStd(),i+0.2,bandit.getMean()+bandit.getStd(), banditColor,banditOutlinr);
                    graph.addLine(i-0.2,bandit.getMean(),i+0.2,bandit.getMean(),banditOutlinr);
                }
                graph.addAxisAndTitle();
                image = graph.getImage();
            }
        }
        return image;
    }


    @Setter
    @Accessors(chain = true, fluent = true)
    public static class BanditWidgetBuilder {
        private int width = 200;
        private int height = 200;
        private Theme theme = Theme.LIGHT;

        public BanditWidget build() {
            return new BanditWidget(width, height, theme);
        }
    }

}
