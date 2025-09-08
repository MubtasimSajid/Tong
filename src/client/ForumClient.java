package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javafx.application.Platform;

public class ForumClient {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private ForumMessageListener messageListener;

    public interface ForumMessageListener {
        void onMessageReceived(String message);
        void onConnectionStatusChanged(boolean connected);
    }

    public ForumClient(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void setMessageListener(ForumMessageListener listener) {
        this.messageListener = listener;
    }

    public void sendMessage(String messageToSend) {
        try {
            bufferedWriter.write(username + ": " + messageToSend);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage() {
        new Thread(() -> {
            String msgFromGroupChat;

            while (socket.isConnected()) {
                try {
                    msgFromGroupChat = bufferedReader.readLine();
                    if (msgFromGroupChat != null && messageListener != null) {
                        final String finalMessage = msgFromGroupChat;
                        try {
                            Platform.runLater(() -> messageListener.onMessageReceived(finalMessage));
                        } catch (IllegalStateException e) {
                            messageListener.onMessageReceived(finalMessage);
                        }
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
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
            if (messageListener != null) {
                try {
                    Platform.runLater(() -> messageListener.onConnectionStatusChanged(false));
                } catch (IllegalStateException e) {
                    messageListener.onConnectionStatusChanged(false);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
