/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.bandit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.BitmapOutputRenderer;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.simulation.bandit.model.BanditPullEnumeration;
import dev.aisandbox.server.simulation.bandit.model.BanditUpdateEnumeration;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class TestRunBandit {

  @ParameterizedTest
  @EnumSource(Theme.class)
  public void testRunBanditGame(Theme theme) {
    assertDoesNotThrow(() -> {
      // create simulation
      BanditScenario banditBuilder = new BanditScenario();
      banditBuilder.setBanditUpdate(BanditUpdateEnumeration.EQUALISE);
      banditBuilder.setBanditPulls(BanditPullEnumeration.TWENTY);
      // create players
      List<Agent> agents = Arrays.stream(banditBuilder.getAgentNames(1))
          .map(s -> (Agent) new MockBanditPlayer(s)).toList();
      // create simulation
      Simulation sim = banditBuilder.build(agents, theme, new Random());
      // create output directory
      File outputDirectory = new File("build/test/bandit/" + theme.name().toLowerCase());
      outputDirectory.mkdirs();
      // create output
      OutputRenderer out = new BitmapOutputRenderer();
      out.setSkipFrames(100);
      out.setOutputDirectory(outputDirectory);
      out.setup(sim);
      // start simulation
      for (int step = 0; step < 1000; step++) {
        sim.step(out);
      }
      // finish simulation
      sim.close();
      agents.forEach(Agent::close);
    });
  }

}
