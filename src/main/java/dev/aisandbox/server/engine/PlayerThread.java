package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.SynchronousQueue;

@Slf4j
public class PlayerThread extends Thread {

    private static final int MAX_PORT_TRIES = 10;
    private final String playerName;
    private final int defaultPort;
    ServerSocket serverSocket = null;
    OutputStream output = null;
    InputStream inputStream = null;
    SynchronousQueue<GeneratedMessage> inputQueue = new SynchronousQueue<>();
    SynchronousQueue<NetworkPlayerMessage> outputQueue = new SynchronousQueue<>();
    private final Class responseClass;

    public PlayerThread(String playerName, int defaultPort,Class responseClass) {
        this.playerName = playerName;
        this.defaultPort = defaultPort;
        this.responseClass = responseClass;
    }

    public synchronized void sendMessage(GeneratedMessage message) {
        try {
            outputQueue.put(new NetworkPlayerMessage(message, false));
        } catch (InterruptedException e) {
            log.error("Send interrupted", e);
        }
    }

    public synchronized GeneratedMessage sendMessageGetResponse(GeneratedMessage message) {
        GeneratedMessage response = null;
        try {
            outputQueue.put(new NetworkPlayerMessage(message, true));
            response = inputQueue.take();
        } catch (InterruptedException e) {
            log.error("send/recieve interrupted", e);
        }
        return response;
    }

    @Override
    public void run() {
        serverSocket = getServerSocket();
        System.out.println("Connect " + playerName + " to port: " + serverSocket.getLocalPort());
        try (Socket socket = serverSocket.accept()) {
            System.out.println(playerName + " connected");
            output = socket.getOutputStream();
            inputStream = socket.getInputStream();
            while (true) {
                NetworkPlayerMessage message = outputQueue.take();
                message.message().writeDelimitedTo(output);
                if (message.expectResponse()) {

                    Method readDelimited = responseClass.getMethod("parseDelimitedFrom", InputStream.class);

                    GeneratedMessage response = (GeneratedMessage) readDelimited.invoke(null, inputStream);

                    inputQueue.put(response);
                }
            }
        } catch (IOException e) {
            log.error("IO Exception from {}", playerName, e);
        } catch (InterruptedException e) {
            log.error("InterruptedException from {}", playerName, e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        log.info("Player {} thread finished", playerName);
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
            log.error("Unable to create server socket after {} tries", MAX_PORT_TRIES);
            System.out.println("Error, can't create server socket for " + playerName + " after " + MAX_PORT_TRIES + " tries");
            System.exit(1);
        }
        return socket;
    }

}
