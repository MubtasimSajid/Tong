package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.scene.layout.VBox;

public class Server {
    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader bufferReader;
    private BufferedWriter bufferWriter;

    public Server(ServerSocket serverSocket) {
        try {
            this.serverSocket = serverSocket;
            this.socket = serverSocket.accept();
            this.bufferReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("Error creating server");
            e.printStackTrace();
            closeEverything(socket, bufferReader, bufferWriter);
        }
    }

    public void sendMessageToClient(String messageToClient) {
        try {
            bufferWriter.write(messageToClient);
            bufferWriter.newLine();
            bufferWriter.flush();
        } catch (IOException e) {
            System.out.println("Error sending message to client");
            closeEverything(socket, bufferReader, bufferWriter);
            e.printStackTrace();
        }
    }

    public void receiveMessageFromClient(VBox vbox) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (socket.isConnected()) {
                    try {
                        String messageFromClient = bufferReader.readLine();
                        ServerController.addLabel(messageFromClient, vbox);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Error receiving message from client");
                        closeEverything(socket, bufferReader, bufferWriter);
                        break;
                    }
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }

            if (bufferedWriter != null) {
                bufferedWriter.close();
            }

            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
