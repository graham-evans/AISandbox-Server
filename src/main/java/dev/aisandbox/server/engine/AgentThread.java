package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardsAction;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.SynchronousQueue;

@Slf4j
public class AgentThread extends Thread {

    private static final int MAX_PORT_TRIES = 10;
    private final String agentName;
    private final int defaultPort;
    ServerSocket serverSocket = null;
    OutputStream output = null;
    InputStream inputStream = null;
    SynchronousQueue<GeneratedMessage> inputQueue = new SynchronousQueue<>();
    SynchronousQueue<NetworkAgentMessage> outputQueue = new SynchronousQueue<>();

    public AgentThread(String agentName, int defaultPort) {
        this.agentName = agentName;
        this.defaultPort = defaultPort;
    }

    public synchronized void sendMessage(GeneratedMessage message) {
        try {
            outputQueue.put(new NetworkAgentMessage(message, Optional.empty()));
        } catch (InterruptedException e) {
            log.error("Send interrupted", e);
        }
    }

    public synchronized GeneratedMessage sendMessageGetResponse(GeneratedMessage message) {
        GeneratedMessage response = null;
        try {
            outputQueue.put(new NetworkAgentMessage(message, Optional.of(HighLowCardsAction.class)));
            response = inputQueue.take();
        } catch (InterruptedException e) {
            log.error("send/recieve interrupted", e);
        }
        return response;
    }

    @Override
    public void run() {
        serverSocket = getServerSocket();
        log.info("Connect {} to port: {}", agentName, serverSocket.getLocalPort());
        try (Socket socket = serverSocket.accept()) {
            log.info("{} connected", agentName);
            output = socket.getOutputStream();
            inputStream = socket.getInputStream();
            while (true) {

                NetworkAgentMessage outgoingMessage = outputQueue.take();

                outgoingMessage.message().writeDelimitedTo(output);

                if (outgoingMessage.expectedResponse().isPresent()) {
                    Method readDelimited = outgoingMessage.expectedResponse().get().getMethod("parseDelimitedFrom", InputStream.class);
                    GeneratedMessage response = (GeneratedMessage) readDelimited.invoke(null, inputStream);
                    if (response.getClass() == outgoingMessage.expectedResponse().get()) {
                        inputQueue.put(response);
                    } else {
                        log.error("Agent response error, sent {} object, looking for {} received {}",
                                outgoingMessage.message().getClass().getName(),
                                outgoingMessage.expectedResponse().get().getName(),
                                response.getClass().getName()
                        );
                        // TODO - respond to receiving the wrong response.
                        System.exit(-1);
                    }
                }

            }
        } catch (IOException e) {
            log.error("IO Exception from {}", agentName, e);
        } catch (InterruptedException e) {
            log.error("InterruptedException from {}", agentName, e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        log.info("Agent {} thread finished", agentName);
    }

    private ServerSocket getServerSocket() {
        int targetPort = defaultPort;
        int tries = 0;
        ServerSocket socket = null;
        while (socket == null && tries < MAX_PORT_TRIES) {
            try {
                log.info("Trying to create server socket on port {}", targetPort);
                socket = new ServerSocket(targetPort);
                log.info("Successfully created server socket on port {}", targetPort);
            } catch (IOException e) {
                log.warn("Failed to create server socket with port {}", targetPort, e);
                tries++;
                targetPort++;
            }
        }
        if (socket == null) {
            log.error("Unable to create server socket for {} after {} tries", agentName, MAX_PORT_TRIES);
            System.exit(1);
        }
        return socket;
    }

}
