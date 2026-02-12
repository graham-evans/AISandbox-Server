# Developing Scenarios

The AISandbox engine is designed to be easily expanded with new simulations being contributed by the community.

To add a new simulation, the following are required:

1. A **SimulationBuilder** class that describes the options specific to the simulation and creates simulation instances.
2. A **Simulation** class that holds the main logic. It must provide:
    - A `step` method which advances the simulation one tick.
    - A `visualise` method which draws the current state onto a 1920x1080 Graphics2D surface.
3. A **Protocol Buffer** definition (`.proto` file) that defines the messages exchanged between the simulation and external agents.
4. A new entry in **SimulationEnumeration** to register the simulation with the engine.
5. A **mock agent** and **test class** to verify the simulation works correctly.

Each of these is explained in detail below, using the Multi-Armed Bandit simulation as a worked example.

---

## Overview of the Engine

The engine separates concerns into four key interfaces:

- **SimulationBuilder** — factory and configuration container. Exposes parameters for the UI and CLI, and builds a `Simulation` instance.
- **Simulation** — the game/scenario logic. Communicates with agents and renders visual output.
- **Agent** — represents a connected player. `NetworkAgent` handles real TCP connections; `MockAgent` is used for testing.
- **OutputRenderer** — handles display. `FXRenderer` shows live JavaFX output, `BitmapOutputRenderer` saves PNG frames, and `NullOutputRenderer` discards output (useful for headless testing).

The runtime flow is:

1. The user selects a simulation and configures its parameters.
2. The engine creates `Agent` instances (one `NetworkAgent` per player, listening on TCP).
3. `SimulationBuilder.build()` creates a `Simulation` from the agents, a `Theme`, and a `Random` instance.
4. A `SimulationRunner` thread calls `simulation.step(outputRenderer)` in a loop until stopped.
5. On shutdown, `simulation.close()` and `agent.close()` clean up resources.

---

## Step 1: Define the Protocol Buffer Messages

Create a `.proto` file in `src/main/proto/`. This defines the messages that your simulation will exchange with external AI agents over TCP. Most simulations follow a **State -> Action -> Result** pattern per step:

- **State** — sent from the simulation to the agent, describing the current game state.
- **Action** — sent from the agent back to the simulation, describing the agent's chosen move.
- **Result** — sent from the simulation to the agent, reporting the outcome of the action.

Here is the Bandit example (`src/main/proto/Bandit.proto`):

```protobuf
syntax = "proto3";

package bandit;
option java_multiple_files = true;
option java_package = "dev.aisandbox.server.simulation.bandit.proto";

message BanditState {
  string sessionID = 1;
  string episodeID = 2;
  int32 banditCount = 3;
  int32 pullCount = 4;
  int32 pull = 5;
}

message BanditAction {
  int32 arm = 1;
}

message BanditResult {
  int32 arm = 1;
  double score = 2;
  Signal signal = 3;
}

enum Signal {
  CONTINUE = 0;
  RESET = 1;
}
```

Key points:

- Use a unique `package` name matching your simulation.
- Set `option java_package` to the target Java package (typically `dev.aisandbox.server.simulation.<name>.proto`).
- Set `option java_multiple_files = true` so each message gets its own Java class.
- Generated Java code is placed in `build/generated-src/` and is excluded from CheckStyle and PMD analysis.
- A `Signal` enum with `CONTINUE` and `RESET` values is a common pattern for episodic simulations.

---

## Step 2: Create the SimulationBuilder

The `SimulationBuilder` implementation acts as both a configuration object and a factory. The engine calls its methods to display options in the UI and CLI, and to construct the simulation when the user clicks "Run".

The interface requires the following methods:

| Method | Purpose |
|---|---|
| `getSimulationName()` | Short name, no whitespace (e.g. `"Bandit"`) |
| `getDescription()` | Human-readable description of the simulation |
| `getParameters()` | List of configurable `SimulationParameter` records |
| `getMinAgentCount()` | Minimum number of agents required |
| `getMaxAgentCount()` | Maximum number of agents allowed |
| `getAgentNames(int)` | Default display names for agents (e.g. `"Agent 1"`, or role-based names like `"Dispatcher"`) |
| `build(List<Agent>, Theme, Random)` | Construct and return the `Simulation` instance |

