# Writing a Simulation
The AISandbox engine is designed to be easily expanded with new simulations being contributed by the community.

To add a new simulation, the following are required:

- A simulation builder class that describes the options specific to the simulation and sets up the simulation.
- A simulation class, which holds the main logic of the simulation. This needs to provide:
    - A "step" method which advances the simulation one step.
    - A "visualise" method which draws the current state on a ImageBuffer.