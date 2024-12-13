# Getting Started

AI Sandbox allows you to begin reinforcement learning and other artificial intelligence techniques by providing scenarios and simulations which your programmes can interact with.

The software provides a server which applications in any language (that support the protobuf protocol) can connect to and interact with. To see a worked example, see the demos on github:

- Examples in [Java](https://github.com/graham-evans/AISandbox-Demos-Java)
- Examples in [Python](https://github.com/graham-evans/AISandbox-Demos-Python)

To get Started follow these steps:

## Step 1 download and run the client.

The server is available as a Java 'JAR' application which requires you to have an existing Java 21 (or higher) environment, or as a platform-specific installer for Windows or 64 bit Linux.

### Launching the application Jar

you can download the cross-platform JAR and launch the application with the command:

java -jar AISandbox-Server-***version***.jar

Where ***version*** is the application version you have downloaded.

### Launching the installed application (Windows)

TODO

### Launching the installed application (Linux)

TODO

## Step 2 setup the simulation

After choosing a simulation to run, you will be shown a screen similar to this:


On the left hand side are options to tailor the simulation. This will be different for each scenario. The top right is where you configure your agents. Each agent is defined as a URL which the client will connect to.

Add an agent by pressing the add agent button then configure it by double clicking on the URL.


On the edit agent screen, you can change the agent endpoint as well as add authentication and choose between REST-Jason and REST-XML.
Returning to the main screen, the bottom right of the window will allow you to set up any output options. This includes the ability to take screen shots for any frame or export the output of the simulation as a video.

Step 3 Run Simulation
Clicking on ‘next’ will take you to the run screen

Pressing start will initialise the simulation, the output of which is shown in the main viewscreen.

The graphs at the bottom will summarise how your agent is running.