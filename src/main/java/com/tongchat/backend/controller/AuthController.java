package com.tongchat.backend.controller;

import com.tongchat.backend.entity.User;
import com.tongchat.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication Controller - Handles login, registration, and user management
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final UserService userService;
    
    // Constructor injection
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Create anonymous user account
     * POST /api/auth/register/anonymous
     */
    @PostMapping("/register/anonymous")
    public ResponseEntity<Map<String, Object>> createAnonymousAccount() {
        try {
            User user = userService.createAnonymousUser();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Anonymous account created successfully");
            response.put("username", user.getUsername());
            response.put("password", user.getPassword()); // Plain password for one-time display
            response.put("userId", user.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create account: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Login user
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        if (username == null || password == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Username and password are required");
            
            return ResponseEntity.badRequest().body(response);
        }
        
        Optional<User> userOpt = userService.authenticateUser(username, password);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("displayName", user.getDisplayName());
            response.put("status", user.getStatus());
            response.put("isAnonymous", user.getIsAnonymous());
            
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Invalid username or password");
            
            return ResponseEntity.status(401).body(response);
        }
    }
    
    /**
     * Search users
     * GET /api/auth/users/search?q={searchTerm}
     */
    @GetMapping("/users/search")
    public ResponseEntity<Map<String, Object>> searchUsers(@RequestParam("q") String searchTerm) {
        try {
            List<User> users = userService.searchUsers(searchTerm);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("users", users.stream().map(user -> {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("displayName", user.getDisplayName());
                userInfo.put("status", user.getStatus());
                userInfo.put("isAnonymous", user.getIsAnonymous());
                return userInfo;
            }).toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Search failed: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get user by username
     * GET /api/auth/users/{username}
     */
    @GetMapping("/users/{username}")
    public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("displayName", user.getDisplayName());
            userInfo.put("status", user.getStatus());
            userInfo.put("isAnonymous", user.getIsAnonymous());
            userInfo.put("lastSeen", user.getLastSeen());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", userInfo);
            
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Update user status
     * POST /api/auth/users/{userId}/status
     */
    @PostMapping("/users/{userId}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable Long userId, 
            @RequestBody Map<String, String> statusRequest) {
        
        try {
            String statusStr = statusRequest.get("status");
            User.UserStatus status = User.UserStatus.valueOf(statusStr.toUpperCase());
            
            User user = userService.updateUserStatus(userId, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Status updated successfully");
            response.put("status", user.getStatus());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update status: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get online users count
     * GET /api/auth/stats/online
     */
    @GetMapping("/stats/online")
    public ResponseEntity<Map<String, Object>> getOnlineUsersCount() {
        Long count = userService.getOnlineUsersCount();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("onlineUsers", count);
        
        return ResponseEntity.ok(response);
    }
}
