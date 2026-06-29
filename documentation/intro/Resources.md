# Resources

Reinforcement Learning (RL) is an exciting field of machine learning where agents learn to make
decisions by interacting with an environment and receiving feedback. If you're interested in
learning RL coding, here's a structured path to get started.

## Q-Learning: A Great Starting Point

Q-Learning is one of the most accessible RL algorithms for beginners. It's a model-free, value-based
method that learns an action-value function (Q-function) representing the expected utility of taking
a specific action in a given state. It can work on or off-policy so can be trained either directly
or from replaying past recordings.

- **Model-free**: It doesn't require knowledge of the environment's dynamics
- **Value-based**: It learns the value (utility) of actions in different states
- **Off-policy**: It can learn from actions not taken by the current policy

### How Q-Learning works

1. **Q-Table Initialization:**
    - Create a table with rows representing states and columns representing actions
    - Initialize all values to zero (or some arbitrary value)
2. **Learning Loop:**
    - Start in an initial state
    - For each time step until reaching a terminal state:
        - Choose an action using an exploration strategy (like ε-greedy)
        - Take the action, observe the reward and next state
        - Update the Q-value using the Bellman equation:

          Q(s,a) ← Q(s,a) + α[r + γ·max(Q(s',a')) - Q(s,a)]

          where:
          - α (alpha) is the learning rate (how quickly new information overrides old)
          - γ (gamma) is the discount factor (importance of future rewards)
          - r is the immediate reward
          - s' is the new state
          - max(Q(s',a')) is the best estimated future value

3. **Exploitation vs. Exploration:**
    - ε-greedy approach: With probability ε, choose random action (explore)
    - Otherwise, choose action with highest Q-value (exploit)
    - Typically, ε decreases over time as the agent learns

### Simple Python Implementation:

```python
import numpy as np

# Initialize Q-table
Q = np.zeros([num_states, num_actions])

# Hyperparameters
alpha = 0.1  # Learning rate
gamma = 0.99  # Discount factor
epsilon = 0.1  # Exploration rate

# Q-learning algorithm
def q_learning(state, num_episodes):
    for i in range(num_episodes):
        state = env.reset()
        done = False
        
        while not done:
            # Choose action using epsilon-greedy
            if np.random.random() < epsilon:
                action = env.action_space.sample()  # Random action
            else:
                action = np.argmax(Q[state,:])  # Best action
            
            # Take action
            next_state, reward, done, _ = env.step(action)
            
            # Update Q-table
            Q[state, action] += alpha * (reward + gamma * np.max(Q[next_state,:]) - Q[state, action])
            
            state = next_state
```

# Useful Resources and Websites

## Online Courses

- [Reinforcement Learning Specialization](https://www.coursera.org/specializations/reinforcement-learning) (
  Coursera) - Comprehensive course by Martha White and Adam White
- [Deep Reinforcement Learning](https://www.udacity.com/course/deep-reinforcement-learning-nanodegree--nd893) (
  Udacity) - Goes from basics to advanced topics

## Books

