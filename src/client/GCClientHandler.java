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

public class GCClientHandler implements Runnable {
    private static Map<String, ArrayList<GCClientHandler>> gcClientHandlers = new HashMap<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private String gcKey;
    private String gcName;

    public GCClientHandler(Socket socket, String gcKey, String gcName) {
        try {
            this.socket = socket;
            this.gcKey = gcKey;
            this.gcName = gcName;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();

            // Add to GC-specific client list
            gcClientHandlers.computeIfAbsent(gcKey, _ -> new ArrayList<>()).add(this);

            System.out.println("GC Client " + clientUsername + " joined GC: " + gcName);
            broadcastToGC("SERVER: " + clientUsername + " has joined " + gcName + "!");
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
                    broadcastToGC(messageFromClient);
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastToGC(String messageToSend) {
        ArrayList<GCClientHandler> gcClients = gcClientHandlers.get(gcKey);
        if (gcClients != null) {
            for (GCClientHandler clientHandler : gcClients) {
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
        ArrayList<GCClientHandler> gcClients = gcClientHandlers.get(gcKey);
        if (gcClients != null) {
            gcClients.remove(this);
            broadcastToGC("SERVER: " + clientUsername + " has left " + gcName + "!");

            // If no more clients, remove the GC entry
            if (gcClients.isEmpty()) {
                gcClientHandlers.remove(gcKey);
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

    public String getGcKey() {
        return gcKey;
    }

    public String getGcName() {
        return gcName;
    }
}
