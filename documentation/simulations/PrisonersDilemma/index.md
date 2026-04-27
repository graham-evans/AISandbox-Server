# Prisoner's Dilemma: An Iterated Tournament Simulation

## Goal

**Prisoner's Dilemma** is a tournament-style simulation based on the classic game-theory experiment popularised by
Robert Axelrod's 1980 round-robin computer tournaments. Each agent in the tournament plays a series of one-on-one
matches against every other agent. Within each match, the two agents face each other for many rounds, on each round
choosing simultaneously whether to **Cooperate** or **Defect**. Their joint choices determine how many points each
receives that round.

The aim is straightforward: **finish the tournament with the highest total score**. An agent's score is the sum of
its per-match average rewards across all of its matches — each match contributes a number between 0 and 5, so match
length does not bias the rankings. It is not a knockout, and it is not zero-sum: a pair of mutually-cooperating
agents will both score well, while a pair of mutually-defecting agents will both score poorly. Each agent must
therefore decide, round by round and opponent by opponent, when to extend trust and when to protect itself against
exploitation.

This makes the simulation well-suited for behavioural strategy experiments, evolutionary algorithm comparisons,
reinforcement-learning agents that must model an opponent, and classroom demonstrations of cooperation under
self-interest.

---

# Details

## The Game (One Round)

In each round, the two agents simultaneously and independently choose one of two actions:

- **Cooperate (C)** — be willing to take a small individual loss for mutual benefit.
- **Defect (D)** — act in pure self-interest, at the other agent's expense.

Neither agent sees the other's choice until both have committed. Once both choices are revealed, points are awarded
according to the payoff matrix below. The history of all previous rounds in the current match is then made available
to both agents before the next round begins.

### Payoff Matrix

The payoff matrix uses the standard Axelrod values, satisfying the two defining inequalities of the Prisoner's
Dilemma: `T > R > P > S` and `2R > T + S` (the second prevents alternating exploitation from beating mutual
cooperation in the long run).

| Your Choice / Opponent's Choice | Opponent Cooperates | Opponent Defects    |
|---------------------------------|---------------------|---------------------|
| **You Cooperate**               | 3 / 3   (Reward)    | 0 / 5  (Sucker)     |
| **You Defect**                  | 5 / 0  (Temptation) | 1 / 1 (Punishment)  |

Each cell shows *your points / opponent's points*. The named labels are the canonical game-theory terms:

- **R = 3 (Reward)** — both cooperate.
- **P = 1 (Punishment)** — both defect.
- **T = 5 (Temptation)** — you defect, opponent cooperates.
- **S = 0 (Sucker)** — you cooperate, opponent defects.

In a one-shot game, defection strictly dominates: whatever your opponent does, you score strictly more by defecting
than by cooperating (two extra points if they cooperate, one extra if they defect). The interesting behaviour emerges
only when the game is **iterated** — repeated for many rounds — because future retaliation becomes possible.

---

## A Match

A **match** is a sequence of rounds played between exactly two agents. Both agents play the same number of rounds,
and the round count is fixed for the duration of a single match. Crucially, **agents are not told the round count
in advance**. This prevents the well-known endgame collapse to mutual defection, in which a rational agent
working backwards from the known final round defects on round *N − 1*, anticipates that its opponent will defect on
round *N − 1*, and unwinds the entire game to defection on round 0.

> **A note on round indexing.** Throughout this document, and in the wire protocol, **rounds are zero-indexed**.
> The first round of a match is round 0, the second is round 1, and the *N*th and final round is round *N − 1*.
> All built-in agent descriptions below follow the same convention.

The simulation supports three modes for choosing the number of rounds in each match:

| Mode               | Behaviour                                                                                                          |
|--------------------|--------------------------------------------------------------------------------------------------------------------|
| **Fixed (200)**    | Every match runs for exactly 200 rounds.                                                                           |
| **Normal**         | Mean 200 rounds, standard deviation 20, clamped to a minimum of 120. Each match draws independently.               |
| **Uniform random** | Sampled uniformly from the range 160 – 240 rounds (inclusive). Each match draws independently.                     |

