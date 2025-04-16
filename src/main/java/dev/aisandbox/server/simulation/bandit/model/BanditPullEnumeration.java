package dev.aisandbox.server.simulation.bandit.model;

import lombok.Getter;

@Getter
public enum BanditPullEnumeration {
  TWENTY(20),
  ONE_HUNDRED(100),
  FIVE_HUNDRED(500),
  ONE_THOUSAND(1000),
  TWO_THOUSAND(2000),
  FIVE_THOUSAND(5000);

  private final int number;

  BanditPullEnumeration(int number) {
    this.number = number;
  }

}
