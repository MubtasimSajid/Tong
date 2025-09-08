package controllers;

import java.io.IOException;
import java.net.Socket;

import client.ForumClient;
import database.UserDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import models.User;

public class RoomController {

    @FXML
    private AnchorPane ap_main;

    @FXML
    private Button button_dms;

    @FXML
    private Button button_edit_name;

    @FXML
    private Button button_forum;

    @FXML
    private Button button_gcs;

    @FXML
    private Button button_get_randid;

    @FXML
    private Button button_send;

    @FXML
    private Button button_set_randid;

    @FXML
    private ScrollPane sp_main;

    @FXML
    private TextField tf_message;

    @FXML
    private VBox vbox_messages;

    @FXML
    private Label headLabel;

    @FXML
    private Label detailsLabel;

    private User currentUser;
    private ForumClient forumClient;
    private boolean isConnectedToServer = false;

    private User currentDMUser;
    private String currentDMKey;

    private static java.util.Map<String, User> pendingDMRequests = new java.util.HashMap<>();
    private static java.util.Map<String, RoomController> activeControllers = new java.util.HashMap<>();

    private java.util.Map<String, String> savedDMConnections = new java.util.HashMap<>();
    private java.util.Map<String, String> savedGCConnections = new java.util.HashMap<>();

    private java.util.Map<String, java.util.List<javafx.scene.Node>> chatHistories = new java.util.HashMap<>();
    private String currentChatKey = "forum";

    public enum chatType {
        DM,
        GC,
        forum
    }

    chatType ct = chatType.forum;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        chatHistories.put("forum", new java.util.ArrayList<>());

        activeControllers.put(user.randomIdentifier, this);