### Configurable Parameters

Parameters are exposed through `SimulationParameter` records:

```java
new SimulationParameter("banditCount", "The number of bandits", BanditCountEnumeration.class)
```

Each `SimulationParameter` has:
- **name** — must match a getter/setter pair on the builder (e.g. `"banditCount"` matches `getBanditCount()` / `setBanditCount()`).
- **description** — displayed in the UI.
- **parameterType** — the Java class of the parameter value. Enum types are presented as drop-down menus in the UI.

The recommended approach for constrained options is to define an enum for each parameter:

```java
@Getter
public enum BanditCountEnumeration {
  FIVE(5), TEN(10), TWENTY(20), FIFTY(50);

  private final int number;

  BanditCountEnumeration(int number) { this.number = number; }

  @Override
  public String toString() { return Integer.toString(number); }
}
```

The `toString()` override controls how the value appears in the UI. For more complex enums, you can include behaviour methods (see `BanditNormalEnumeration` which provides a `getNormalValue(Random)` method).

### Builder Example

```java
@Setter
@Getter
@Slf4j
public final class BanditScenario implements SimulationBuilder {

  private BanditPullEnumeration banditPulls = BanditPullEnumeration.ONE_HUNDRED;
  private BanditNormalEnumeration banditNormal = BanditNormalEnumeration.NORMAL_0_1;
  private BanditStdEnumeration banditStd = BanditStdEnumeration.ONE;
  private BanditUpdateEnumeration banditUpdate = BanditUpdateEnumeration.FIXED;
  private BanditCountEnumeration banditCount = BanditCountEnumeration.FIVE;

  @Override
  public String getSimulationName() { return "Bandit"; }

  @Override
  public String getDescription() {
    return "The classic Multi-Armed Bandit scenario...";
  }

  @Override
  public List<SimulationParameter> getParameters() {
    return List.of(
        new SimulationParameter("banditCount", "The number of bandits",
            BanditCountEnumeration.class),
        new SimulationParameter("banditUpdate", "How bandits change between pulls",
            BanditUpdateEnumeration.class),
        // ... more parameters
    );
  }

  @Override
  public int getMinAgentCount() { return 1; }

  @Override
  public int getMaxAgentCount() { return 1; }

  @Override
  public String[] getAgentNames(int agentCount) {
    return new String[]{"Agent 1"};
  }

  @Override
  public Simulation build(List<Agent> agents, Theme theme, Random random) {
    return new BanditRuntime(agents.getFirst(), random, banditCount.getNumber(),
        banditPulls.getNumber(), banditNormal, banditStd, banditUpdate, theme);
  }
}
```

Note the use of Lombok `@Getter` and `@Setter` on the class — this generates the getter/setter pairs that the parameter system needs.

---

## Step 3: Create the Simulation

The `Simulation` implementation contains the core game logic. It must implement three methods:

### `step(OutputRenderer output)`

This is the main game loop body, called once per tick. A typical step:

1. Build a protobuf **State** message from the current game state.
2. Send it to the agent with `agent.send(stateMessage)`.
3. Block until the agent responds: `AgentAction action = agent.receive(AgentAction.class)`.
4. Validate the action. If invalid, throw `IllegalActionException`.
5. Apply the action to the game state and compute the result.
6. Update any visualisation widgets.
7. Call `output.display()` to render a frame.
8. Send a protobuf **Result** message back to the agent with `agent.send(resultMessage)`.
9. If the episode is over, reset the game state for the next episode.

The method throws `SimulationException` if an unrecoverable error occurs (e.g. an agent disconnects or sends an invalid action). This will stop the simulation loop.

### `visualise(Graphics2D graphics2D)`

Called by the `OutputRenderer` to draw the current simulation state. The graphics context represents a **1920x1080 pixel** HD surface. Standard layout constants are available in `OutputConstants`:

