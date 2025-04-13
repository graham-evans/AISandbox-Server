package dev.aisandbox.server.simulation.bandit.model;

import lombok.Getter;

@Getter
public enum BanditStdEnumeration {
  ONE(1.0),
  FIVE(5.0),
  TENTH(1.0 / 10);

  private final double value;

  BanditStdEnumeration(double value) {
    this.value = value;
  }
}
