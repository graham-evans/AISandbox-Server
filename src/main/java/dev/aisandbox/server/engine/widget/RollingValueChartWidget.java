package dev.aisandbox.server.engine.widget;

import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.widget.axis.AxisScale;
import dev.aisandbox.server.engine.widget.axis.NiceAxisScale;
import dev.aisandbox.server.engine.widget.axis.TightAxisScale;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public class RollingValueChartWidget {

  // fields from the builder
  private final int width;
  private final int height;
  private final int window;
  private final String title;
  private final String xTitle;
  private final String yTitle;
  private final Theme theme;
  // internal fields
  private final List<Double> values = new ArrayList<>();
  private int startIndex = 1;
  private BufferedImage image = null;

  public static RollingScoreChartBuilder builder() {
    return new RollingScoreChartBuilder();
  }

  public void addValue(double value) {
    values.add(value);
    while (values.size() > window) {
      values.removeFirst();
      startIndex++;
    }
    image = null;
  }

  public void resetValues() {
    values.clear();
    image = null;
  }

  public BufferedImage getImage() {
    if (image == null) {
      if (values.isEmpty()) {
        image = GraphicsUtils.createBlankImage(width, height, theme.getBackground());
      } else {
        AxisScale xAxis = new TightAxisScale(startIndex, startIndex + values.size() - 1,
            width / 40);
        AxisScale yAxis = new NiceAxisScale(
            values.stream().mapToDouble(value -> value).min().orElse(0.0),
            values.stream().mapToDouble(value -> value).max().orElse(0.0),
            height / 40);
        BaseGraph graph = new BaseGraph(width, height, title, xTitle, yTitle, theme, xAxis, yAxis);
        for (int i = 1; i < values.size(); i++) {
          graph.addLine(startIndex + i - 1, values.get(i - 1), startIndex + i, values.get(i),
              theme.getGraphColor1());
        }
        graph.addAxisAndTitle();
        image = graph.getImage();
      }
    }
    return image;
  }

  @Setter
  @Accessors(chain = true, fluent = true)
  public static class RollingScoreChartBuilder {

    private int width = 200;
    private int height = 200;
    private int window = 200;
    private String title = "Values";
    private String yTitle = "Score";
    private String xTitle = "Episode";
    private Theme theme = Theme.LIGHT;

    public RollingValueChartWidget build() {
      return new RollingValueChartWidget(width, height, window, title, xTitle, yTitle, theme);
    }
  }

}
