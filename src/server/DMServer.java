package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import client.DMClientHandler;

public class DMServer {
    private ServerSocket serverSocket;
    private int port;
    private String dmKey;
    private static Map<String, DMServer> activeDMServers = new HashMap<>();

    public DMServer(ServerSocket serverSocket, int port, String dmKey) {
        this.serverSocket = serverSocket;
        this.port = port;
        this.dmKey = dmKey;
    }

    public void startServer() {
        try {
            System.out.println("DM Server started for " + dmKey + " on port " + port + ". Waiting for connections...");
            activeDMServers.put(dmKey, this);

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("New DM client connected: " + socket.getInetAddress() + " for DM: " + dmKey);
                DMClientHandler clientHandler = new DMClientHandler(socket, dmKey);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("DMServer.java : startServer - " + dmKey);
            e.printStackTrace();
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            activeDMServers.remove(dmKey);
            System.out.println("DM Server closed for " + dmKey);
        } catch (IOException e) {
            System.out.println("DMServer.java : closeServerSocket - " + dmKey);
            e.printStackTrace();
        }
    }

    public static DMServer createDMServer(String dmKey) {
        try {
            int port = findAvailablePort(2000);
            ServerSocket serverSocket = new ServerSocket(port);
            DMServer dmServer = new DMServer(serverSocket, port, dmKey);

            Thread serverThread = new Thread(() -> dmServer.startServer());
            serverThread.setDaemon(true);
            serverThread.start();

            return dmServer;
        } catch (IOException e) {
            System.out.println("Failed to create DM Server for " + dmKey);
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

    public static DMServer getDMServer(String dmKey) {
        return activeDMServers.get(dmKey);
    }

    public static void closeDMServer(String dmKey) {
        DMServer server = activeDMServers.get(dmKey);
        if (server != null) {
            server.closeServerSocket();
        }
    }

    public int getPort() {
        return port;
    }

    public String getDmKey() {
        return dmKey;
    }
}
