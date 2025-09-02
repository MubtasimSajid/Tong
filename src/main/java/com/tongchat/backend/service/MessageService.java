package com.tongchat.backend.service;

import com.tongchat.backend.entity.ChatGroup;
import com.tongchat.backend.entity.Message;
import com.tongchat.backend.entity.User;
import com.tongchat.backend.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Message Service - Business logic for messaging operations
 */
@Service
@Transactional
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ChatGroupService chatGroupService;
    
    // Constructor injection
    public MessageService(MessageRepository messageRepository, UserService userService, ChatGroupService chatGroupService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.chatGroupService = chatGroupService;
    }
    
    /**
     * Send private message between users
     */
    public Message sendPrivateMessage(String senderUsername, String recipientUsername, 
                                    String content, String messageType) {
        User sender = userService.findByUsername(senderUsername)
            .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        User recipient = userService.findByUsername(recipientUsername)
            .orElseThrow(() -> new RuntimeException("Recipient not found"));
        
        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        message.setType(Message.MessageType.valueOf(messageType.toUpperCase()));
        message.setStatus(Message.MessageStatus.SENT);
        
        return messageRepository.save(message);
    }
    
    /**
     * Send message to group
     */
    public Message sendGroupMessage(String senderUsername, Long groupId, 
                                  String content, String messageType) {
        User sender = userService.findByUsername(senderUsername)
            .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        ChatGroup group = chatGroupService.findGroupById(groupId);
        
        // Check if user is member and not muted
        if (!group.isMember(sender)) {
            throw new RuntimeException("User is not a member of this group");
        }
        
        if (group.isMuted(sender)) {
            throw new RuntimeException("User is muted in this group");
        }
        
        Message message = new Message();
        message.setSender(sender);
        message.setGroup(group);
        message.setContent(content);
        message.setType(Message.MessageType.valueOf(messageType.toUpperCase()));
        message.setStatus(Message.MessageStatus.SENT);
        
        return messageRepository.save(message);
    }
    
    /**
     * Send group notice (admin only)
     */
    public Message sendGroupNotice(String senderUsername, Long groupId, String content) {
        User sender = userService.findByUsername(senderUsername)
            .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        ChatGroup group = chatGroupService.findGroupById(groupId);
        
        // Check if user is admin
        if (!group.isAdmin(sender)) {
            throw new RuntimeException("Only admins can send notices");
        }
        
        Message notice = new Message();
        notice.setSender(sender);
        notice.setGroup(group);
        notice.setContent(content);
        notice.setType(Message.MessageType.NOTICE);
        notice.setStatus(Message.MessageStatus.SENT);
        
        return messageRepository.save(notice);
    }
    
    /**
     * Get private messages between two users
     */
    @Transactional(readOnly = true)
    public List<Message> getPrivateMessages(String username1, String username2) {
        User user1 = userService.findByUsername(username1)
            .orElseThrow(() -> new RuntimeException("User1 not found"));
        
        User user2 = userService.findByUsername(username2)
            .orElseThrow(() -> new RuntimeException("User2 not found"));
        
        return messageRepository.findPrivateMessagesBetweenUsers(user1, user2);
    }
    
    /**
     * Get group messages
     */
    @Transactional(readOnly = true)
    public List<Message> getGroupMessages(Long groupId) {
        ChatGroup group = chatGroupService.findGroupById(groupId);
        return messageRepository.findMessagesByGroup(group);
    }
    
    /**
     * Get group messages with pagination
     */
    @Transactional(readOnly = true)
    public Page<Message> getGroupMessagesPaginated(Long groupId, Pageable pageable) {
        ChatGroup group = chatGroupService.findGroupById(groupId);
        return messageRepository.findMessagesByGroupPaginated(group, pageable);
    }
    
    /**
     * Get group notices
     */
    @Transactional(readOnly = true)
    public List<Message> getGroupNotices(Long groupId) {
        ChatGroup group = chatGroupService.findGroupById(groupId);
        return messageRepository.findNoticesByGroup(group);
    }
    
    /**
     * Search messages by content
     */
    @Transactional(readOnly = true)
    public List<Message> searchMessages(String searchTerm) {
        return messageRepository.searchMessagesByContent(searchTerm);
    }
    
    /**
     * Mark message as read
     */
    public Message markMessageAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
        
        message.setStatus(Message.MessageStatus.READ);
        return messageRepository.save(message);
    }
    
    /**
     * Edit message
     */
    public Message editMessage(Long messageId, String newContent, String editorUsername) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Check if editor is the sender
        if (!message.getSender().getUsername().equals(editorUsername)) {
            throw new RuntimeException("Only sender can edit message");
        }
        
        message.setContent(newContent);
        message.markAsEdited();
        
        return messageRepository.save(message);
    }
    
    /**
     * Delete message
     */
    public void deleteMessage(Long messageId, String deleterUsername) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Check if deleter is the sender or group admin
        boolean canDelete = message.getSender().getUsername().equals(deleterUsername);
        
        if (message.getGroup() != null) {
            User deleter = userService.findByUsername(deleterUsername)
                .orElseThrow(() -> new RuntimeException("Deleter not found"));
            canDelete = canDelete || message.getGroup().isAdmin(deleter);
        }
        
        if (!canDelete) {
            throw new RuntimeException("Not authorized to delete this message");
        }
        
        message.setStatus(Message.MessageStatus.DELETED);
        messageRepository.save(message);
    }
    
    /**
     * Get unread messages for user
     */
    @Transactional(readOnly = true)
    public List<Message> getUnreadMessages(String username) {
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return messageRepository.findUnreadMessagesForUser(user);
    }
    
    /**
     * Cleanup expired messages
     */
    public void cleanupExpiredMessages() {
        List<Message> expiredMessages = messageRepository.findExpiredMessages(LocalDateTime.now());
        
        for (Message message : expiredMessages) {
            message.setStatus(Message.MessageStatus.DELETED);
            messageRepository.save(message);
        }
    }
}