The chosen mode applies to *every* match in the tournament. Within a single match, the round count is fixed once
drawn; an agent observing 197 rounds elapsed cannot conclude that the match is about to end.

After each round, both agents receive the result of that round (their score and their opponent's choice) and may use
this information when deciding the next round's action.

---

## A Tournament

A **tournament** consists of a round-robin between every participating agent. Each agent plays exactly one match
against each other agent. There is **no self-play** — an agent does not face a copy of itself.

If the tournament contains *N* agents, each agent plays *N − 1* matches, and a total of *N × (N − 1) / 2* matches
are played. With the maximum of 10 user agents plus all 10 built-in agents (20 participants), this is 190 matches.

There are no per-move timeouts. However, if a network error occurs during the tournament — for example the underlying
TCP connection times out, or a user agent disconnects — the **entire simulation aborts**. Partial results are not
reported and there is no graceful failover; tournament integrity depends on every user agent staying connected and
responsive for its full schedule of matches.

### Participating Agents

There are two kinds of agent in the tournament:

- **User agents** — external agents connected over the network protocol. The setup screen allows between 1 and 10
  user agents to join the tournament.
- **Built-in agents** — strategy implementations that run inside the simulation server itself, listed below. Each
  built-in agent can be enabled or disabled independently; any subset (including all 10) may be added to the
  tournament alongside the user agents.

If only **one user agent** is configured, **at least one built-in agent must also be added** — a tournament needs
at least two participants. There is no joint cap across the two kinds: up to 10 user agents *plus* up to all 10
built-in agents may participate, for a maximum tournament size of **20 participants**.

### Built-in Agents

The built-in agents are chosen to give a **graded ladder of opponents** rather than a collection of tournament
champions. The aim is for an agent author to be able to start by reliably beating *Always Cooperate* and *Random*,
work up through *Tit For Tat* and its variants, and finally face the probing strategies (*Joss*, *Tester*) which
deliberately look for weaknesses such as a missing retaliation rule or excessive trust. Together they cover a
useful spread along the *nice ↔ nasty*, *forgiving ↔ unforgiving*, and *honest ↔ probing* axes.

| Name                              | Strategy                                                                                                                          |
|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| **Always Cooperate**              | Cooperates on every round, regardless of history.                                                                                 |
| **Always Defect**                 | Defects on every round, regardless of history.                                                                                    |
| **Random**                        | Cooperates or defects with equal probability on every round, independent of history.                                              |
| **Tit For Tat**                   | Cooperates on round 0; from round 1 onward, copies the opponent's previous move.                                                  |
| **Tit For Two Tats**              | Cooperates by default; defects exactly once after seeing the opponent defect in two consecutive rounds.                           |
| **Suspicious Tit For Tat**        | Defects on round 0; thereafter copies the opponent's previous move.                                                               |
| **Grudger** (Friedman)            | Cooperates until the opponent defects once; defects forever afterwards.                                                           |
| **Pavlov** (Win-Stay, Lose-Shift) | Cooperates on round 0; thereafter repeats its previous move if it scored 3 or 5 last round, otherwise switches.                   |
| **Joss**                          | Tit For Tat, but defects with ≈ 10% probability even when the opponent just cooperated — a sneaky exploiter.                      |
| **Tester**                        | Defects on round 0 to probe; if retaliated against, apologises by cooperating then plays Tit For Tat; otherwise alternates C / D. |

Strategies such as Tit For Tat and its variants tend to score well in mixed populations because they are *nice*
(never defect first), *retaliatory* (punish defection), *forgiving* (return to cooperation when the opponent does),
and *clear* (easy for an opponent to learn and cooperate with).

---

## Scoring

Each match produces a **per-round average score** for each of the two agents — the total points the agent earned in
that match divided by the number of rounds played. This number lies in the range **0 – 5** and is independent of the
match length, so a match that happened to draw 80 rounds counts the same in the rankings as one that drew 450.

An agent's **tournament score** is the sum of its per-match averages across all of its matches. Equivalently — and
sometimes more intuitively — it is the agent's mean per-round score across the tournament, multiplied by *N − 1*
(the number of opponents). Agents are ranked highest score first. Per-match averages and raw point totals are both
reported, so it remains easy to inspect which pairings went well and which went badly.

If two or more agents finish on identical tournament scores, they are considered **tied** and share the same rank
(no secondary tiebreaker is computed). For display purposes the UI lists tied agents in alphabetical order by name,
purely as a stable presentation convention.

### Theoretical Bounds (per-match average, 0 – 5)

- **5.0** — defect every round against an opponent who cooperates every round (e.g. *Always Defect* vs *Always Cooperate*). Unachievable against any retaliatory strategy.
- **3.0** — sustained mutual cooperation (e.g. *Always Cooperate* vs *Always Cooperate*, or *Tit For Tat* vs *Tit For Tat*).
- **1.0** — sustained mutual defection (e.g. *Always Defect* vs *Always Defect*).
- **0.0** — cooperate every round against a constant defector (e.g. *Always Cooperate* vs *Always Defect*).

A round of mutual cooperation produces 6 points of total value to the pair; mutual defection produces only 2; one-sided
defection produces 5. This is why mutual cooperation, when it can be sustained, is collectively optimal even though
it is never individually dominant in a single round.

### Tournament-Score Bounds

In a tournament with *N* participants each agent plays *N − 1* matches, so the tournament score lies in
the range `0` to `5 × (N − 1)`. A 20-agent tournament therefore has a theoretical maximum of 95 (only
attainable against a table full of pure cooperators) and a practical mutual-cooperation reference of `3 × (N − 1) = 57`.

<!-- TODO: Add a worked end-to-end tournament example (small round-robin, full per-match averages, final
     leaderboard) once the UI / report layout is finalised so the example can match what the operator actually
     sees. -->

---

## Information Available to an Agent

Before each round, an agent is told:

- An **opaque opponent identifier** — see *Opponent Identifiers* below.
- The full sequence of previous rounds in the current match — its own actions and the opponent's actions.
- Its own running score in the current match, and the opponent's (both are derivable from the action history and
  the payoff matrix, but are sent explicitly for convenience).
