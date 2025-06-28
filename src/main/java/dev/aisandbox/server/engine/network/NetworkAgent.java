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
  private final SynchronousQueue<ConnectionPair> connectionQueue = new SynchronousQueue<>();
  private NetworkAgentConnectionThread.ConnectionPair connectionPair = null;


  public NetworkAgent(String agentName, int defaultPort, boolean openExternal)
      throws SimulationSetupException {
    this.agentName = agentName;
    serverSocket = getServerSocket(defaultPort, openExternal);
    NetworkAgentConnectionThread networkAgentConnectionThread = new NetworkAgentConnectionThread(
        agentName, serverSocket, connectionQueue);
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
      } catch (IOException e) {
        log.warn("Failed to create server socket with port {}", targetPort, e);
        tries++;
        targetPort++;
      }
    }
    if (socket == null) {
      log.error("Unable to create server socket for {} after {} tries", agentName, MAX_PORT_TRIES);
      throw new SimulationSetupException("Unable to create server socket for " + agentName);
    }
    return socket;
  }

  @Override
  public void send(GeneratedMessage o) throws SimulationException {
    try {
      if (connectionPair == null) {
        // wait for a connection
        connectionPair = connectionQueue.take();
      }
      // send the message
      log.debug("Sending {} to {}", o, agentName);
      o.writeDelimitedTo(connectionPair.output());
    } catch (IOException e) {
      log.error("IO exception while sending generated message to {}", agentName, e);
      throw new SimulationException("Error sending to " + agentName);
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
      log.debug("Asking agent thread for response type {}", responseType);

      Method readDelimited = responseType.getMethod("parseDelimitedFrom", InputStream.class);
      GeneratedMessage response = (GeneratedMessage) readDelimited.invoke(null,
          connectionPair.input());
      return responseType.cast(response);
    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
      log.error("Error receiving generated message from {}, expecting {}", agentName,
          responseType.getName(), e);
      throw new SimulationException("Error receiving generated message from " + agentName);
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
