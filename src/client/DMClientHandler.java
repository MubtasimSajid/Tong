package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DMClientHandler implements Runnable {
    private static Map<String, ArrayList<DMClientHandler>> dmClientHandlers = new HashMap<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private String dmKey;

    public DMClientHandler(Socket socket, String dmKey) {
        try {
            this.socket = socket;
            this.dmKey = dmKey;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();

            // Add to DM-specific client list
            dmClientHandlers.computeIfAbsent(dmKey, _ -> new ArrayList<>()).add(this);

            System.out.println("DM Client " + clientUsername + " joined DM: " + dmKey);
            broadcastToDM("SERVER: " + clientUsername + " has joined the DM!");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient != null) {
                    broadcastToDM(messageFromClient);
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastToDM(String messageToSend) {
        ArrayList<DMClientHandler> dmClients = dmClientHandlers.get(dmKey);
        if (dmClients != null) {
            for (DMClientHandler clientHandler : dmClients) {
                try {
                    if (!clientHandler.clientUsername.equals(clientUsername)) {
                        clientHandler.bufferedWriter.write(messageToSend);
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    clientHandler.closeEverything(clientHandler.socket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
                }
            }
        }
    }

    public void removeClientHandler() {
        ArrayList<DMClientHandler> dmClients = dmClientHandlers.get(dmKey);
        if (dmClients != null) {
            dmClients.remove(this);
            broadcastToDM("SERVER: " + clientUsername + " has left the DM!");

            // If no more clients, remove the DM entry
            if (dmClients.isEmpty()) {
                dmClientHandlers.remove(dmKey);
            }
        }
    }

    public void closeEverything(Socket socket, BufferedReader br, BufferedWriter bw) {
        removeClientHandler();

        try {
            if (br != null) {
                br.close();
            }

            if (bw != null) {
                bw.close();
            }

            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getClientUsername() {
        return clientUsername;
    }

    public String getDmKey() {
        return dmKey;
    }
}
