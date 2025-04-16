package dev.aisandbox.server.simulation.bandit.model;

import lombok.Getter;

public enum BanditCountEnumeration {
  FIVE(5), TEN(10), TWENTY(20), FIFTY(50);

  @Getter
  private final int number;

  BanditCountEnumeration(int number) {
    this.number = number;
  }

}
