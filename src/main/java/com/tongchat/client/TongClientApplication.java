package com.tongchat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class TongClientApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            // Load premium Messenger-style UI as the home screen
            Parent root = FXMLLoader.load(getClass().getResource("/com/tongchat/client/views/client_ui.fxml"));
            Scene scene = new Scene(root);

            // Note: Themes are now managed by PremiumClientController
            
            Image tongLogo = new Image("/com/tongchat/client/views/tongLogo.png");
            stage.setTitle("Tong Messenger — Premium Unified");
            stage.getIcons().add(tongLogo);
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setResizable(true);
            stage.show();
            
            System.out.println("✅ Tong JavaFX Client Started Successfully!");
            System.out.println("🎨 Premium Messenger UI Loaded");
            System.out.println("🌓 Dark/Light theme available");
        } catch (Exception e) {
            System.err.println("❌ Failed to load JavaFX UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
