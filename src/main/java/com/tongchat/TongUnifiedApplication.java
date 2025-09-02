package com.tongchat;

import com.tongchat.client.TongClientApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.CompletableFuture;

/**
 * Unified Tong Messenger Application
 * Automatically starts both Spring Boot backend and JavaFX frontend as a single integrated application
 */
public class TongUnifiedApplication {
    
    private static ConfigurableApplicationContext backendContext;
    
    public static void main(String[] args) {
        printWelcomeBanner();
        
        // Check for help argument
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            showUsage();
            return;
        }
        
        System.out.println("🚀 Starting Tong Messenger - Unified Mode");
        System.out.println("📡 Initializing backend server...");
        System.out.println("🎨 Preparing JavaFX client...");
        System.out.println();
        
        startUnifiedApplication(args);
    }
    
    private static void printWelcomeBanner() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║               🚀 TONG MESSENGER - UNIFIED                   ║");
        System.out.println("║                Premium Chat Experience                      ║");
        System.out.println("║          📱 JavaFX Frontend + 🖥️ Spring Backend           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    private static void showUsage() {
        System.out.println("Usage: java -jar tong-messenger-unified.jar [--help]");
        System.out.println();
        System.out.println("Tong Messenger Unified Application");
        System.out.println("Automatically starts both backend server and JavaFX client.");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --help, -h  - Show this help message");
        System.out.println();
        System.out.println("Features:");
        System.out.println("  ✅ Spring Boot backend with REST API & WebSocket");
        System.out.println("  ✅ JavaFX premium client interface");
        System.out.println("  ✅ Integrated authentication system");
        System.out.println("  ✅ Real-time messaging");
        System.out.println("  ✅ Group chat support");
        System.out.println();
        shutdown();
    }
    
    private static void startUnifiedApplication(String[] args) {
        try {
            // Start backend server in background thread
            CompletableFuture.runAsync(() -> {
                try {
                    System.out.println("🖥️  Starting Spring Boot backend server...");
                    // Set headless mode for backend
                    System.setProperty("java.awt.headless", "true");
                    backendContext = SpringApplication.run(com.tongchat.TongMessengerApplication.class, args);
                    System.out.println("✅ Backend server started successfully!");
                } catch (Exception e) {
                    System.err.println("❌ Failed to start backend server: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            // Give backend a moment to start
            Thread.sleep(2000);
            
            // Start JavaFX client in main thread
            System.out.println("🎨 Starting JavaFX client interface...");
            CompletableFuture<Void> clientFuture = CompletableFuture.runAsync(() -> {
                try {
                    // Disable headless mode for JavaFX
                    System.setProperty("java.awt.headless", "false");
                    TongClientApplication.main(args);
                    System.out.println("✅ JavaFX client started successfully!");
                } catch (Exception e) {
                    System.err.println("❌ Failed to start JavaFX client: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            System.out.println("🚀 Tong Messenger is now running!");
            System.out.println("� Backend: http://localhost:8080");
            System.out.println("🎨 Client: JavaFX interface should open shortly");
            System.out.println("💡 Close the JavaFX window to shut down the application");
            System.out.println();
            
            // Wait for client to finish (when user closes JavaFX window)
            clientFuture.join();
            
            System.out.println("🔄 JavaFX client closed. Shutting down backend...");
            shutdown();
            
        } catch (Exception e) {
            System.err.println("❌ Error starting unified application: " + e.getMessage());
            e.printStackTrace();
            shutdown();
        }
    }
    
    private static void shutdown() {
        System.out.println("🛑 Shutting down Tong Messenger...");
        if (backendContext != null) {
            try {
                backendContext.close();
                System.out.println("✅ Backend server shutdown complete");
            } catch (Exception e) {
                System.err.println("⚠️  Error during backend shutdown: " + e.getMessage());
            }
        }
        System.out.println("👋 Goodbye!");
        System.exit(0);
    }
}
