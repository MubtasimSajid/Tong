package com.tongchat.client.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;

@SuppressWarnings({"unchecked", "deprecation"})
public class PremiumClientController implements Initializable {

    // Premium UI Components
    @FXML private BorderPane mainBorderPane;
    @FXML private Label userNameLabel;
    @FXML private Label statusLabel;
    @FXML private Button settingsButton;
    @FXML private Button themeToggleButton;
    @FXML private TextField searchField;
    @FXML private Button newChatButton;
    @FXML private Button createGroupButton;
    @FXML private ListView<HBox> conversationsList;
    @FXML private Label chatPartnerName;
    @FXML private Label chatPartnerStatus;
    @FXML private Button callButton;
    @FXML private Button videoButton;
    @FXML private Button infoButton;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private Button attachButton;
    @FXML private Button emojiButton;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;

    private String currentUsername;
    private String selectedConversation;
    private Map<String, VBox> conversationMessages = new HashMap<>();
    private ObservableList<HBox> conversations = FXCollections.observableArrayList();
    private StompSession stompSession;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SERVER_URL = "http://localhost:8081";
    private static final String WEBSOCKET_URL = "ws://localhost:8081/ws";
    private boolean isDarkTheme = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        setupEventHandlers();
        connectToWebSocket();
        
        // Delay theme application until scene is ready
        Platform.runLater(() -> {
            Platform.runLater(() -> {
                applyTheme();
                showLoginDialog();
            });
        });
    }

    private void connectToWebSocket() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        try {
            stompSession = stompClient.connect(WEBSOCKET_URL, new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    Platform.runLater(() -> statusLabel.setText("Connected"));
                    session.subscribe("/topic/public", this);
                    session.subscribe("/user/queue/reply", this);
                }

                @Override
                public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                    showErrorAlert("WebSocket Error", "Exception in WebSocket session: " + exception.getMessage());
                }

                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Map.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    handleIncomingMessage((Map<String, Object>) payload);
                }
            }).get();
        } catch (Exception e) {
            showErrorAlert("WebSocket Connection Failed", "Could not connect to WebSocket: " + e.getMessage());
        }
    }

    private void setupUI() {
        conversationsList.setItems(conversations);
        conversationsList.setCellFactory(listView -> new ListCell<HBox>() {
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : item);
                setStyle("-fx-background-color: transparent;");
            }
        });
        messageInput.setMaxHeight(80);
        messagesContainer.heightProperty().addListener((obs, oldHeight, newHeight) -> Platform.runLater(() -> messagesScrollPane.setVvalue(1.0)));
    }

    private void setupEventHandlers() {
        sendButton.setOnAction(this::handleSendMessage);
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER") && !event.isShiftDown()) {
                event.consume();
                handleSendMessage(null);
            }
        });
        newChatButton.setOnAction(e -> showNewChatDialog());
        createGroupButton.setOnAction(e -> showCreateGroupDialog());
        searchField.textProperty().addListener((obs, oldText, newText) -> filterConversations(newText));
        conversationsList.setOnMouseClicked(event -> {
            HBox selected = conversationsList.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getUserData() != null) {
                selectConversation(selected.getUserData().toString());
            }
        });
        emojiButton.setOnAction(e -> {
            String[] emojis = {"😊", "😂", "❤️", "👍", "😢", "😮", "😡", "🙏", "👋", "🎉"};
            ChoiceDialog<String> dialog = new ChoiceDialog<>(emojis[0], emojis);
            dialog.setTitle("Choose Emoji");
            dialog.setHeaderText("Select an emoji to add");
            dialog.showAndWait().ifPresent(emoji -> messageInput.appendText(emoji));
        });
        
        themeToggleButton.setOnAction(e -> toggleTheme());
    }

    private void showLoginDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Tong Messenger - Login");
        dialog.setHeaderText("Welcome to Tong Premium Chat");
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        ButtonType registerButtonType = new ButtonType("Get Anonymous Account", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, registerButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(username::requestFocus);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) return new String[]{username.getText(), password.getText(), "login"};
            if (dialogButton == registerButtonType) return new String[]{"", "", "register"};
            return null;
        });
        dialog.showAndWait().ifPresent(result -> {
            if ("register".equals(result[2])) handleRegister();
            else if ("login".equals(result[2])) handleLogin(result[0], result[1]);
            else Platform.exit();
        });
    }

    private void handleLogin(String username, String password) {
        // For a real app, you would perform a proper login request to the backend
        // and get a JWT token to use for subsequent requests.
        currentUsername = username;
        userNameLabel.setText(username);
        statusLabel.setText("Online");
        addSystemMessage("Welcome to Tong Messenger! 🎉");
        
        // Add sample conversations for demonstration
        addSampleConversations();
        
        stompSession.subscribe("/user/" + currentUsername + "/queue/private", new StompSessionHandlerAdapter() {
             @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                handleIncomingMessage((Map<String, Object>) payload);
            }
        });
    }

    private void addSampleConversations() {
        Platform.runLater(() -> {
            // Add some sample conversations to demonstrate the premium UI
            addConversation("Alex Chen", "Hey! How's your day going? 😊", "2 min");
            addConversation("Sarah Wilson", "The project looks great! 👍", "5 min");  
            addConversation("Design Team", "New UI mockups are ready for review", "12 min");
            addConversation("John Smith", "Thanks for the help earlier", "1 hr");
            addConversation("Dev Community", "Anyone working with JavaFX lately?", "2 hr");
            addConversation("Maria Garcia", "Coffee later? ☕", "3 hr");
            addConversation("Tech News", "Latest updates in software development", "1 day");
            
            // Pre-select the first conversation
            if (!conversations.isEmpty()) {
                conversationsList.getSelectionModel().select(0);
                HBox firstConversation = conversations.get(0);
                if (firstConversation.getUserData() != null) {
                    selectConversation(firstConversation.getUserData().toString());
                }
            }
        });
    }

    private void addConversation(String name, String lastMessage, String time) {
        HBox conversationItem = new HBox(15);
        conversationItem.setAlignment(Pos.CENTER_LEFT);
        conversationItem.setPadding(new Insets(12, 15, 12, 15));
        conversationItem.getStyleClass().add("conversation-item");
        conversationItem.setUserData(name);

        // Avatar circle
        Circle avatar = new Circle(25);
        avatar.setFill(Color.web("#0084ff"));
        avatar.getStyleClass().add("avatar");

        // Name and message container
        VBox messageContainer = new VBox(4);
        HBox.setHgrow(messageContainer, Priority.ALWAYS);

        // Name label
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        nameLabel.getStyleClass().add("conversation-name");

        // Last message label
        Label messageLabel = new Label(lastMessage);
        messageLabel.setFont(Font.font("System", 13));
        messageLabel.getStyleClass().add("conversation-preview");
        messageLabel.setMaxWidth(180);

        // Time label
        Label timeLabel = new Label(time);
        timeLabel.setFont(Font.font("System", 11));
        timeLabel.getStyleClass().add("conversation-time");

        messageContainer.getChildren().addAll(nameLabel, messageLabel);
        conversationItem.getChildren().addAll(avatar, messageContainer, timeLabel);

        conversations.add(conversationItem);
    }

    private void handleRegister() {
        try {
            URL url = new URL(SERVER_URL + "/api/auth/register/anonymous");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(conn.getInputStream());
                String responseBody = scanner.useDelimiter("\\A").next();
                scanner.close();
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, HashMap.class);
                String newUsername = (String) responseMap.get("username");
                String newPassword = (String) responseMap.get("password");
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Account Created");
                    alert.setHeaderText("Your Anonymous Account");
                    alert.setContentText("Username: " + newUsername + "\nPassword: " + newPassword + "\n\nPlease save these credentials!");
                    alert.showAndWait();
                    handleLogin(newUsername, newPassword);
                });
            } else {
                showErrorAlert("Registration Failed", "Server returned error code: " + responseCode);
            }
        } catch (IOException e) {
            showErrorAlert("Connection Error", "Failed to communicate with server: " + e.getMessage());
        }
    }

    private void handleSendMessage(ActionEvent event) {
        String message = messageInput.getText().trim();
        if (!message.isEmpty() && selectedConversation != null) {
            Map<String, Object> messagePayload = new HashMap<>();
            messagePayload.put("sender", currentUsername);
            messagePayload.put("recipient", selectedConversation);
            messagePayload.put("content", message);
            stompSession.send("/app/chat.private", messagePayload);
            addMessage(currentUsername, message, true);
            messageInput.clear();
            messageInput.setPrefRowCount(1);
        }
    }

    private void addMessage(String sender, String content, boolean isOwn) {
        Platform.runLater(() -> {
            HBox messageBox = createMessageBubble(sender, content, isOwn);
            messagesContainer.getChildren().add(messageBox);
        });
    }

    private HBox createMessageBubble(String sender, String content, boolean isOwn) {
        HBox container = new HBox();
        container.setPadding(new Insets(5, 10, 5, 10));
        VBox bubble = new VBox();
        bubble.setMaxWidth(400);
        bubble.setPadding(new Insets(12, 16, 12, 16));
        bubble.setSpacing(4);
        
        if (isOwn) {
            container.setAlignment(Pos.CENTER_RIGHT);
            bubble.getStyleClass().add("message-bubble-own");
        } else {
            container.setAlignment(Pos.CENTER_LEFT);
            bubble.getStyleClass().add("message-bubble-other");
        }
        
        Text messageText = new Text(content);
        messageText.getStyleClass().add(isOwn ? "message-text-own" : "message-text-other");
        messageText.setFont(Font.font("System", FontWeight.NORMAL, 14));
        messageText.setWrappingWidth(380);
        
        Text timeText = new Text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeText.getStyleClass().add(isOwn ? "message-time-own" : "message-time-other");
        timeText.setFont(Font.font("System", FontWeight.NORMAL, 11));
        
        bubble.getChildren().addAll(messageText, timeText);
        container.getChildren().add(bubble);
        
        return container;
    }

    private void addSystemMessage(String message) {
        Platform.runLater(() -> {
            HBox container = new HBox();
            container.setAlignment(Pos.CENTER);
            container.setPadding(new Insets(10));
            Text systemText = new Text(message);
            systemText.getStyleClass().add("system-message");
            systemText.setFont(Font.font("System", FontWeight.NORMAL, 12));
            container.getChildren().add(systemText);
            messagesContainer.getChildren().add(container);
        });
    }

    private void showNewChatDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Chat");
        dialog.setHeaderText("Start a new conversation");
        dialog.setContentText("Enter username to chat with:");
        dialog.showAndWait().ifPresent(username -> {
            if (!username.trim().isEmpty()) {
                addConversation(username.trim());
                selectConversation(username.trim());
            }
        });
    }

    private void showCreateGroupDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Group");
        dialog.setHeaderText("Create a new group chat");
        dialog.setContentText("Enter group name:");
        dialog.showAndWait().ifPresent(groupName -> {
            if (!groupName.trim().isEmpty()) {
                Map<String, Object> groupPayload = new HashMap<>();
                groupPayload.put("name", groupName.trim());
                groupPayload.put("creator", currentUsername);
                stompSession.send("/app/chat.group", groupPayload);
                addSystemMessage("Group '" + groupName + "' created successfully! 🎉");
            }
        });
    }

    private void addConversation(String name) {
        Platform.runLater(() -> {
            if (conversationMessages.containsKey(name)) return;
            HBox conversationItem = createConversationItem(name);
            conversations.add(conversationItem);
            conversationMessages.put(name, new VBox());
        });
    }

    private HBox createConversationItem(String name) {
        HBox item = new HBox();
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 15, 12, 15));
        item.setSpacing(12);
        item.setUserData(name);
        item.getStyleClass().add("conversation-item");
        item.setStyle("-fx-cursor: hand;");
        
        Circle avatar = new Circle(22);
        avatar.setFill(Color.web("#0084ff"));
        
        VBox userInfo = new VBox();
        userInfo.setSpacing(2);
        
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("conversation-name");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label lastMessage = new Label("Click to start chatting");
        lastMessage.getStyleClass().add("conversation-preview");
        lastMessage.setFont(Font.font("System", FontWeight.NORMAL, 12));
        
        userInfo.getChildren().addAll(nameLabel, lastMessage);
        item.getChildren().addAll(avatar, userInfo);
        
        return item;
    }

    private void selectConversation(String name) {
        selectedConversation = name;
        chatPartnerName.setText(name);
        chatPartnerStatus.setText("Online");
        messagesContainer.getChildren().clear();
        VBox messages = conversationMessages.get(name);
        if (messages != null) {
            messagesContainer.getChildren().setAll(messages.getChildren());
        }
        addSystemMessage("Conversation with " + name + " started");
    }

    private void filterConversations(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            conversationsList.setItems(conversations);
        } else {
            ObservableList<HBox> filtered = conversations.filtered(item ->
                item.getUserData().toString().toLowerCase().contains(filter.toLowerCase())
            );
            conversationsList.setItems(filtered);
        }
    }

    private void handleIncomingMessage(Map<String, Object> message) {
        Platform.runLater(() -> {
            String sender = (String) message.get("sender");
            String content = (String) message.get("content");
            if (!conversationMessages.containsKey(sender)) {
                addConversation(sender);
            }
            if (sender.equals(selectedConversation)) {
                addMessage(sender, content, false);
            }
            // You can also update the last message preview in the conversation list
        });
    }

    private void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        applyTheme();
        updateThemeButton();
    }
    
    private void applyTheme() {
        Platform.runLater(() -> {
            if (mainBorderPane.getScene() != null) {
                mainBorderPane.getScene().getStylesheets().clear();
                
                // Always apply the premium style first as base
                try {
                    String premiumStyleUrl = getClass().getResource("/com/tongchat/client/views/premium-style.css").toExternalForm();
                    mainBorderPane.getScene().getStylesheets().add(premiumStyleUrl);
                } catch (Exception e) {
                    System.err.println("Could not load premium style");
                }
                
                // Then apply the theme-specific styles
                String themePath = isDarkTheme ? "/com/tongchat/client/views/dark-theme.css" : "/com/tongchat/client/views/light-theme.css";
                try {
                    String themeUrl = getClass().getResource(themePath).toExternalForm();
                    mainBorderPane.getScene().getStylesheets().add(themeUrl);
                    
                    System.out.println("✅ Applied theme: " + (isDarkTheme ? "Dark" : "Light Premium White & Blue"));
                } catch (Exception e) {
                    System.err.println("Could not load theme: " + themePath);
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void updateThemeButton() {
        Platform.runLater(() -> {
            themeToggleButton.setText(isDarkTheme ? "☀" : "🌙");
        });
    }
}