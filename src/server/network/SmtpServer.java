package server.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class SmtpServer implements Runnable {

	private final String serverIP;
    private final int port;
    private boolean running;
    private Consumer<String> logCallback;
    private Consumer<String> uiEventCallback;

    public SmtpServer(String serverIP, int port, Consumer<String> logCallback) {
    	this(serverIP, port, logCallback, null);
    }
    
    public SmtpServer(String serverIP, int port, Consumer<String> logCallback, Consumer<String> uiEventCallback) {
        this.serverIP = serverIP;
        this.port = port;
        this.logCallback = logCallback;
        this.uiEventCallback = uiEventCallback;
    }

    public void start() {
        running = true;
        new Thread(this, "SMTP-Server-Thread").start();
    }

    public void stop() {
        running = false;
        log("Server stopped.");
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log("SMTP server started on port " + port);

            while (running) {
                Socket client = serverSocket.accept();
                log("Client connected: " + client.getInetAddress());
                new Thread(new SmtpClientHandler(serverIP, client, this::log, uiEventCallback)).start();
            }

        } catch (IOException e) {
            log("Error: " + e.getMessage());
        }
    }

    private void log(String msg) {
        if (logCallback != null)
            logCallback.accept(msg);
        else
            System.out.println(msg);
    }
}
