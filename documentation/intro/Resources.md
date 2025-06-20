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

## Libraries and Frameworks

- [Stable Baselines3](https://stable-baselines3.readthedocs.io/) - Well-documented implementations
  of RL algorithms
- [TensorFlow Agents](https://www.tensorflow.org/agents) - RL library for TensorFlow
- [PyTorch RL](https://github.com/pytorch/rl) - PyTorch reinforcement learning library

## Interactive Tutorials

- [Hugging Face Deep RL Course](https://huggingface.co/deep-rl-course/unit0/introduction) - Free,
  hands-on course with implementations
- [DeepMind's RL Lecture Series](https://www.youtube.com/playlist?list=PLqYmG7hTraZBKeNJ-JE_eyJHZ7XgBoAyb) -
  Video lectures from leading researchers

## Communities

- [r/ReinforcementLearning](https://www.reddit.com/r/reinforcementlearning/) - Active Reddit
  community
- [RL Discord](https://discord.gg/xhfNqQv) - Community for discussions and help
