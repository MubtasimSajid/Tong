package com.tongchat.backend.controller;

import com.tongchat.backend.entity.Message;
import com.tongchat.backend.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebSocket Message Controller for real-time chat
 */
@Controller
public class WebSocketMessageController {
    
    private final SimpMessageSendingOperations messagingTemplate;
    private final MessageService messageService;
    
    // Constructor injection
    public WebSocketMessageController(SimpMessageSendingOperations messagingTemplate, MessageService messageService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
    }
    
    /**
     * Handle private messages
     */
    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload Map<String, Object> messageData) {
        try {
            String senderUsername = (String) messageData.get("sender");
            String recipientUsername = (String) messageData.get("recipient");
            String content = (String) messageData.get("content");
            String messageType = (String) messageData.get("type");
            
            // Save message to database
            Message message = messageService.sendPrivateMessage(
                senderUsername, recipientUsername, content, messageType);
            
            // Send to recipient via WebSocket
            messagingTemplate.convertAndSendToUser(
                recipientUsername, 
                "/queue/messages", 
                createMessageResponse(message)
            );
            
            // Send confirmation to sender
            messagingTemplate.convertAndSendToUser(
                senderUsername,
                "/queue/confirmation",
                Map.of("messageId", message.getId(), "status", "delivered")
            );
            
        } catch (Exception e) {
            System.err.println("Error sending private message: " + e.getMessage());
        }
    }
    
    /**
     * Handle group messages
     */
    @MessageMapping("/chat.group")
    public void sendGroupMessage(@Payload Map<String, Object> messageData) {
        try {
            String senderUsername = (String) messageData.get("sender");
            Long groupId = Long.valueOf(messageData.get("groupId").toString());
            String content = (String) messageData.get("content");
            String messageType = (String) messageData.get("type");
            
            // Save message to database
            Message message = messageService.sendGroupMessage(
                senderUsername, groupId, content, messageType);
            
            // Broadcast to all group members
            messagingTemplate.convertAndSend(
                "/topic/group." + groupId,
                createMessageResponse(message)
            );
            
        } catch (Exception e) {
            System.err.println("Error sending group message: " + e.getMessage());
        }
    }
    
    /**
     * Handle group notices (admin only)
     */
    @MessageMapping("/chat.notice")
    public void sendGroupNotice(@Payload Map<String, Object> noticeData) {
        try {
            String senderUsername = (String) noticeData.get("sender");
            Long groupId = Long.valueOf(noticeData.get("groupId").toString());
            String content = (String) noticeData.get("content");
            
            // Save notice to database
            Message notice = messageService.sendGroupNotice(senderUsername, groupId, content);
            
            // Broadcast notice to all group members
            messagingTemplate.convertAndSend(
                "/topic/group." + groupId + ".notices",
                createNoticeResponse(notice)
            );
            
        } catch (Exception e) {
            System.err.println("Error sending group notice: " + e.getMessage());
        }
    }
    
    /**
     * Handle user typing indicators
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload Map<String, Object> typingData) {
        String username = (String) typingData.get("username");
        String recipientUsername = (String) typingData.get("recipient");
        Long groupId = typingData.get("groupId") != null ? 
            Long.valueOf(typingData.get("groupId").toString()) : null;
        Boolean isTyping = (Boolean) typingData.get("isTyping");
        
        Map<String, Object> typingResponse = Map.of(
            "username", username,
            "isTyping", isTyping,
            "timestamp", System.currentTimeMillis()
        );
        
        if (groupId != null) {
            // Group typing indicator
            messagingTemplate.convertAndSend(
                "/topic/group." + groupId + ".typing",
                typingResponse
            );
        } else if (recipientUsername != null) {
            // Private typing indicator
            messagingTemplate.convertAndSendToUser(
                recipientUsername,
                "/queue/typing",
                typingResponse
            );
        }
    }
    
    /**
     * Handle user online status
     */
    @MessageMapping("/user.status")
    public void updateUserStatus(@Payload Map<String, Object> statusData) {
        String username = (String) statusData.get("username");
        String status = (String) statusData.get("status");
        
        Map<String, Object> statusResponse = Map.of(
            "username", username,
            "status", status,
            "timestamp", System.currentTimeMillis()
        );
        
        // Broadcast status update
        messagingTemplate.convertAndSend("/topic/users.status", statusResponse);
    }
    
    /**
     * Create message response for WebSocket
     */
    private Map<String, Object> createMessageResponse(Message message) {
        return Map.of(
            "id", message.getId(),
            "sender", message.getSender().getUsername(),
            "content", message.getContent(),
            "type", message.getType(),
            "timestamp", message.getCreatedAt(),
            "isEdited", message.getIsEdited()
        );
    }
    
    /**
     * Create notice response for WebSocket
     */
    private Map<String, Object> createNoticeResponse(Message notice) {
        return Map.of(
            "id", notice.getId(),
            "sender", notice.getSender().getUsername(),
            "content", notice.getContent(),
            "type", "NOTICE",
            "timestamp", notice.getCreatedAt(),
            "groupId", notice.getGroup().getId()
        );
    }
}
