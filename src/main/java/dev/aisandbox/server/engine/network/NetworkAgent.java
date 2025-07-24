/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.network;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.exception.SimulationException;
import dev.aisandbox.server.engine.exception.SimulationSetupException;
import dev.aisandbox.server.engine.network.NetworkAgentConnectionThread.ConnectionPair;
import dev.aisandbox.server.engine.output.OutputRenderer;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.SynchronousQueue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkAgent implements Agent {


  private static final int MAX_PORT_TRIES = 10;
  @Getter
  private final String agentName;
  private final ServerSocket serverSocket;
  private final OutputRenderer renderer;
  private final SynchronousQueue<ConnectionPair> connectionQueue = new SynchronousQueue<>();
  private NetworkAgentConnectionThread.ConnectionPair connectionPair = null;

  public NetworkAgent(String agentName, int defaultPort, boolean openExternal,
      OutputRenderer renderer) throws SimulationSetupException {
    this.agentName = agentName;
    this.renderer = renderer;
    serverSocket = getServerSocket(defaultPort, openExternal);
    NetworkAgentConnectionThread networkAgentConnectionThread = new NetworkAgentConnectionThread(
        agentName, serverSocket, connectionQueue, renderer);
    networkAgentConnectionThread.start();
  }

  private ServerSocket getServerSocket(int defaultPort, boolean openExternal)
      throws SimulationSetupException {
    int targetPort = defaultPort;
    int tries = 0;
    ServerSocket socket = null;
    while (socket == null && tries < MAX_PORT_TRIES) {
      try {
        if (openExternal) {
          log.info("Trying to create server socket on port {}", targetPort);
          socket = new ServerSocket(targetPort);
        } else {
          log.info("Trying to create server socket on loopback port {}", targetPort);
          socket = new ServerSocket(targetPort, 1, InetAddress.getLoopbackAddress());
        }
        log.info("Successfully created server socket on port {}", targetPort);
        renderer.write("Connect " + agentName + " to port " + targetPort);
      } catch (IOException e) {
        log.warn("Failed to create server socket with port {}", targetPort, e);
        tries++;
        targetPort++;
      }
    }
    if (socket == null) {
      renderer.write("Failed to create server socket.");
      log.error("Unable to create server socket for {} after {} tries", agentName, MAX_PORT_TRIES);
      throw new SimulationSetupException("Unable to create server socket for " + agentName);
    }
    return socket;
  }

  @Override
  public void send(GeneratedMessage o) throws SimulationException {
    if (o == null) {
      log.warn("Trying to send a null object to {}", agentName);
    } else {
      log.debug("Sending '{}' to {}", o.toString().replaceAll("[\\n\\r]", ""), agentName);
    }
    try {
      if (connectionPair == null) {
        // wait for a connection
        connectionPair = connectionQueue.take();
      }
      // send the message
      o.writeDelimitedTo(connectionPair.output());
    } catch (IOException e) {
      log.error("IO exception while sending message to {}", agentName, e);
      throw new SimulationException("IO Error sending to " + agentName);
    } catch (InterruptedException e) {
      throw new SimulationException("Sending message while shutting down");
    }
  }

  @Override
  public <T extends GeneratedMessage> T receive(Class<T> responseType) throws SimulationException {
    try {
      if (connectionPair == null) {
        // wait for a connection
        connectionPair = connectionQueue.take();
      }
      log.debug("Asking {} thread for response type {}", agentName, responseType.getSimpleName());

      Method readDelimited = responseType.getMethod("parseDelimitedFrom", InputStream.class);
      GeneratedMessage response = (GeneratedMessage) readDelimited.invoke(null,
          connectionPair.input());

      if (response == null) {
        // special case response == null means the stream has ended
        log.debug("Received null response from {}", agentName);
        throw new SimulationException("Network connection closed by " + agentName);
      }
      // cast and return
      return responseType.cast(response);
    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException |
             ClassCastException e) {
      log.error("Error decoding message from {}, expecting {}", agentName,
          responseType.getSimpleName(), e);
      throw new SimulationException(
          "Error decoding generated message from " + agentName + " expecting "
              + responseType.getSimpleName());
    } catch (InterruptedException e) {
      throw new SimulationException("Reading message while shutting down");
    }
  }

  @Override
  public void close() {
    try {
      serverSocket.close();
    } catch (IOException e) {
      log.error("Error closing server socket", e);
    }
  }

}