- [Reinforcement Learning: An Introduction](http://incompleteideas.net/book/the-book-2nd.html) by
  Richard Sutton and Andrew Barto - The definitive textbook (free online)
- [Algorithms for Reinforcement Learning](https://sites.ualberta.ca/~szepesva/papers/RLAlgsInMDPs.pdf)
  by Csaba Szepesvári - Concise overview of key algorithms

## Interactive Tutorials

- [Hugging Face Deep RL Course](https://huggingface.co/deep-rl-course/unit0/introduction) - Free,
  hands-on course with implementations
- [DeepMind's RL Lecture Series](https://www.youtube.com/playlist?list=PLqYmG7hTraZBKeNJ-JE_eyJHZ7XgBoAyb) -
  Video lectures from leading researchers

## Communities

- [r/ReinforcementLearning](https://www.reddit.com/r/reinforcementlearning/) - Active Reddit
  community
- [RL Discord](https://discord.gg/xhfNqQv) - Community for discussions and help

## Libraries and Frameworks

Most languages have dedicated RL libraries as well as other frameworks related to different aspects of AI. Here are 
a selection grouped by the most used languages that support Protocol Buffers (according to the 2025 [TIOBE Index](https://www.tiobe.com/tiobe-index/)).

| Language | DNN / Deep Learning | Classical ML | RL / Q-Tables | LLM Connectors | Agent Frameworks | Rules Engines | Data / Numeric |
|---|---|---|---|---|---|---|---|
| **Python** (#1) | [PyTorch](https://pytorch.org/) · [TensorFlow](https://www.tensorflow.org/) · [JAX](https://github.com/jax-ml/jax) · [Keras](https://keras.io/) | [scikit-learn](https://scikit-learn.org/) · [XGBoost](https://xgboost.readthedocs.io/) · [LightGBM](https://lightgbm.readthedocs.io/) | [Stable Baselines3](https://stable-baselines3.readthedocs.io/) · [Ray RLlib](https://docs.ray.io/en/latest/rllib/) · [Gymnasium](https://gymnasium.farama.org/) · [CleanRL](https://github.com/vwxyzjn/cleanrl) | [LangChain](https://www.langchain.com/) · [OpenAI SDK](https://github.com/openai/openai-python) · [Anthropic SDK](https://github.com/anthropics/anthropic-sdk-python) · [LiteLLM](https://github.com/BerriAI/litellm) | [LangGraph](https://github.com/langchain-ai/langgraph) · [AutoGen](https://github.com/microsoft/autogen) · [CrewAI](https://github.com/crewAIInc/crewAI) · [Google ADK](https://github.com/google/adk-python) | [Durable Rules](https://github.com/jruizgit/rules) · [business-rules](https://github.com/venmo/business-rules) · [Experta](https://github.com/nilp0inter/experta) | [NumPy](https://numpy.org/) · [Pandas](https://pandas.pydata.org/) · [Polars](https://pola.rs/) |
| **C++** (#2) | [LibTorch](https://pytorch.org/cppdocs/) · [TensorFlow C++](https://www.tensorflow.org/install/lang_c) · [ONNX Runtime](https://onnxruntime.ai/) · [Caffe2](https://caffe2.ai/) | [mlpack](https://www.mlpack.org/) · [dlib](http://dlib.net/) · [Shark](https://www.shark-ml.org/) | [RLtools](https://github.com/rl-tools/rl-tools) · [AI-Toolbox](https://github.com/Svalorzen/AI-Toolbox) · [RLLib](https://github.com/samindaa/RLLib) · [relearn](https://github.com/alexge233/relearn) | [llama.cpp](https://github.com/ggerganov/llama.cpp) · [whisper.cpp](https://github.com/ggerganov/whisper.cpp) · [CTranslate2](https://github.com/OpenNMT/CTranslate2) | [BehaviorTree.CPP](https://github.com/BehaviorTree/BehaviorTree.CPP) — *limited higher-level agent tooling* | [CLIPS](https://www.clipsrules.net/) · [UE Rules](https://unrealengine.com/) | [Eigen](https://eigen.tuxfamily.org/) · [Armadillo](https://arma.sourceforge.net/) · [OpenCV](https://opencv.org/) |
| **Java** (#3) | [Deeplearning4j](https://deeplearning4j.konduit.ai/) · [DJL](https://djl.ai/) · [ONNX Runtime Java](https://onnxruntime.ai/) | [Weka](https://www.cs.waikato.ac.nz/ml/weka/) · [Tribuo](https://tribuo.org/) · [Smile](https://haifengl.github.io/) · [Spark MLlib](https://spark.apache.org/mllib/) | [RL4J](https://github.com/eclipse/deeplearning4j/tree/master/rl4j) · [Burlap](http://burlap.cs.brown.edu/) | [LangChain4j](https://github.com/langchain4j/langchain4j) · [Spring AI](https://spring.io/projects/spring-ai) · [Semantic Kernel](https://github.com/microsoft/semantic-kernel-java) | [LangGraph4j](https://github.com/langchain4j/langgraph4j) · [Kalix Agents](https://docs.kalix.io/) · [Google ADK Java](https://github.com/google/adk-java) | [Drools](https://www.drools.org/) · [Easy Rules](https://github.com/j-easy/easy-rules) · [OpenL Tablets](https://openl-tablets.org/) | [ND4J](https://github.com/eclipse/deeplearning4j/tree/master/nd4j) · [Tablesaw](https://github.com/jtablesaw/tablesaw) · [Apache Commons Math](https://commons.apache.org/proper/commons-math/) |
| **C#** (#5) | [TorchSharp](https://github.com/dotnet/TorchSharp) · [ONNX Runtime .NET](https://onnxruntime.ai/) · [TensorFlow.NET](https://github.com/SciSharp/TensorFlow.NET) | [ML.NET](https://dotnet.microsoft.com/apps/machinelearning-ai/ml-dotnet) · [Accord.NET](http://accord-framework.net/) | *Limited native options* — build custom with [ML.NET](https://dotnet.microsoft.com/apps/machinelearning-ai/ml-dotnet) | [Semantic Kernel](https://github.com/microsoft/semantic-kernel) · [LLM Tornado](https://github.com/lofcz/LLMTornado) · [LLamaSharp](https://github.com/SciSharp/LLamaSharp) · [Microsoft.Extensions.AI](https://learn.microsoft.com/en-us/dotnet/ai/dotnet-ai-ecosystem) | [MS Agent Framework](https://learn.microsoft.com/en-us/dotnet/ai/dotnet-ai-ecosystem) · [AutoGen.NET](https://github.com/microsoft/autogen/tree/main/dotnet) | [NRules](https://github.com/NRules/NRules) · [RulesEngine](https://github.com/microsoft/RulesEngine) | [Math.NET](https://www.mathdotnet.com/) · [NumSharp](https://github.com/SciSharp/NumSharp) |
| **JavaScript / TypeScript** (#6/#8) | [TensorFlow.js](https://www.tensorflow.org/js) · [ONNX Runtime Web](https://onnxruntime.ai/) · [Brain.js](https://brain.js.org/) | [ml.js](https://github.com/mljs) · [Danfo.js](https://danfo.jsdata.org/) | [REINFORCEjs](https://cs.stanford.edu/people/karpathy/reinforcejs/) — *niche ecosystem* | [LangChain.js](https://github.com/langchain-ai/langchainjs) · [Vercel AI SDK](https://sdk.vercel.ai/) · [OpenAI Node](https://github.com/openai/openai-node) · [Anthropic Node](https://github.com/anthropics/anthropic-sdk-typescript) | [LangGraph.js](https://github.com/langchain-ai/langgraphjs) · [Mastra](https://github.com/mastra-ai/mastra) · [MCP SDK](https://github.com/modelcontextprotocol/typescript-sdk) | [json-rules-engine](https://github.com/CacheControl/json-rules-engine) · [Nools](https://github.com/noolsjs/nools) | [mathjs](https://mathjs.org/) · [Arquero](https://github.com/uwdata/arquero) |
| **Go** (#7) | [Gorgonia](https://github.com/gorgonia/gorgonia) · [GoMLX](https://github.com/gomlx/gomlx) · [ONNX Runtime Go](https://onnxruntime.ai/) | [GoLearn](https://github.com/sjwhitworth/golearn) · [Gonum](https://www.gonum.org/) | *Very limited* — [gold](https://github.com/aunum/gold) | [LangChainGo](https://github.com/tmc/langchaingo) · [Eino](https://github.com/cloudwego/eino) · [Ollama API](https://github.com/ollama/ollama) · [GenKit Go](https://github.com/firebase/genkit/tree/main/go) | [ADK-Go](https://github.com/google/adk-go) · [go-llm](https://github.com/natexcvi/go-llm) · [MCP Go-SDK](https://github.com/modelcontextprotocol/go-sdk) | [Grule](https://github.com/hyperjumptech/grule-rule-engine) · [GoRules](https://github.com/gorules/zen) | [Gonum](https://www.gonum.org/) · [go-dataframe](https://github.com/go-gota/gota) |
| **Kotlin** (#13) | [KotlinDL](https://github.com/Kotlin/kotlindl) · [DJL](https://djl.ai/) · [DL4J](https://deeplearning4j.konduit.ai/) *(via JVM)* | [Smile](https://haifengl.github.io/) *(via Kotlin DSL)* · [Tribuo](https://tribuo.org/) *(via JVM)* | *Via Java* — [RL4J](https://github.com/eclipse/deeplearning4j/tree/master/rl4j) · [Burlap](http://burlap.cs.brown.edu/) | [LangChain4j](https://github.com/langchain4j/langchain4j) · [Spring AI](https://spring.io/projects/spring-ai) | [JB AI Agent](https://github.com/JetBrains/koog) · [Google ADK](https://github.com/google/adk-java) *(Kotlin support)* | [Drools](https://www.drools.org/) *(via JVM)* | [KMath](https://github.com/SciProgCentre/kmath) · [Multik](https://github.com/Kotlin/multik) · [Krangl](https://github.com/holgerbrandl/krangl) |
| **Rust** (#14) | [Burn](https://github.com/tracel-ai/burn) · [Candle](https://github.com/huggingface/candle) · [tch-rs](https://github.com/LaurentMazare/tch-rs) · [ort](https://github.com/pykeio/ort) | [Linfa](https://github.com/rust-ml/linfa) · [SmartCore](https://github.com/smartcorelib/smartcore) · [tract](https://github.com/sonos/tract) | [REnforce](https://github.com/NivenT/REnforce) — *emerging ecosystem* | [genai](https://github.com/jeremychone/rust-genai) · [ollama-rs](https://github.com/pepperoni21/ollama-rs) · [async-openai](https://github.com/64bit/async-openai) | [Rig](https://github.com/0xPlaygrounds/rig) · [MCP SDK Rust](https://github.com/modelcontextprotocol/rust-sdk) | *No major native option* | [ndarray](https://github.com/rust-ndarray/ndarray) · [Polars](https://pola.rs/) · [nalgebra](https://nalgebra.org/) |

### Notes

- Languages ranked by TIOBE Index (2025). All 8 languages have official or community-maintained Protobuf support.
- **Python** dominates across every category with the deepest, most mature ecosystem.
- **Java** and **C#** excel in enterprise tooling, LLM connectors, and rules engines (Drools, NRules).
- **C++** leads for low-level inference (llama.cpp powers most local LLM tooling) and performance-critical RL.
- **Go** and **Rust** are rapidly growing, especially in LLM connectors and agent frameworks, but have notable gaps in RL and rules engines.
- **Kotlin** inherits most Java JVM libraries and adds JetBrains-native tools like KotlinDL and Multik.
- *"Limited"* or *"Emerging"* indicates only a handful of experimental or unmaintained libraries exist in that space.