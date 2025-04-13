package dev.aisandbox.server.simulation.bandit.model;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BanditUpdateEnumeration {
  FIXED("Fixed"), // no update
  RANDOM("Random Change"), // all bandits move N(0,0.1)
  FADE("Selection Fade"), // the selected bandit decreases 0.001
  EQUALISE("Equalise"); // the selected bandit decreases 0.001 others increase 0.001/k

  private final String name;

}