| Constant | Value | Purpose |
|---|---|---|
| `HD_WIDTH` | 1920 | Canvas width |
| `HD_HEIGHT` | 1080 | Canvas height |
| `TOP_MARGIN` | 50 | Top margin |
| `BOTTOM_MARGIN` | 50 | Bottom margin |
| `LEFT_MARGIN` | 50 | Left margin |
| `RIGHT_MARGIN` | 50 | Right margin |
| `WIDGET_SPACING` | 50 | Gap between widgets |
| `TITLE_HEIGHT` | 50 | Height of the title bar |

A typical `visualise` implementation:

1. Fill the background with `theme.getBase()`.
2. Draw each widget's `getImage()` at its layout position.
3. Draw the theme logo image in the top-right corner.

### `close()`

Optional cleanup. Has a default no-op implementation. Override if your simulation holds resources (open files, threads, etc.).

### Theming

Every simulation receives a `Theme` object that provides a consistent colour palette:

| Theme Colour | Typical Use |
|---|---|
| `base` | Main background fill |
| `background` | Widget/panel backgrounds |
| `border` | Widget borders |
| `text` | Text colour |
| `primary` | Primary accent (charts, highlights) |
| `secondary` | Secondary accent |
| `accent` | Additional accent colour |
| `baize` | Game board / table surface |
| `baizeBorder` | Game board border |

Five built-in themes are available: `LIGHT`, `DARK`, `MIDNIGHT`, `WARM`, and `FOREST`. All simulations should use the theme colours rather than hard-coding specific colours so that they work well across all themes.

### Visualisation Widgets

The engine provides a set of reusable widgets for building simulation displays. All widgets use a builder pattern and support theming.

**Text and Title:**

| Widget | Purpose |
|---|---|
| `TitleWidget` | Centered title text at the top of the display |
| `TextWidget` | Scrolling text log with word wrapping |

**Charts and Statistics:**

| Widget | Purpose |
|---|---|
| `RollingValueChartWidget` | Line chart of values over a rolling window |
| `RollingValueHistogramWidget` | Histogram with automatic binning |
| `RollingStatisticsWidget` | Displays min, max, mean, std dev, variance |
| `RollingSuccessStatisticsWidget` | Success/failure tracking with stats on successes |
| `PieChartWidget` | Static pie chart from fixed data |
| `RollingPieChartWidget` | Dynamic pie chart updated from a stream of values |
| `RollingIconWidget` | Grid of icons showing recent events |

Example widget construction:

```java
RollingValueChartWidget scoreChart = RollingValueChartWidget.builder()
    .width(400)
    .height(300)
    .window(200)
    .title("Episode Score")
    .theme(theme)
    .build();
```

During the `step` method, update widgets with new data (e.g. `scoreChart.addValue(score)`). In `visualise`, draw each widget with `graphics2D.drawImage(widget.getImage(), x, y, null)`.

---

## Step 4: Register the Simulation

Add a new entry to the `SimulationEnumeration` enum in `dev.aisandbox.server.simulation`:

```java
public enum SimulationEnumeration {
  COIN_GAME(new CoinGameBuilder()),
  HIGH_LOW_CARDS(new HighLowCardsBuilder()),
  MULTI_BANDIT(new BanditScenario()),
  MAZE(new MazeBuilder()),
  MINE(new MineHunterScenario()),
  TWISTY(new TwistyBuilder()),
  MY_NEW_SIMULATION(new MyNewSimulationBuilder());  // Add your entry here

  @Getter
  private final SimulationBuilder builder;

  SimulationEnumeration(SimulationBuilder builder) {
    this.builder = builder;
  }
}
```

This is all that is needed — the engine discovers simulations through this enum and the UI/CLI will automatically pick up the new entry.

---

## Step 5: Write Tests

### Create a Mock Agent

Extend the `MockAgent` base class to simulate an external agent during testing. Override the `send` method to inspect incoming State messages and queue appropriate Action responses.

```java
@Slf4j
@RequiredArgsConstructor
public class MockBanditPlayer extends MockAgent {

  private final Random random = new Random();
  private final String name;

  @Override
  public void send(GeneratedMessage o) {
    if (o instanceof BanditState state) {
      // Respond to state messages by choosing a random arm
      getOutputQueue().add(
          BanditAction.newBuilder()
              .setArm(random.nextInt(state.getBanditCount()))
              .build());
    }
    // Ignore other message types (e.g. BanditResult)
  }
}
```

