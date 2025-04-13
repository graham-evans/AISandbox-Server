package dev.aisandbox.server.engine.widget;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PieChartTest {

  private static final File outputDir = new File("build/test/widgets/piechart");
  private static final Random rand = new Random();

  @BeforeAll
  public static void setupDir() {
    outputDir.mkdirs();
  }


  @Test
  public void testNoData() throws IOException {
    PieChartWidget pieChart = PieChartWidget.builder().width(500).height(500)
        .title("Empty Pie Chart").build();
    ImageIO.write(pieChart.getImage(), "png", new File(outputDir, "empty.png"));
  }

  @Test
  public void testOneData() throws IOException {
    PieChartWidget pieChart = PieChartWidget.builder().width(500).height(500)
        .title("Pie Chart with 1 segment").build();
    pieChart.setPie(List.of(new PieChartWidget.Slice("Agent 1", 5.0, Color.BLUE)));
    ImageIO.write(pieChart.getImage(), "png", new File(outputDir, "one.png"));
  }

  @Test
  public void testTwoData() throws IOException {
    PieChartWidget pieChart = PieChartWidget.builder().width(500).height(500)
        .title("Pie Chart with 2 segments").build();
    pieChart.setPie(List.of(
        new PieChartWidget.Slice("Agent 1", 5.0, Color.BLUE),
        new PieChartWidget.Slice("Agent 2", 5.0, Color.RED)
    ));
    ImageIO.write(pieChart.getImage(), "png", new File(outputDir, "two.png"));
  }

  @Test
  public void testTenData() throws IOException {
    PieChartWidget pieChart = PieChartWidget.builder().width(500).height(500)
        .title("Pie Chart with 10 segments").build();
    List<PieChartWidget.Slice> segments = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      segments.add(new PieChartWidget.Slice("Agent " + i, i,
          new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat())));
    }
    pieChart.setPie(segments);
    ImageIO.write(pieChart.getImage(), "png", new File(outputDir, "ten.png"));
  }

}
