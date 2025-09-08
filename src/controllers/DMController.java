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
import server.DMServer;

public class DMController implements Initializable {
    @FXML private Button button_send;
    @FXML private TextField tf_message;
    @FXML private ScrollPane sp_main;
    @FXML private VBox vbox_messages;
    @FXML private Label headLabel;

    private User currentUser;
    private User targetUser;
    private ForumClient forumClient;
    private boolean isConnectedToServer = false;
    private String currentDMKey;
    private DMServer currentDMServer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tf_message.setOnAction(_ -> sendMessage());
        tf_message.requestFocus();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setDMInfo(User target, String dmKey, DMServer dmServer) {
        this.targetUser = target;
        this.currentDMKey = dmKey;
        this.currentDMServer = dmServer;

        connectToDMServer();
    }

    public void setDMInfoForJoining(User target, String dmKey, String ip, int port) {
        this.targetUser = target;
        this.currentDMKey = dmKey;
        this.currentDMServer = null;

        connectToExistingDMServer(ip, port);
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
            alert.setHeaderText("No Direct Message Connection");
            alert.setContentText("Please wait for connection to be established.");
            alert.showAndWait();
        }
    }

    private void connectToDMServer() {
        if (currentDMServer == null) return;

        try {
            Thread.sleep(500);

            Socket socket = new Socket("localhost", currentDMServer.getPort());
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
                            alert.setTitle("DM Connection Lost");
                            alert.setHeaderText("Direct Message Connection Lost");
                            alert.setContentText("Connection to the DM has been lost.");
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
                errorAlert.setHeaderText("Cannot Connect to DM");
                errorAlert.setContentText("Failed to connect to DM server: " + e.getMessage());
                errorAlert.showAndWait();
            });
        }
    }

    private void connectToExistingDMServer(String ip, int port) {
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
                            alert.setTitle("DM Connection Lost");
                            alert.setHeaderText("Direct Message Connection Lost");
                            alert.setContentText("Connection to the DM has been lost.");
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
                errorAlert.setHeaderText("Cannot Join DM");
                errorAlert.setContentText("Failed to connect to existing DM server: " + e.getMessage());
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
