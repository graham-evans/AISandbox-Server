# AI Sandbox - Simulation Server

Server simulations for learning Reinforcement Learning and action-based AI.

The AI Sandbox provides a collection of environments where you can build and train AI agents using any programming language that supports [Protocol Buffers](https://protobuf.dev/). Write your agent, connect it over TCP, and start learning.

Full documentation is available at [aisandbox.dev](https://aisandbox.dev).

## Implemented Simulations

| Simulation | Description |
|---|---|
| **Multi Armed Bandits** | Repeatedly select from a series of one-armed bandits to achieve the best return over time. |
| **The Coin Game** | Two-player puzzles where the aim is to force your opponent to take the last coin. |
| **High / Low Cards** | Choose whether the next card will be higher or lower than the last. |
| **Maze** | Explore a maze to find the exit, then exploit the biases in the generator. |
| **Mine Hunter** | Use logic to defuse a minefield. |
| **Twisty Puzzles** | Traditional logic puzzles of various sizes. |
| **Mancala** | Two-player game collecting seeds (version 2.1) |

## Getting Started

### Download

Platform-specific installers and archives are available from the [downloads page](https://aisandbox.dev/intro/Downloads.html) on the website.

### Run from Source

Requires JDK 21 or higher ([Adoptium](https://adoptium.net/), [Oracle](https://www.oracle.com/java/technologies/downloads), or [Microsoft OpenJDK](https://learn.microsoft.com/en-us/java/openjdk/download)).

```bash
./gradlew run
```

This launches the GUI where you can select a simulation, configure its parameters, and start it. The server then opens a TCP port for each agent slot, ready for your code to connect.

### Write an Agent

Connect to the server using Protocol Buffers over TCP from any language. Example agents are available in:

- [Java demos](https://github.com/graham-evans/AISandbox-Demos-Java)
- [Python demos](https://github.com/graham-evans/AISandbox-Demos-Python)

### CLI Mode

Run simulations headlessly by passing command-line arguments:

```bash
./gradlew run --args="--help"
```

## Building

```bash
./gradlew build       # Compile, test, and run static analysis
./gradlew test        # Run tests only
./gradlew distZip     # Create distribution archive
```

Cross-compile for other platforms with:

```bash
./gradlew clean distZip -Denv=win
```

Supported platforms: `win`, `linux`, `linux-aarch64`, `mac`, `osx`, `osx-aarch64`.

## Licence

This project is licensed under the [GNU General Public License v3.0](LICENCE).
