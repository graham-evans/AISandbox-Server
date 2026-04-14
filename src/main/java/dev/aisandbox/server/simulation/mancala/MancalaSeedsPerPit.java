/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mancala;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of allowed seeds-per-pit configurations for the Mancala game.
 *
 * <p>Each value represents a different starting number of seeds in each pit, which affects
 * game length and strategic complexity. The standard Kalah configuration uses 4 seeds per pit.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum MancalaSeedsPerPit {

  /** Three seeds per pit (36 total seeds). Shorter games. */
  THREE(3),

  /** Four seeds per pit (48 total seeds). Standard Kalah configuration. */
  FOUR(4),

  /** Five seeds per pit (60 total seeds). Longer, more strategic games. */
  FIVE(5),

  /** Six seeds per pit (72 total seeds). Extended games with more captures. */
  SIX(6);

  /** The number of seeds placed in each pit at game start. */
  private final int seeds;
}
