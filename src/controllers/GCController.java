package controllers;

import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import client.ForumClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import models.User;
import server.GCServer;

public class GCController implements Initializable {
    @FXML private Button button_send;
    @FXML private TextField tf_message;
    @FXML private ScrollPane sp_main;
    @FXML private VBox vbox_messages;
    @FXML private Label headLabel;

    private User currentUser;
    private ForumClient forumClient;
    private boolean isConnectedToServer = false;
    private String currentGCKey;
    private String currentGCName;
    private GCServer currentGCServer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tf_message.setOnAction(_ -> sendMessage());
        tf_message.requestFocus();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setGCInfo(String gcName, String gcKey, GCServer gcServer) {
        this.currentGCName = gcName;
        this.currentGCKey = gcKey;
        this.currentGCServer = gcServer;

        connectToGCServer();
    }

    public void setGCInfoForJoining(String gcName, String gcKey, String ip, int port) {
        this.currentGCName = gcName;
        this.currentGCKey = gcKey;
        this.currentGCServer = null;

        connectToExistingGCServer(ip, port);
    }

    @FXML
    public void sendMessage() {
        String msg = tf_message.getText().trim();
        if (!msg.isEmpty() && currentUser != null && isConnectedToServer && forumClient != null) {
            forumClient.sendMessage(msg);

            displayOwnMessage(msg);

            tf_message.clear();
        } else if (!isConnectedToServer) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Not Connected");
            alert.setHeaderText("No Group Chat Connection");
            alert.setContentText("Please wait for connection to be established.");
            alert.showAndWait();
        }
    }

    private void connectToGCServer() {
        if (currentGCServer == null) return;

        try {
            Thread.sleep(1000);

            Socket socket = new Socket("localhost", currentGCServer.getPort());
            java.io.BufferedWriter bw = new java.io.BufferedWriter(
                new java.io.OutputStreamWriter(socket.getOutputStream()));
            bw.write(currentUser.displayName);
            bw.newLine();
            bw.flush();

            forumClient = new ForumClient(socket, currentUser.displayName);
            forumClient.setMessageListener(new ForumClient.ForumMessageListener() {
                @Override
                public void onMessageReceived(String message) {
                    Platform.runLater(() -> displayReceivedMessage(message));
                }

                @Override
                public void onConnectionStatusChanged(boolean connected) {
                    isConnectedToServer = connected;
                    if (!connected) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("GC Connection Lost");
                            alert.setHeaderText("Group Chat Connection Lost");
                            alert.setContentText("Connection to the Group Chat has been lost.");
                            alert.showAndWait();
                        });
                    }
                }
            });

            forumClient.listenForMessage();
            isConnectedToServer = true;

        } catch (Exception e) {
            Platform.runLater(() -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Connection Failed");
                errorAlert.setHeaderText("Cannot Connect to GC");
                errorAlert.setContentText("Failed to connect to Group Chat server: " + e.getMessage());
                errorAlert.showAndWait();
            });
        }
    }

    private void connectToExistingGCServer(String ip, int port) {
        try {
            Socket socket = new Socket(ip, port);
            java.io.BufferedWriter bw = new java.io.BufferedWriter(
                new java.io.OutputStreamWriter(socket.getOutputStream()));
            bw.write(currentUser.displayName);
            bw.newLine();
            bw.flush();

            forumClient = new ForumClient(socket, currentUser.displayName);
            forumClient.setMessageListener(new ForumClient.ForumMessageListener() {
                @Override
                public void onMessageReceived(String message) {
                    Platform.runLater(() -> displayReceivedMessage(message));
                }

                @Override
                public void onConnectionStatusChanged(boolean connected) {
                    isConnectedToServer = connected;
                    if (!connected) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("GC Connection Lost");
                            alert.setHeaderText("Group Chat Connection Lost");
                            alert.setContentText("Connection to the Group Chat has been lost.");
                            alert.showAndWait();
                        });
                    }
                }
            });

            forumClient.listenForMessage();
            isConnectedToServer = true;

        } catch (Exception e) {
            Platform.runLater(() -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Connection Failed");
                errorAlert.setHeaderText("Cannot Join GC");
                errorAlert.setContentText("Failed to join Group Chat server at " + ip + ":" + port + ": " + e.getMessage());
                errorAlert.showAndWait();
            });
        }
    }

    private void displayOwnMessage(String message) {
        String displayName = currentUser != null ? currentUser.displayName : "You";
        Text nameText = new Text(displayName + ": ");

        String userColor = currentUser != null ? currentUser.colorHex : "#2196F3";
        if (userColor.equalsIgnoreCase("#FFFFFF") || userColor.equalsIgnoreCase("#ffffff") ||
            userColor.equalsIgnoreCase("white") || userColor.equalsIgnoreCase("#FFF")) {
            userColor = "#2196F3";
        }

        nameText.setStyle("-fx-font-weight: bold; -fx-fill: " + userColor + ";");
        Text msgText = new Text(message);

        TextFlow flow = new TextFlow(nameText, msgText);
        flow.setPrefWidth(380);
        flow.setMaxWidth(380);

        vbox_messages.getChildren().add(flow);

        Platform.runLater(() -> sp_main.setVvalue(1.0));
    }

    private void displayReceivedMessage(String message) {
        Text msgText = new Text(message);
        TextFlow flow = new TextFlow(msgText);
        flow.setPrefWidth(380);
        flow.setMaxWidth(380);

        vbox_messages.getChildren().add(flow);

        Platform.runLater(() -> sp_main.setVvalue(1.0));
    }
}
