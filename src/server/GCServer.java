package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import client.GCClientHandler;

public class GCServer {
    private ServerSocket serverSocket;
    private int port;
    private String gcName;
    private String gcKey;
    private static Map<String, GCServer> activeGCServers = new HashMap<>();

    public GCServer(ServerSocket serverSocket, int port, String gcKey, String gcName) {
        this.serverSocket = serverSocket;
        this.port = port;
        this.gcKey = gcKey;
        this.gcName = gcName;
    }

    public void startServer() {
        try {
            System.out.println("GC Server started for '" + gcName + "' (" + gcKey + ") on port " + port + ". Waiting for connections...");
            activeGCServers.put(gcKey, this);

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("New GC client connected: " + socket.getInetAddress() + " for GC: " + gcName);
                GCClientHandler clientHandler = new GCClientHandler(socket, gcKey, gcName);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("GCServer.java : startServer - " + gcName);
            e.printStackTrace();
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            activeGCServers.remove(gcKey);
            System.out.println("GC Server closed for " + gcName);
        } catch (IOException e) {
            System.out.println("GCServer.java : closeServerSocket - " + gcName);
            e.printStackTrace();
        }
    }

    public static GCServer createGCServer(String gcKey, String gcName) {
        try {
            int port = findAvailablePort(3000);
            ServerSocket serverSocket = new ServerSocket(port);
            GCServer gcServer = new GCServer(serverSocket, port, gcKey, gcName);

            Thread serverThread = new Thread(() -> gcServer.startServer());
            serverThread.setDaemon(true);
            serverThread.start();

            return gcServer;
        } catch (IOException e) {
            System.out.println("Failed to create GC Server for " + gcName);
            e.printStackTrace();
            return null;
        }
    }

    public static int findAvailablePort(int startPort) {
        for (int port = startPort; port < startPort + 1000; port++) {
            try {
                ServerSocket testSocket = new ServerSocket(port);
                testSocket.close();
                return port;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("No available ports found");
    }

    public static GCServer getGCServer(String gcKey) {
        return activeGCServers.get(gcKey);
    }

    public static void closeGCServer(String gcKey) {
        GCServer server = activeGCServers.get(gcKey);
        if (server != null) {
            server.closeServerSocket();
        }
    }

    public int getPort() {
        return port;
    }

    public String getGcKey() {
        return gcKey;
    }

    public String getGcName() {
        return gcName;
    }
}
