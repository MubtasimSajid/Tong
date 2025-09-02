package com.tongchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Tong Messenger - Premium Anonymous Chat Platform
 * 
 * Features:
 * - Anonymous user registration with auto-generated credentials
 * - Real-time messaging with WebSocket
 * - Group chat with admin controls
 * - User search and connection system
 * - Temporary/Permanent chat sessions
 * - Notice board system
 * - SQL database persistence
 * - JWT authentication
 * - RESTful API
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
public class TongMessengerApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Starting Tong Messenger Backend...");
        System.out.println("💬 Premium Anonymous Chat Platform");
        System.out.println("🔥 Spring Boot + SQL Database + WebSocket");
        System.out.println("=====================================");
        
        SpringApplication.run(TongMessengerApplication.class, args);
        
        System.out.println("✅ Tong Messenger Backend Started Successfully!");
        System.out.println("📡 Server: http://localhost:8080");
        System.out.println("🔗 WebSocket: ws://localhost:8080/ws");
        System.out.println("📊 H2 Console: http://localhost:8080/h2-console");
        System.out.println("📚 API Docs: http://localhost:8080/swagger-ui.html");
    }
}
