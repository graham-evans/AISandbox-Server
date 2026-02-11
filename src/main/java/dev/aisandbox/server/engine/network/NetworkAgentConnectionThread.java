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

/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.network;

import dev.aisandbox.server.engine.output.OutputRenderer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.SynchronousQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Thread for accepting network connections from AI agents.
 *
 * <p>This thread waits on a ServerSocket for an external AI agent to connect, then
 * establishes the communication channel by placing a ConnectionPair into a queue
 * for the main simulation thread to use.
 */
@Slf4j
@RequiredArgsConstructor
public class NetworkAgentConnectionThread extends Thread {

  private final String agentName;
  private final ServerSocket serverSocket;
  private final SynchronousQueue<ConnectionPair> connectionQueue;
  private final OutputRenderer renderer;

  @Override
  @SuppressWarnings("PMD.CloseResource")
  public void run() {
    log.info("Opening network agent for {}", agentName);
    try {
      Socket socket = serverSocket.accept();
      log.info("{} connected from {}", agentName, socket.getRemoteSocketAddress());
      renderer.write(agentName + " connected from " + socket.getRemoteSocketAddress());
      ConnectionPair connectionPair = new ConnectionPair(socket.getInputStream(),
          socket.getOutputStream());
      connectionQueue.put(connectionPair);
    } catch (IOException e) {
      log.warn("Error while opening network agent for {}", agentName, e);
    } catch (InterruptedException e) {
      log.warn("Canceled network connection for {}", agentName);
    }
  }

  /**
   * Represents a pair of input and output streams for agent communication.
   */
  public record ConnectionPair(InputStream input, OutputStream output) {

  }


}
