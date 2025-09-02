package com.tongchat.backend.controller;

import com.tongchat.backend.entity.ChatGroup;
import com.tongchat.backend.entity.User;
import com.tongchat.backend.service.ChatGroupService;
import com.tongchat.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Group Controller - Handles group operations
 */
@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
public class GroupController {
    
    private final ChatGroupService chatGroupService;
    private final UserService userService;
    
    // Constructor injection
    public GroupController(ChatGroupService chatGroupService, UserService userService) {
        this.chatGroupService = chatGroupService;
        this.userService = userService;
    }
    
    /**
     * Create new group
     * POST /api/groups
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody Map<String, Object> groupRequest) {
        try {
            String name = (String) groupRequest.get("name");
            String description = (String) groupRequest.get("description");
            String typeStr = (String) groupRequest.get("type");
            String visibilityStr = (String) groupRequest.get("visibility");
            Long creatorId = Long.valueOf(groupRequest.get("creatorId").toString());
            
            ChatGroup.GroupType type = ChatGroup.GroupType.valueOf(typeStr.toUpperCase());
            ChatGroup.GroupVisibility visibility = ChatGroup.GroupVisibility.valueOf(visibilityStr.toUpperCase());
            
            User creator = userService.findByUsername("").orElseThrow(); // TODO: Get from security context
            
            ChatGroup group = chatGroupService.createGroup(name, description, type, visibility, creator);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Group created successfully");
            response.put("groupId", group.getId());
            response.put("groupName", group.getName());
            response.put("type", group.getType());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create group: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get public groups
     * GET /api/groups/public
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getPublicGroups() {
        try {
            List<ChatGroup> groups = chatGroupService.getPublicGroups();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("groups", groups.stream().map(group -> {
                Map<String, Object> groupInfo = new HashMap<>();
                groupInfo.put("id", group.getId());
                groupInfo.put("name", group.getName());
                groupInfo.put("description", group.getDescription());
                groupInfo.put("type", group.getType());
                groupInfo.put("memberCount", group.getMembers().size());
                groupInfo.put("createdAt", group.getCreatedAt());
                return groupInfo;
            }).toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch groups: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Search groups
     * GET /api/groups/search?q={searchTerm}
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchGroups(@RequestParam("q") String searchTerm) {
        try {
            List<ChatGroup> groups = chatGroupService.searchGroups(searchTerm);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("groups", groups.stream().map(group -> {
                Map<String, Object> groupInfo = new HashMap<>();
                groupInfo.put("id", group.getId());
                groupInfo.put("name", group.getName());
                groupInfo.put("description", group.getDescription());
                groupInfo.put("type", group.getType());
                groupInfo.put("visibility", group.getVisibility());
                groupInfo.put("memberCount", group.getMembers().size());
                return groupInfo;
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
     * Join group
     * POST /api/groups/{groupId}/join
     */
    @PostMapping("/{groupId}/join")
    public ResponseEntity<Map<String, Object>> joinGroup(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> joinRequest) {
        
        try {
            Long userId = Long.valueOf(joinRequest.get("userId").toString());
            User user = userService.findByUsername("").orElseThrow(); // TODO: Get from security context
            
            ChatGroup group = chatGroupService.addMemberToGroup(groupId, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully joined group");
            response.put("groupName", group.getName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to join group: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Mute user in group
     * POST /api/groups/{groupId}/mute
     */
    @PostMapping("/{groupId}/mute")
    public ResponseEntity<Map<String, Object>> muteUser(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> muteRequest) {
        
        try {
            String targetUsername = (String) muteRequest.get("targetUsername");
            Long adminId = Long.valueOf(muteRequest.get("adminId").toString());
            
            User admin = userService.findByUsername("").orElseThrow(); // TODO: Get from security context
            User targetUser = userService.findByUsername(targetUsername)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
            
            ChatGroup group = chatGroupService.muteUser(groupId, targetUser, admin);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User muted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to mute user: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Kick user from group
     * POST /api/groups/{groupId}/kick
     */
    @PostMapping("/{groupId}/kick")
    public ResponseEntity<Map<String, Object>> kickUser(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> kickRequest) {
        
        try {
            String targetUsername = (String) kickRequest.get("targetUsername");
            Long adminId = Long.valueOf(kickRequest.get("adminId").toString());
            
            User admin = userService.findByUsername("").orElseThrow(); // TODO: Get from security context
            User targetUser = userService.findByUsername(targetUsername)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
            
            ChatGroup group = chatGroupService.removeMemberFromGroup(groupId, targetUser, admin);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User kicked successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to kick user: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Make user admin
     * POST /api/groups/{groupId}/make-admin
     */
    @PostMapping("/{groupId}/make-admin")
    public ResponseEntity<Map<String, Object>> makeAdmin(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> adminRequest) {
        
        try {
            String targetUsername = (String) adminRequest.get("targetUsername");
            Long currentAdminId = Long.valueOf(adminRequest.get("currentAdminId").toString());
            
            User currentAdmin = userService.findByUsername("").orElseThrow(); // TODO: Get from security context
            User targetUser = userService.findByUsername(targetUsername)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
            
            ChatGroup group = chatGroupService.makeUserAdmin(groupId, targetUser, currentAdmin);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User promoted to admin successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to make user admin: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
