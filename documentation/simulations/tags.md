## Simulation Tags

Use tags to quickly find simulations suited to your algorithm or learning goal:

###### Competitive

The simulation pits two or more agents against each other. Only one agent (or team) can win — cooperation is not the goal. This is a subset of Multi-Agent.

###### Cooperative

Two or more agents must work together to achieve a shared goal. Agents are rewarded collectively, not against each other. This is a subset of Multi-Agent.

###### Dense Reward

The simulation returns a meaningful reward signal after every step, not just at episode end. This makes it well-suited to gradient-based RL methods such as PPO or Q-learning.

###### Deterministic

The simulation contains no random elements. Given the same state, the same action always produces the same next state. Planning and search algorithms (e.g. minimax, MCTS) are directly applicable.

###### Experimental

The simulation is not final and likely to change. Often this is due to balancing issues where the final scoring system has not been agreed.

**Don't expect results to be consistent between releases.**

###### Image

The simulation sends an image to the agents rather than a well-structured data object.

###### Multi-Agent

The simulation requires more than one connected agent to run.

###### Partial Observation

The agent cannot see the full game state. Some information is hidden (e.g. mine positions, opponent cards). Algorithms must reason under uncertainty rather than planning over a known state space.

###### Perfect Information

The full game state is visible to all agents at every step. Planning and search algorithms can exploit this directly.

###### Random

The simulation includes random elements — initial conditions, environmental transitions, or both.

###### Single-Agent

The simulation is designed to run with exactly one agent.

###### Sparse Reward

The simulation only signals success or failure at the end of an episode, with little or no reward during play. This makes credit assignment harder; shaping or curriculum approaches may be needed.