- The current round number within the match.

An agent is **not** told:

- The total number of rounds in the current match.
- Which strategy any other agent is using (including built-in agents — they are anonymised; see below).
- Results from matches it is not currently playing.

Between matches, an agent is informed that the previous match has ended and that a new opponent is about to be
faced. History from prior matches is **not** carried into the new match's history block, but a strategic agent
may, of course, retain its own memory of past opponents and use it to refine future play.

### Opponent Identifiers

At tournament start, every participant — both user agents and built-in agents — is assigned an **opaque
tournament-stable identifier** (e.g. `Player_A`, `Player_B`, …, or a short random token). This token is what the
agent sees as its opponent's identity, and nothing more. For example, an agent called *Alpha* playing a match
against the *Tit For Tat* built-in would see the same token, say `Player_E`, on every round of that match:

```
opponent_id: "Player_E"
round: 17
history: [(C,C), (C,C), (C,D), (D,C), (C,C), ...]
```

The token is **stable for the duration of the tournament**: it identifies the same opponent across every round
of the match, and would identify the same opponent again in any future encounter within the same tournament.
Concretely, this means:

- Agents can tag observed actions to a particular opponent during a match.
- Agents *cannot* read the strategy off the identifier — `Player_E` carries no hint that the opponent is
  *Tit For Tat*, *Always Defect*, another user agent, or anything else. The strategy must be inferred from
  observed behaviour.
- In subsequent tournaments, fresh identifiers are issued, so an agent that retains memory across runs cannot
  recognise a returning opponent by ID — only by behavioural fingerprint.

The real names of built-in strategies (and the connection labels of user agents) appear only in the
**post-tournament report** shown to the operator, so it remains possible to inspect, after the fact, which
pairing produced which result.

---