        connectToServer();
    }

    private void saveChatHistory() {
        java.util.List<javafx.scene.Node> currentMessages = new java.util.ArrayList<>();
        for (javafx.scene.Node node : vbox_messages.getChildren()) {
            currentMessages.add(node);
        }
        chatHistories.put(currentChatKey, currentMessages);
    }

    private void restoreChatHistory(String chatKey) {
        saveChatHistory();

        vbox_messages.getChildren().clear();

        if (chatHistories.containsKey(chatKey)) {
            java.util.List<javafx.scene.Node> messages = chatHistories.get(chatKey);
            vbox_messages.getChildren().addAll(messages);
        }

        currentChatKey = chatKey;

        Platform.runLater(() -> {
            sp_main.setVvalue(1.0);
        });
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 1234);
            java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream()));
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
                            alert.setTitle("Connection Lost");
                            alert.setHeaderText("Server Connection Lost");
                            alert.setContentText("Connection to the server has been lost.");
                            alert.showAndWait();
                        });
                    }
                }
            });

            forumClient.listenForMessage();
            isConnectedToServer = true;

        } catch (IOException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Connection Error");
                alert.setHeaderText("Cannot Connect to Server");
                alert.setContentText("Failed to connect to the server. Please ensure the server is running.");
                alert.showAndWait();
            });
            isConnectedToServer = false;
        }
    }

    private void displayReceivedMessage(String message) {
        if (message.startsWith("SERVER:")) {
            return;
        }

        javafx.scene.Node messageNode = null;

        if (message.contains(":")) {
            String[] parts = message.split(":", 2);
            if (parts.length == 2) {
                String username = parts[0].trim();
                String content = parts[1].trim();

                Text nameText = new Text(username + ": ");
                nameText.setStyle("-fx-font-weight: bold; -fx-fill: #2196F3;");
                Text msgText = new Text(content);

                TextFlow flow = new TextFlow(nameText, msgText);
                flow.setPrefWidth(280);
                flow.setMaxWidth(280);

                nameText.setSelectionFill(javafx.scene.paint.Color.LIGHTBLUE);
                msgText.setSelectionFill(javafx.scene.paint.Color.LIGHTBLUE);

                javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
                javafx.scene.control.MenuItem copyItem = new javafx.scene.control.MenuItem("Copy Message");
                copyItem.setOnAction(e -> {
                    javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                    javafx.scene.input.ClipboardContent clipboardContent = new javafx.scene.input.ClipboardContent();
                    clipboardContent.putString(username + ": " + content);
                    clipboard.setContent(clipboardContent);
                });
                contextMenu.getItems().add(copyItem);
                flow.setOnContextMenuRequested(e -> contextMenu.show(flow, e.getScreenX(), e.getScreenY()));

                messageNode = flow;
            } else {
                Label label = new Label(message);
                label.setStyle("-fx-text-fill: #757575; -fx-font-style: italic;");
                label.setWrapText(true);
                label.setPrefWidth(280);
                label.setMaxWidth(280);

                javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
                javafx.scene.control.MenuItem copyItem = new javafx.scene.control.MenuItem("Copy Message");
                copyItem.setOnAction(e -> {
                    javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                    javafx.scene.input.ClipboardContent clipboardContent = new javafx.scene.input.ClipboardContent();
                    clipboardContent.putString(message);
                    clipboard.setContent(clipboardContent);
                });
                contextMenu.getItems().add(copyItem);
                label.setOnContextMenuRequested(e -> contextMenu.show(label, e.getScreenX(), e.getScreenY()));

                messageNode = label;
            }
        } else {
            if (!message.startsWith("SERVER:")) {
                Label label = new Label(message);
                label.setStyle("-fx-text-fill: #757575; -fx-font-style: italic;");
                label.setWrapText(true);
                label.setPrefWidth(280);
                label.setMaxWidth(280);

                javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
                javafx.scene.control.MenuItem copyItem = new javafx.scene.control.MenuItem("Copy Message");
                copyItem.setOnAction(e -> {
                    javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                    javafx.scene.input.ClipboardContent clipboardContent = new javafx.scene.input.ClipboardContent();
                    clipboardContent.putString(message);
                    clipboard.setContent(clipboardContent);
                });
                contextMenu.getItems().add(copyItem);
                label.setOnContextMenuRequested(e -> contextMenu.show(label, e.getScreenX(), e.getScreenY()));

                messageNode = label;
            }
        }

        if (messageNode != null) {
            vbox_messages.getChildren().add(messageNode);

            if (chatHistories.containsKey(currentChatKey)) {
                chatHistories.get(currentChatKey).add(messageNode);
            }
        }

        Platform.runLater(() -> {
            sp_main.setVvalue(1.0);
        });
    }

    public void getRandID() {
        if (currentUser != null && currentUser.randomIdentifier != null) {
            javafx.scene.control.TextField idField = new javafx.scene.control.TextField(currentUser.randomIdentifier);
            idField.setEditable(false);
            idField.setPrefWidth(300);

            Button copyBtn = new Button("Copy");
            copyBtn.setOnAction(e -> {
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(currentUser.randomIdentifier);
                clipboard.setContent(content);
            });

            VBox vbox = new VBox(10, idField, copyBtn);
            vbox.setPrefWidth(320);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Random ID");
            alert.setHeaderText("Your Random Identifier");
            alert.getDialogPane().setContent(vbox);
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Random ID Not Found");
            alert.setContentText("No random identifier found for the current user.");
            alert.showAndWait();
        }
    }

    public void setRandID() {
        if (currentUser != null) {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            StringBuilder sb = new StringBuilder(10);
            for (int i = 0; i < 10; i++) {
                int idx = (int) (Math.random() * chars.length());
                sb.append(chars.charAt(idx));
            }
            currentUser.randomIdentifier = sb.toString();
            UserDAO.updateUserRandomID(currentUser.emailAddress, currentUser.randomIdentifier);

            javafx.scene.control.TextField idField = new javafx.scene.control.TextField(currentUser.randomIdentifier);
            idField.setEditable(false);
            idField.setPrefWidth(300);

            Button copyBtn = new Button("Copy");
            copyBtn.setOnAction(e -> {
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(currentUser.randomIdentifier);
                clipboard.setContent(content);
            });

            VBox vbox = new VBox(10, idField, copyBtn);
            vbox.setPrefWidth(320);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Random ID Set");
            alert.setHeaderText("Random Identifier Updated");
            alert.getDialogPane().setContent(vbox);
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No User Logged In");
            alert.setContentText("Please log in to set a random identifier.");
            alert.showAndWait();
        }
    }

    public void editDisplayName() {
        TextField nameField = new TextField();
        nameField.setPromptText("Enter new display name");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Edit Display Name");
        alert.setHeaderText("Change your display name");
        alert.getDialogPane().setContent(nameField);

        alert.showAndWait().ifPresent(response -> {
            String newName = nameField.getText().trim();
            if (!newName.isEmpty() && currentUser != null) {
                currentUser.displayName = newName;
                UserDAO.updateUserDisplayName(currentUser.emailAddress, newName);

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Display Name Updated");
                success.setHeaderText(null);
                success.setContentText("Your display name has been updated to: " + newName);
                success.showAndWait();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("Invalid Name");
                error.setContentText("Please enter a valid display name.");
                error.showAndWait();
            }
        });
    }

    public void sendMessage() {
        String msg = tf_message.getText().trim();
        if (!msg.isEmpty() && currentUser != null) {
            if (isConnectedToServer && forumClient != null) {
                forumClient.sendMessage(msg);

                displayOwnMessage(msg);

                tf_message.clear();
            } else {
                displayOwnMessage(msg);
                tf_message.clear();

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Not Connected");
                alert.setHeaderText("Server Connection Issue");
                alert.setContentText("Message sent locally only. Not connected to server.");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot Send Message");
            alert.setContentText("Please enter a message and ensure you are logged in.");
            alert.showAndWait();
        }
    }

    public void receiveDMMessage(User sender, String message, String dmKey) {
        String formattedMessage = sender.displayName + ": " + message;

        if (ct == chatType.DM && currentDMKey != null && currentDMKey.equals(dmKey)) {
            displayReceivedMessage(formattedMessage);
        } else {
            if (!chatHistories.containsKey(dmKey)) {
                chatHistories.put(dmKey, new java.util.ArrayList<>());
            }

            Text nameText = new Text(sender.displayName + ": ");
            nameText.setStyle("-fx-font-weight: bold; -fx-fill: #2196F3;");
            Text msgText = new Text(message);

            TextFlow flow = new TextFlow(nameText, msgText);
            flow.setPrefWidth(280);
            flow.setMaxWidth(280);

            nameText.setSelectionFill(javafx.scene.paint.Color.LIGHTBLUE);
            msgText.setSelectionFill(javafx.scene.paint.Color.LIGHTBLUE);

            // Add context menu for copying
            javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
            javafx.scene.control.MenuItem copyItem = new javafx.scene.control.MenuItem("Copy Message");
            copyItem.setOnAction(e -> {
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent clipboardContent = new javafx.scene.input.ClipboardContent();
                clipboardContent.putString(formattedMessage);
                clipboard.setContent(clipboardContent);
            });
            contextMenu.getItems().add(copyItem);
            flow.setOnContextMenuRequested(e -> contextMenu.show(flow, e.getScreenX(), e.getScreenY()));

            // Add to DM chat history
            chatHistories.get(dmKey).add(flow);

            // Show notification that a message was received
            Platform.runLater(() -> {
                Alert notificationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                notificationAlert.setTitle("New DM Message");
                notificationAlert.setHeaderText("Message from " + sender.displayName);
                notificationAlert.setContentText("You received a new direct message: \"" + message + "\"\n\nWould you like to switch to this DM?");

                javafx.scene.control.ButtonType switchButton = new javafx.scene.control.ButtonType("Switch to DM", javafx.scene.control.ButtonBar.ButtonData.YES);
                javafx.scene.control.ButtonType stayButton = new javafx.scene.control.ButtonType("Stay Here", javafx.scene.control.ButtonBar.ButtonData.NO);

                notificationAlert.getButtonTypes().setAll(switchButton, stayButton);

                java.util.Optional<javafx.scene.control.ButtonType> result = notificationAlert.showAndWait();

                if (result.isPresent() && result.get() == switchButton) {
                    // Switch to this DM
                    this.currentDMUser = sender;
                    this.currentDMKey = dmKey;

                    // Update UI
                    headLabel.setText("DM: " + sender.displayName);
                    detailsLabel.setText("Connected to " + sender.displayName + " (" + sender.randomIdentifier + ")");
                    detailsLabel.setVisible(true);

                    // Switch to DM view
                    switchToDM(dmKey);
                }
            });
        }
    }

    private void displayOwnMessage(String msg) {
        if (currentUser.colorHex == null || currentUser.colorHex.isEmpty()) {
            String[] colors = {
                "#e57373", "#f06292", "#ba68c8", "#64b5f6", "#4db6ac",
                "#81c784", "#ffd54f", "#ffb74d", "#a1887f", "#90a4ae"
            };
            int idx = (int) (Math.random() * colors.length);
            currentUser.colorHex = colors[idx];
        }

        Text nameText = new Text(currentUser.displayName + ": ");
        nameText.setStyle("-fx-font-weight: bold; -fx-fill: " + currentUser.colorHex + ";");
        Text msgText = new Text(msg);

        TextFlow flow = new TextFlow(nameText, msgText);
        flow.setPrefWidth(280); // Set preferred width to match ScrollPane
        flow.setMaxWidth(280);  // Ensure maximum width for wrapping

        // Make text selectable
        nameText.setSelectionFill(javafx.scene.paint.Color.LIGHTBLUE);
        msgText.setSelectionFill(javafx.scene.paint.Color.LIGHTBLUE);

        // Add context menu for copying own messages
        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
        javafx.scene.control.MenuItem copyItem = new javafx.scene.control.MenuItem("Copy Message");
        copyItem.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent clipboardContent = new javafx.scene.input.ClipboardContent();
            clipboardContent.putString(currentUser.displayName + ": " + msg);
            clipboard.setContent(clipboardContent);
        });
        contextMenu.getItems().add(copyItem);
        flow.setOnContextMenuRequested(e -> contextMenu.show(flow, e.getScreenX(), e.getScreenY()));

        vbox_messages.getChildren().add(flow);

        // Update chat history
        if (chatHistories.containsKey(currentChatKey)) {
            chatHistories.get(currentChatKey).add(flow);
        }

        Platform.runLater(() -> {
            sp_main.setVvalue(1.0);
        });
    }

    @FXML
    public void initialize() {
        tf_message.setOnAction(event -> sendMessage());
        tf_message.requestFocus();
        headLabel.setText("Forum");
        detailsLabel.setVisible(false);
    }

    public void goToForum() {
        restoreChatHistory("forum");
        headLabel.setText("Forum");
        detailsLabel.setVisible(false);
        ct = chatType.forum;

        // Reconnect to main forum server if not connected
        if (!isConnectedToServer || forumClient == null) {
            connectToServer();
        }
    }

    public void joinGC() {
        // Create custom dialog for GC connection
        Alert gcDialog = new Alert(Alert.AlertType.NONE);
        gcDialog.setTitle("Group Chat");
        gcDialog.setHeaderText("Group Chat Options");

        // Create UI components
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField gcNameField = new TextField();
        gcNameField.setPromptText("Enter Group Chat Name");
        gcNameField.setText("My Group Chat");

        TextField ipField = new TextField();
        ipField.setPromptText("Enter IP Address (for joining existing GC)");
        ipField.setText("localhost");

        TextField portField = new TextField();
        portField.setPromptText("Enter Port (for joining existing GC)");
        portField.setText("3000");

        grid.add(new Label("GC Name:"), 0, 0);
        grid.add(gcNameField, 1, 0);
        grid.add(new Label("IP Address (join only):"), 0, 1);
        grid.add(ipField, 1, 1);
        grid.add(new Label("Port (join only):"), 0, 2);
        grid.add(portField, 1, 2);

        gcDialog.getDialogPane().setContent(grid);

        // Create custom buttons
        javafx.scene.control.ButtonType createButtonType = new javafx.scene.control.ButtonType("Create New GC", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        javafx.scene.control.ButtonType joinButtonType = new javafx.scene.control.ButtonType("Join Existing GC", javafx.scene.control.ButtonBar.ButtonData.APPLY);
        javafx.scene.control.ButtonType cancelButtonType = new javafx.scene.control.ButtonType("Cancel", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        gcDialog.getDialogPane().getButtonTypes().addAll(createButtonType, joinButtonType, cancelButtonType);

        // Handle button clicks
        java.util.Optional<javafx.scene.control.ButtonType> result = gcDialog.showAndWait();

        if (result.isPresent()) {
            String gcName = gcNameField.getText().trim();
            String ip = ipField.getText().trim();
            String portText = portField.getText().trim();

            if (gcName.isEmpty()) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Invalid Input");
                errorAlert.setHeaderText("Missing Information");
                errorAlert.setContentText("Please enter a Group Chat name.");
                errorAlert.showAndWait();
                return;
            }

            if (result.get() == createButtonType) {
                createNewGroupChat(gcName);
            } else if (result.get() == joinButtonType) {
                if (ip.isEmpty() || portText.isEmpty()) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Invalid Input");
                    errorAlert.setHeaderText("Missing Information");
                    errorAlert.setContentText("Please enter both IP address and port number to join an existing Group Chat.");
                    errorAlert.showAndWait();
                    return;
                }

                try {
                    int port = Integer.parseInt(portText);
                    joinExistingGroupChat(gcName, ip, port);
                } catch (NumberFormatException e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Invalid Port");
                    errorAlert.setHeaderText("Invalid Port Number");
                    errorAlert.setContentText("Please enter a valid port number.");
                    errorAlert.showAndWait();
                }
            }
        }
    }

    private void createNewGroupChat(String gcName) {
        try {
            // Create unique key for this group chat
            String gcKey = "gc_" + gcName.replaceAll("\\s+", "_") + "_" + System.currentTimeMillis();

            // Create GC server
            server.GCServer gcServer = server.GCServer.createGCServer(gcKey, gcName);
            if (gcServer == null) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Server Creation Failed");
                errorAlert.setHeaderText("Cannot Create GC Server");
                errorAlert.setContentText("Failed to create Group Chat server.");
                errorAlert.showAndWait();
                return;
            }

            // Load the GC FXML
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/gc.fxml"));
            javafx.scene.Parent root = loader.load();

            // Get the controller and set the information
            controllers.GCController gcController = loader.getController();
            gcController.setCurrentUser(currentUser);
            gcController.setGCInfo(gcName, gcKey, gcServer);

            // Create new stage
            javafx.stage.Stage gcStage = new javafx.stage.Stage();
            gcStage.setTitle("Tong - Group Chat: " + gcName);
            gcStage.setScene(new javafx.scene.Scene(root));
            gcStage.setResizable(false);

            // Show the window
            gcStage.show();

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Group Chat Created");
            successAlert.setHeaderText("Server Started");
            successAlert.setContentText(
                "Group Chat '" + gcName + "' created successfully!\n" +
                "Port: " + gcServer.getPort() + "\n\n" +
                "Share this port with others to join your Group Chat."
            );
            successAlert.showAndWait();

        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Cannot Create Group Chat");
            errorAlert.setContentText("Failed to create Group Chat: " + e.getMessage());
            errorAlert.showAndWait();
            e.printStackTrace();
        }
    }

    private void joinExistingGroupChat(String gcName, String ip, int port) {
        try {
            // Create a unique key for joining
            String gcKey = "gc_join_" + gcName.replaceAll("\\s+", "_") + "_" + ip.replaceAll("\\.", "_") + "_" + port;

            // Load the GC FXML
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/gc.fxml"));
            javafx.scene.Parent root = loader.load();

            // Get the controller and set the information
            controllers.GCController gcController = loader.getController();
            gcController.setCurrentUser(currentUser);
            gcController.setGCInfoForJoining(gcName, gcKey, ip, port);

            // Create new stage
            javafx.stage.Stage gcStage = new javafx.stage.Stage();
            gcStage.setTitle("Tong - Group Chat: " + gcName + " (" + ip + ":" + port + ")");
            gcStage.setScene(new javafx.scene.Scene(root));
            gcStage.setResizable(false);

            // Show the window
            gcStage.show();

        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Cannot Join Group Chat");
            errorAlert.setContentText("Failed to join Group Chat: " + e.getMessage());
            errorAlert.showAndWait();
            e.printStackTrace();
        }
    }

    private void joinGroupChat(String gcName, String ip, int port) {
        try {
            // Create unique key for this group chat
            String gcKey = "gc_" + ip + "_" + port + "_" + gcName.replaceAll("\\s+", "_");

            // Initialize chat history for this GC if it doesn't exist
            if (!chatHistories.containsKey(gcKey)) {
                chatHistories.put(gcKey, new java.util.ArrayList<>());
            }

            // Restore chat history for this group chat
            restoreChatHistory(gcKey);

            // Disconnect from current server if connected
            if (forumClient != null) {
                forumClient.closeEverything(null, null, null);
            }

            // Try to connect to existing GC server or create one
            server.GCServer gcServer = server.GCServer.getGCServer(gcKey);
            if (gcServer == null) {
                // Server doesn't exist, try to create one at the specified port
                try {
                    gcServer = server.GCServer.createGCServer(gcKey, gcName);
                    if (gcServer == null) {
                        throw new IOException("Failed to create GC server");
                    }
                    // Wait for server to start
                    Thread.sleep(1000);
                } catch (Exception e) {
                    // If we can't create server, try to connect to existing one
                }
            }

            // Connect to the GC server
            int connectPort = (gcServer != null) ? gcServer.getPort() : port;
            Socket socket = new Socket(ip, connectPort);
            java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream()));
            bw.write(currentUser.displayName);
            bw.newLine();
            bw.flush();

            // Create new ForumClient for group chat
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
                            alert.setContentText("Connection to the group chat has been lost.");
                            alert.showAndWait();
                        });
                    }
                }
            });

            forumClient.listenForMessage();
            isConnectedToServer = true;

            // Update UI with GC name
            headLabel.setText("Group Chat");
            detailsLabel.setText(gcName);
            detailsLabel.setVisible(true);

            // Save this GC connection
            savedGCConnections.put(ip + ":" + connectPort, gcName);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Connected");
            successAlert.setHeaderText("Group Chat Joined");
            successAlert.setContentText("Successfully connected to " + gcName + " at " + ip + ":" + connectPort);
            successAlert.showAndWait();

        } catch (IOException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Connection Failed");
            errorAlert.setHeaderText("Cannot Join Group Chat");
            errorAlert.setContentText("Failed to connect to " + gcName + " at " + ip + ":" + port +
                                    "\nPlease check the IP address and port, and ensure the server is running.");
            errorAlert.showAndWait();
            isConnectedToServer = false;
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Connection Failed");
            errorAlert.setHeaderText("Cannot Join Group Chat");
            errorAlert.setContentText("Failed to connect to " + gcName + ": " + e.getMessage());
            errorAlert.showAndWait();
            isConnectedToServer = false;
        }
    }    private void createGroupChat(String gcName, String ip, int port) {
        try {
            // Create unique key for this group chat
            String gcKey = "gc_" + ip + "_" + port + "_" + gcName.replaceAll("\\s+", "_");

            // Create GC server
            server.GCServer gcServer = server.GCServer.createGCServer(gcKey, gcName);
            if (gcServer == null) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Server Creation Failed");
                errorAlert.setHeaderText("Cannot Create Group Chat Server");
                errorAlert.setContentText("Failed to create group chat server.");
                errorAlert.showAndWait();
                return;
            }

            // Wait a moment for server to start, then connect to it
            Thread.sleep(1000);

            // Connect to the newly created server
            joinGroupChat(gcName, "localhost", gcServer.getPort());

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Group Chat Created");
            successAlert.setHeaderText("Server Started");
            successAlert.setContentText("Group chat '" + gcName + "' created on port " + gcServer.getPort() +
                                      "\nShare 'localhost:" + gcServer.getPort() + "' with others to join your group chat!");
            successAlert.showAndWait();

        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Server Error");
            errorAlert.setHeaderText("Group Chat Creation Failed");
            errorAlert.setContentText("Failed to create group chat server: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    public void enterDM() {
        // Create custom dialog for DM options
        Alert dmDialog = new Alert(Alert.AlertType.NONE);
        dmDialog.setTitle("Direct Message");
        dmDialog.setHeaderText("Direct Message Options");

        // Create UI components
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField targetUserField = new TextField();
        targetUserField.setPromptText("Enter target user's Random ID");

        TextField ipField = new TextField();
        ipField.setPromptText("Enter IP Address (for joining existing DM)");
        ipField.setText("localhost");

        TextField portField = new TextField();
        portField.setPromptText("Enter Port (for joining existing DM)");
        portField.setText("2000");

        grid.add(new Label("Target User ID:"), 0, 0);
        grid.add(targetUserField, 1, 0);
        grid.add(new Label("IP Address (join only):"), 0, 1);
        grid.add(ipField, 1, 1);
        grid.add(new Label("Port (join only):"), 0, 2);
        grid.add(portField, 1, 2);

        dmDialog.getDialogPane().setContent(grid);

        // Create custom buttons
        javafx.scene.control.ButtonType createButtonType = new javafx.scene.control.ButtonType("Create New DM", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        javafx.scene.control.ButtonType joinButtonType = new javafx.scene.control.ButtonType("Join Existing DM", javafx.scene.control.ButtonBar.ButtonData.APPLY);
        javafx.scene.control.ButtonType cancelButtonType = new javafx.scene.control.ButtonType("Cancel", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        dmDialog.getDialogPane().getButtonTypes().addAll(createButtonType, joinButtonType, cancelButtonType);

        // Handle button clicks
        java.util.Optional<javafx.scene.control.ButtonType> result = dmDialog.showAndWait();

        if (result.isPresent()) {
            String targetUserID = targetUserField.getText().trim();
            String ip = ipField.getText().trim();
            String portText = portField.getText().trim();

            if (result.get() == createButtonType) {
                if (targetUserID.isEmpty()) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Invalid Input");
                    errorAlert.setHeaderText("Missing Information");
                    errorAlert.setContentText("Please enter the target user's Random ID.");
                    errorAlert.showAndWait();
                    return;
                }

                // Check if the random ID exists in database
                User targetUser = UserDAO.getUserByRandomID(targetUserID);

                if (targetUser == null) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("User Not Found");
                    errorAlert.setHeaderText("Invalid Random ID");
                    errorAlert.setContentText("No user found with the Random ID: " + targetUserID);
                    errorAlert.showAndWait();
                    return;
                }

                if (targetUser.randomIdentifier.equals(currentUser.randomIdentifier)) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Invalid Target");
                    errorAlert.setHeaderText("Cannot DM Yourself");
                    errorAlert.setContentText("You cannot start a DM with yourself.");
                    errorAlert.showAndWait();
                    return;
                }

                createDirectMessage(targetUser);
            } else if (result.get() == joinButtonType) {
                if (targetUserID.isEmpty() || portText.isEmpty()) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Invalid Input");
                    errorAlert.setHeaderText("Missing Information");
                    errorAlert.setContentText("Please enter both target user ID and port number.");
                    errorAlert.showAndWait();
                    return;
                }

                try {
                    int port = Integer.parseInt(portText);
                    User targetUser = UserDAO.getUserByRandomID(targetUserID);
                    if (targetUser == null) {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("User Not Found");
                        errorAlert.setHeaderText("Invalid Random ID");
                        errorAlert.setContentText("No user found with the Random ID: " + targetUserID);
                        errorAlert.showAndWait();
                        return;
                    }

                    // Use localhost if IP is empty
                    if (ip.isEmpty()) {
                        ip = "localhost";
                    }

                    joinExistingDirectMessage(targetUser, ip, port);
                } catch (NumberFormatException e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Invalid Port");
                    errorAlert.setHeaderText("Invalid Port Number");
                    errorAlert.setContentText("Please enter a valid port number.");
                    errorAlert.showAndWait();
                }
            }
        }
    }

    private void createDirectMessage(User targetUser) {
        try {
            // Create unique DM key
            String dmKey = createDMKey(currentUser.randomIdentifier, targetUser.randomIdentifier);

            // Create DM server
            server.DMServer dmServer = server.DMServer.createDMServer(dmKey);
            if (dmServer == null) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Server Creation Failed");
                errorAlert.setHeaderText("Cannot Create DM Server");
                errorAlert.setContentText("Failed to create Direct Message server.");
                errorAlert.showAndWait();
                return;
            }

            // Load the DM FXML
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/dm.fxml"));
            javafx.scene.Parent root = loader.load();

            // Get the controller and set the information
            controllers.DMController dmController = loader.getController();
            dmController.setCurrentUser(currentUser);
            dmController.setDMInfo(targetUser, dmKey, dmServer);

            // Create new stage
            javafx.stage.Stage dmStage = new javafx.stage.Stage();
            dmStage.setTitle("Tong - DM: " + targetUser.displayName);
            dmStage.setScene(new javafx.scene.Scene(root));
            dmStage.setResizable(false);

            // Show the window
            dmStage.show();

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Direct Message Created");
            successAlert.setHeaderText("DM Server Started");
            successAlert.setContentText(
                "Direct Message with " + targetUser.displayName + " created successfully!\n" +
                "Port: " + dmServer.getPort() + "\n\n" +
                "Share this port with " + targetUser.displayName + " to connect:\n" +
                "They should use 'Join Existing DM' with port " + dmServer.getPort() +
                " and your Random ID: " + currentUser.randomIdentifier
            );
            successAlert.showAndWait();

        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Cannot Create Direct Message");
            errorAlert.setContentText("Failed to create Direct Message: " + e.getMessage());
            errorAlert.showAndWait();
            e.printStackTrace();
        }
    }

    private void joinExistingDirectMessage(User targetUser, String ip, int port) {
        try {
            // Create unique DM key
            String dmKey = createDMKey(currentUser.randomIdentifier, targetUser.randomIdentifier);

            // Load the DM FXML
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/dm.fxml"));
            javafx.scene.Parent root = loader.load();

            // Get the controller and set the information
            controllers.DMController dmController = loader.getController();
            dmController.setCurrentUser(currentUser);
            dmController.setDMInfoForJoining(targetUser, dmKey, ip, port);

            // Create new stage
            javafx.stage.Stage dmStage = new javafx.stage.Stage();
            dmStage.setTitle("Tong - DM: " + targetUser.displayName);
            dmStage.setScene(new javafx.scene.Scene(root));
            dmStage.setResizable(false);

            // Show the window
            dmStage.show();

        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Cannot Join Direct Message");
            errorAlert.setContentText("Failed to join Direct Message: " + e.getMessage());
            errorAlert.showAndWait();
            e.printStackTrace();
        }
    }

    private void sendDMRequest(User targetUser) {
        // Check if target user is online (has an active controller)
        RoomController targetController = activeControllers.get(targetUser.randomIdentifier);

        if (targetController == null) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("User Offline");
            errorAlert.setHeaderText("User Not Available");
            errorAlert.setContentText(targetUser.displayName + " is not currently online.");
            errorAlert.showAndWait();
            return;
        }

        // Create unique DM key
        String dmKey = createDMKey(currentUser.randomIdentifier, targetUser.randomIdentifier);

        // Store the pending request
        pendingDMRequests.put(dmKey, currentUser);

        // Create the DM waiting room for the requester
        createDMWaitingRoom(targetUser, dmKey);

        // Show request to target user
        Platform.runLater(() -> targetController.showDMRequest(currentUser, dmKey));

        Alert waitingAlert = new Alert(Alert.AlertType.INFORMATION);
        waitingAlert.setTitle("DM Request Sent");
        waitingAlert.setHeaderText("Waiting for Response");
        waitingAlert.setContentText("DM request sent to " + targetUser.displayName + ". Waiting for their response...");
        waitingAlert.showAndWait();
    }

    private void createDMWaitingRoom(User targetUser, String dmKey) {
        // Initialize DM chat history if it doesn't exist
        if (!chatHistories.containsKey(dmKey)) {
            chatHistories.put(dmKey, new java.util.ArrayList<>());
        }

        // Restore chat history for this DM
        restoreChatHistory(dmKey);

        // Update UI
        headLabel.setText("DM: " + targetUser.displayName + " (Waiting...)");
        detailsLabel.setText("Waiting for " + targetUser.displayName + " to accept your DM request");
        detailsLabel.setVisible(true);

        // Start DM server/client system in waiting mode
        setupDMConnection(targetUser, dmKey);
    }

    public void showDMRequest(User requesterUser, String dmKey) {
        // Create custom alert for DM request
        Alert requestAlert = new Alert(Alert.AlertType.CONFIRMATION);
        requestAlert.setTitle("DM Request");
        requestAlert.setHeaderText("Direct Message Request");
        requestAlert.setContentText(requesterUser.displayName + " wants to start a direct message conversation with you.\n\nDo you want to accept?");

        // Custom buttons
        javafx.scene.control.ButtonType acceptButton = new javafx.scene.control.ButtonType("Accept", javafx.scene.control.ButtonBar.ButtonData.YES);
        javafx.scene.control.ButtonType declineButton = new javafx.scene.control.ButtonType("Decline", javafx.scene.control.ButtonBar.ButtonData.NO);

        requestAlert.getButtonTypes().setAll(acceptButton, declineButton);

        java.util.Optional<javafx.scene.control.ButtonType> result = requestAlert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == acceptButton) {
                acceptDMRequest(requesterUser, dmKey);
            } else {
                declineDMRequest(requesterUser, dmKey);
            }
        } else {
            // Dialog was closed without selection - treat as decline
            declineDMRequest(requesterUser, dmKey);
        }
    }

    private void acceptDMRequest(User requesterUser, String dmKey) {
        // Remove from pending requests
        pendingDMRequests.remove(dmKey);

        // Initialize DM chat history if it doesn't exist
        if (!chatHistories.containsKey(dmKey)) {
            chatHistories.put(dmKey, new java.util.ArrayList<>());
        }

        // Restore chat history for this DM
        restoreChatHistory(dmKey);

        // Update UI
        headLabel.setText("DM: " + requesterUser.displayName);
        detailsLabel.setText("Connected to " + requesterUser.displayName + " (" + requesterUser.randomIdentifier + ")");
        detailsLabel.setVisible(true);

        // Start DM server/client system
        setupDMConnection(requesterUser, dmKey);

        // Save this DM connection
        savedDMConnections.put(requesterUser.randomIdentifier, requesterUser.displayName);

        // Notify requester that request was accepted
        RoomController requesterController = activeControllers.get(requesterUser.randomIdentifier);
        if (requesterController != null) {
            Platform.runLater(() -> requesterController.onDMRequestAccepted(currentUser));
        }

        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("DM Started");
        successAlert.setHeaderText("Direct Message Accepted");
        successAlert.setContentText("You are now connected with " + requesterUser.displayName);
        successAlert.showAndWait();
    }

    private void declineDMRequest(User requesterUser, String dmKey) {
        // Remove from pending requests
        pendingDMRequests.remove(dmKey);

        // Notify requester that request was declined
        RoomController requesterController = activeControllers.get(requesterUser.randomIdentifier);
        if (requesterController != null) {
            Platform.runLater(() -> requesterController.onDMRequestDeclined(currentUser));
        }
    }

    public void onDMRequestAccepted(User acceptingUser) {
        // Update UI to show connection is established
        headLabel.setText("DM: " + acceptingUser.displayName);
        detailsLabel.setText("Connected to " + acceptingUser.displayName + " (" + acceptingUser.randomIdentifier + ")");

        // Save this DM connection
        savedDMConnections.put(acceptingUser.randomIdentifier, acceptingUser.displayName);

        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("DM Request Accepted");
        successAlert.setHeaderText("Request Accepted");
        successAlert.setContentText(acceptingUser.displayName + " has accepted your DM request!");
        successAlert.showAndWait();
    }

    public void onDMRequestDeclined(User decliningUser) {
        // Show decline notification
        Alert declineAlert = new Alert(Alert.AlertType.WARNING);
        declineAlert.setTitle("DM Request Declined");
        declineAlert.setHeaderText("Request Declined");
        declineAlert.setContentText(decliningUser.displayName + " has declined your DM request.");
        declineAlert.showAndWait();

        // Close the DM and return to forum
        goToForum();
    }

    private String createDMKey(String randId1, String randId2) {
        // Create consistent key regardless of order
        if (randId1.compareTo(randId2) < 0) {
            return "dm_" + randId1 + "_" + randId2;
        } else {
            return "dm_" + randId2 + "_" + randId1;
        }
    }

    private void setupDMConnection(User targetUser, String dmKey) {
        // Store DM information
        this.currentDMUser = targetUser;
        this.currentDMKey = dmKey;

        // Set chat type
        ct = chatType.DM;

        // Create or get DM server
        server.DMServer dmServer = server.DMServer.getDMServer(dmKey);
        if (dmServer == null) {
            dmServer = server.DMServer.createDMServer(dmKey);
            if (dmServer == null) {
                Platform.runLater(() -> {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("DM Server Error");
                    errorAlert.setHeaderText("Cannot Create DM Server");
                    errorAlert.setContentText("Failed to create DM server for this conversation.");
                    errorAlert.showAndWait();
                });
                return;
            }
        }

        // Connect to DM server
        try {
            // Wait a moment for server to be ready
            Thread.sleep(500);

            // Disconnect from current server if connected
            if (forumClient != null) {
                forumClient.closeEverything(null, null, null);
            }

            // Connect to DM server
            Socket socket = new Socket("localhost", dmServer.getPort());
            java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream()));
            bw.write(currentUser.displayName);
            bw.newLine();
            bw.flush();

            // Create new ForumClient for DM
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
                errorAlert.setTitle("DM Connection Failed");
                errorAlert.setHeaderText("Cannot Connect to DM");
                errorAlert.setContentText("Failed to connect to DM server: " + e.getMessage());
                errorAlert.showAndWait();
            });
        }

        // Switch to DM view
        switchToDM(dmKey);
    }

    private void switchToDM(String dmKey) {
        // Restore chat history for this DM
        restoreChatHistory(dmKey);

        // Set current chat type
        ct = chatType.DM;
    }

    public void cleanup() {
        if (forumClient != null) {
            forumClient.closeEverything(null, null, null);
        }
    }
    private javafx.scene.layout.HBox createActiveDMRow(String dmKey) {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new javafx.geometry.Insets(5));
        row.setStyle("-fx-border-color: #4CAF50; -fx-border-radius: 5; -fx-background-color: #E8F5E8; -fx-background-radius: 5;");

        // Extract user IDs from DM key
        String[] parts = dmKey.substring(3).split("_"); // Remove "dm_" prefix and split
        String otherUserID = null;

        for (String part : parts) {
            if (!part.equals(currentUser.randomIdentifier)) {
                otherUserID = part;
                break;
            }
        }

        if (otherUserID != null) {
            final String finalOtherUserID = otherUserID; // Make effectively final
            User otherUser = UserDAO.getUserByRandomID(finalOtherUserID);
            String displayName = otherUser != null ? otherUser.displayName : "Unknown User";

            Label infoLabel = new Label("DM with " + displayName + " (" + finalOtherUserID + ")");
            infoLabel.setPrefWidth(300);

            // Switch button
            Button switchBtn = new Button("Switch to DM");
            switchBtn.setOnAction(e -> {
                // Switch to this active DM
                if (otherUser != null) {
                    this.currentDMUser = otherUser;
                    this.currentDMKey = dmKey;

                    // Update UI
                    headLabel.setText("DM: " + displayName);
                    detailsLabel.setText("Connected to " + displayName + " (" + finalOtherUserID + ")");
                    detailsLabel.setVisible(true);

                    // Switch to DM view
                    switchToDM(dmKey);

                    // Close the saved spaces dialog
                    javafx.stage.Stage stage = (javafx.stage.Stage) ((Button) e.getSource()).getScene().getWindow();
                    stage.close();
                }
            });

            row.getChildren().addAll(infoLabel, switchBtn);
        }

        return row;
    }

    private void connectToSavedDM(String randomID) {
        // First check if we already have this DM in our chat history
        String dmKey = null;
        for (String key : chatHistories.keySet()) {
            if (key.startsWith("dm_") && key.contains(randomID)) {
                dmKey = key;
                break;
            }
        }

        if (dmKey != null) {
            // We have an existing DM history, switch to it directly
            User targetUser = UserDAO.getUserByRandomID(randomID);
            if (targetUser != null) {
                this.currentDMUser = targetUser;
                this.currentDMKey = dmKey;

                // Update UI
                headLabel.setText("DM: " + targetUser.displayName);
                detailsLabel.setText("Connected to " + targetUser.displayName + " (" + targetUser.randomIdentifier + ")");
                detailsLabel.setVisible(true);

                // Switch to DM view
                switchToDM(dmKey);
                return;
            }
        }

        // No existing DM, create new request
        User targetUser = UserDAO.getUserByRandomID(randomID);
        if (targetUser == null) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("User Not Found");
            errorAlert.setHeaderText("Invalid Random ID");
            errorAlert.setContentText("No user found with the Random ID: " + randomID);
            errorAlert.showAndWait();
            return;
        }

        if (targetUser.randomIdentifier.equals(currentUser.randomIdentifier)) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Invalid Target");
            errorAlert.setHeaderText("Cannot DM Yourself");
            errorAlert.setContentText("You cannot start a DM with yourself.");
            errorAlert.showAndWait();
            return;
        }

        // Send DM request
        sendDMRequest(targetUser);
    }

    private void connectToSavedGC(String ipPort, String gcName) {
        String[] parts = ipPort.split(":");
        if (parts.length != 2) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Invalid Address");
            errorAlert.setHeaderText("Invalid IP:Port Format");
            errorAlert.setContentText("Please use format IP:Port (e.g., localhost:5000)");
            errorAlert.showAndWait();
            return;
        }

        String ip = parts[0].trim();
        try {
            int port = Integer.parseInt(parts[1].trim());
            joinGroupChat(gcName, ip, port);
        } catch (NumberFormatException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Invalid Port");
            errorAlert.setHeaderText("Invalid Port Number");
            errorAlert.setContentText("Please enter a valid port number.");
            errorAlert.showAndWait();
        }
    }
}
