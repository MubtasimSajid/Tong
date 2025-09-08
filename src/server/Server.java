package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import client.ClientHandler;

public class Server {
    public ServerSocket serverSocket;
    public int port;

    public Server(ServerSocket serverSocket, int port) {
        this.serverSocket = serverSocket;
        this.port = port;
    }

    public Server(ServerSocket serverSocket) {
        this(serverSocket, 1234);
    }

    public void startServer() {
        try {
            System.out.println("Server started on port 1234. Waiting for connections...");
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("Server.java : startServer");
            e.printStackTrace();
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Server.java : closeServerSocket");
        }
    }

    public static void main(String[] args) {
        try {
            int portVal = 1234;
            ServerSocket serverSocket = new ServerSocket(portVal);
            Server server = new Server(serverSocket, portVal);
            server.startServer();
        } catch (IOException e) {
            System.out.println("Server.java : main");
            e.printStackTrace();
        }
    }
}