# Algorithms and Hints

**Don't reinvent the canon — start by understanding it**

Decades of tournaments have surfaced a small set of robust principles. *Be nice* (don't be the first to defect),
*be retaliatory* (don't let defection go unpunished), *be forgiving* (return to cooperation once the opponent does),
and *be clear* (use a strategy your opponent can recognise and learn). Tit For Tat embodies all four and is famously
hard to beat in mixed populations. Treat these properties as the baseline your agent must justify departing from.

**Model the opponent, not just the history**

A flat list of past actions is data; a model of the opponent is knowledge. Try to classify each opponent into broad
behavioural buckets — *cooperative*, *exploitative*, *random*, *reactive*, *grudge-bearing* — within the first few
rounds, and adapt your policy to the bucket. Against an *Always Defect*, defect immediately; against a *Tit For Tat*,
cooperate every round; against a *Random*, defect (the expected payoff against a fair-coin opponent is higher for
defection).

**Avoid the endgame trap**

Because the round count is hidden, you should avoid building an explicit "final round" into your strategy. Even if
you suspect the match is near its end, defecting prematurely against a Tit For Tat opponent erases all of the mutual
cooperation you've built. The expected loss from one extra round of cooperation is small; the loss from triggering
mutual defection in the final stretch is substantial.

**Beware infinite grudges**

Pure Grudger-style strategies are seductive — they punish defection severely and reward cooperation cleanly — but
they cannot recover from a single noisy mistake. If you are tempted to implement a "never forgive" rule, consider
adding a small probability of forgiveness, or a finite punishment window. A single accidental defection should not
cost you 199 rounds of mutual cooperation.

**Watch for exploiters that imitate cooperators**

Some strategies will cooperate for the first several rounds to lull a generous agent into complacency, then defect
repeatedly once trust is established. A robust agent should remain alert to *changes* in opponent behaviour, not
just averages — a window-based or recency-weighted analysis catches this where a long-run average does not.

**Quantify before you commit**

If you find yourself reaching for a hand-tuned heuristic, take a moment to compute the expected payoff. Against an
opponent who cooperates with probability *p*, your expected payoff per round is `3p` if you cooperate and `5p + 1(1−p) = 4p + 1`
if you defect. Defection beats cooperation whenever `4p + 1 > 3p`, i.e. always — but only in a one-shot world.
Iteration changes the calculus by introducing future rounds in which your opponent can retaliate, which is exactly
why simple expected-value calculations against a fixed *p* are not enough.

**Think about the tournament, not just the match**

Your final ranking depends on cumulative score across many opponents. A strategy that crushes one weak opponent but
loses heavily to several strong ones may rank below a moderate strategy that scores acceptably against everyone. The
empirical winner of Axelrod's tournaments was rarely the strategy that won the most individual matches — it was the
strategy that lost the fewest points overall.

**Use built-in opponents as a calibration set**

When developing your own agent, run it against the built-in agents first. A well-designed agent should:
- Cooperate stably with *Always Cooperate* (per-match average ≈ 3.0).
- Quickly switch to defection against *Always Defect* (per-match average ≈ 1.0, losing only the first few rounds).
- Match *Tit For Tat* with full mutual cooperation (per-match average ≈ 3.0).
- Beat *Random* on average by leaning toward defection (per-match average comfortably above 2.25, the score of a pure cooperator against a fair coin).
- Avoid getting locked into mutual defection with *Grudger* after a single mistake.

If your agent fails any of these basic checks, fix that before tackling more sophisticated opponents.

**Consider noise and robustness**

Some research variants of the tournament inject random *noise* — an action is occasionally flipped before being
delivered to the opponent. This simulation does not currently model noise, but designing your agent so that its
behaviour degrades gracefully under noise (e.g. using *Generous Tit For Tat*, which forgives defection with a small
probability) is good practice for the variants likely to be added later.

---

# Protocol

TODO: Detail the protocol used (State / Action / Result message definitions, match-boundary signalling, opponent
identifiers, and the exact fields available in the per-round history block).