The key pattern: when `send` receives a State message, push an Action onto `getOutputQueue()`. When the simulation later calls `agent.receive(ActionClass.class)`, the mock returns the queued message. Result messages can be ignored since the simulation does not expect a response to them.

### Create a Test Class

Write a JUnit Jupiter test that builds the simulation, runs it for a set number of steps, and verifies no exceptions are thrown:

```java
public class TestRunBandit {

  @ParameterizedTest
  @EnumSource(Theme.class)
  public void testRunBanditGame(Theme theme) {
    assertDoesNotThrow(() -> {
      // 1. Configure the builder
      BanditScenario builder = new BanditScenario();
      builder.setBanditUpdate(BanditUpdateEnumeration.EQUALISE);
      builder.setBanditPulls(BanditPullEnumeration.TWENTY);

      // 2. Create mock agents
      List<Agent> agents = Arrays.stream(builder.getAgentNames(1))
          .map(s -> (Agent) new MockBanditPlayer(s))
          .toList();

      // 3. Build the simulation
      Simulation sim = builder.build(agents, theme, new Random());

      // 4. Set up output rendering
      File outputDirectory = new File("build/test/bandit/" + theme.name().toLowerCase());
      outputDirectory.mkdirs();
      OutputRenderer out = new BitmapOutputRenderer();
      out.setSkipFrames(100);
      out.setOutputDirectory(outputDirectory);
      out.setup(sim);

      // 5. Run for a fixed number of steps
      for (int step = 0; step < 1000; step++) {
        sim.step(out);
      }

      // 6. Clean up
      sim.close();
      agents.forEach(Agent::close);
    });
  }
}
```

Using `@ParameterizedTest` with `@EnumSource(Theme.class)` ensures the simulation is tested against all five visual themes. The `BitmapOutputRenderer` with `setSkipFrames` saves occasional frames for visual inspection in `build/test/`.

---

## Exception Handling

The engine provides three exception types:

| Exception | When to Use |
|---|---|
| `SimulationException` | General runtime failure (agent disconnected, I/O error). Thrown from `step()` to stop the simulation. |
| `IllegalActionException` | Agent sent an invalid action (out-of-bounds move, rule violation). Extends `SimulationException`. |
| `SimulationSetupException` | Initialisation failure (bad config, port unavailable). Thrown during setup, before the simulation loop starts. |

In your `step` method, validate agent actions and throw `IllegalActionException` for rule violations:

```java
if (arm < 0 || arm >= bandits.size()) {
  throw new IllegalActionException("Invalid arm: " + arm);
}
```

---

## Agent Communication

The `Agent` interface provides two methods for protobuf messaging:

- `agent.send(GeneratedMessage msg)` — sends a protobuf message to the agent. Does not block.
- `agent.receive(Class<T> responseType)` — blocks until the agent sends a response of the expected type. Throws `SimulationException` if the agent disconnects or sends the wrong message type.

For real (non-test) execution, the `NetworkAgent` implementation:

1. Opens a TCP server socket on a configurable port (tries nearby ports if the default is taken).
2. Waits for an external agent to connect.
3. Uses Protocol Buffers' `writeDelimitedTo` / `parseDelimitedFrom` for framing messages on the wire.

External agents can be written in any language that supports TCP sockets and Protocol Buffers.

---

## Checklist

When adding a new simulation, make sure you have:

- [ ] Created a `.proto` file in `src/main/proto/` with State, Action, and Result messages
- [ ] Created a `SimulationBuilder` implementation with parameters, agent counts, and a `build` method
- [ ] Created a `Simulation` implementation with `step` and `visualise` methods
- [ ] Used theme colours (not hard-coded colours) in all visualisation code
- [ ] Registered the simulation in `SimulationEnumeration`
- [ ] Created a mock agent that extends `MockAgent`
- [ ] Created a test class that runs the simulation across all themes
- [ ] Added the GPL v3 header to every new Java source file
- [ ] Verified the code passes `./gradlew checkstyleMain` and `./gradlew pmdMain`