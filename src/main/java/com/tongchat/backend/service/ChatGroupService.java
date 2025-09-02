package com.tongchat.backend.service;

import com.tongchat.backend.entity.ChatGroup;
import com.tongchat.backend.entity.GroupJoinRequest;
import com.tongchat.backend.entity.User;
import com.tongchat.backend.repository.ChatGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ChatGroup Service - Business logic for group operations
 */
@Service
@Transactional
public class ChatGroupService {
    
    private final ChatGroupRepository chatGroupRepository;
    
    // Constructor injection
    public ChatGroupService(ChatGroupRepository chatGroupRepository) {
        this.chatGroupRepository = chatGroupRepository;
    }
    
    /**
     * Create new chat group
     */
    public ChatGroup createGroup(String name, String description, ChatGroup.GroupType type, 
                                ChatGroup.GroupVisibility visibility, User creator) {
        ChatGroup group = new ChatGroup();
        group.setName(name);
        group.setDescription(description);
        group.setType(type);
        group.setVisibility(visibility);
        group.setCreator(creator);
        group.setIsActive(true);
        
        // Set expiration for temporary groups (24 hours)
        if (type == ChatGroup.GroupType.TEMPORARY) {
            group.setExpiresAt(LocalDateTime.now().plusHours(24));
        }
        
        // Add creator as member and admin
        group.addMember(creator);
        group.addAdmin(creator);
        
        return chatGroupRepository.save(group);
    }
    
    /**
     * Add member to group
     */
    public ChatGroup addMemberToGroup(Long groupId, User user) {
        ChatGroup group = findGroupById(groupId);
        
        if (!group.isBanned(user) && !group.isMember(user)) {
            group.addMember(user);
            return chatGroupRepository.save(group);
        }
        
        throw new RuntimeException("User cannot be added to group");
    }
    
    /**
     * Remove member from group
     */
    public ChatGroup removeMemberFromGroup(Long groupId, User member, User admin) {
        ChatGroup group = findGroupById(groupId);
        
        if (!group.isAdmin(admin)) {
            throw new RuntimeException("Only admins can remove members");
        }
        
        if (group.getCreator().equals(member)) {
            throw new RuntimeException("Cannot remove group creator");
        }
        
        group.removeMember(member);
        return chatGroupRepository.save(group);
    }
    
    /**
     * Make user admin
     */
    public ChatGroup makeUserAdmin(Long groupId, User newAdmin, User currentAdmin) {
        ChatGroup group = findGroupById(groupId);
        
        if (!group.isAdmin(currentAdmin)) {
            throw new RuntimeException("Only admins can promote members");
        }
        
        if (group.isMember(newAdmin)) {
            group.addAdmin(newAdmin);
            return chatGroupRepository.save(group);
        }
        
        throw new RuntimeException("User must be a member to become admin");
    }
    
    /**
     * Remove admin privileges
     */
    public ChatGroup removeAdmin(Long groupId, User adminToRemove, User currentAdmin) {
        ChatGroup group = findGroupById(groupId);
        
        if (!group.getCreator().equals(currentAdmin)) {
            throw new RuntimeException("Only group creator can remove admins");
        }
        
        group.removeAdmin(adminToRemove);
        return chatGroupRepository.save(group);
    }
    
    /**
     * Mute user in group
     */
    public ChatGroup muteUser(Long groupId, User userToMute, User admin) {
        ChatGroup group = findGroupById(groupId);
        
        if (!group.isAdmin(admin)) {
            throw new RuntimeException("Only admins can mute users");
        }
        
        group.muteUser(userToMute);
        return chatGroupRepository.save(group);
    }
    
    /**
     * Unmute user in group
     */
    public ChatGroup unmuteUser(Long groupId, User userToUnmute, User admin) {
        ChatGroup group = findGroupById(groupId);
        
        if (!group.isAdmin(admin)) {
            throw new RuntimeException("Only admins can unmute users");
        }
        
        group.unmuteUser(userToUnmute);
        return chatGroupRepository.save(group);
    }
    
    /**
     * Ban user from group
     */
    public ChatGroup banUser(Long groupId, User userToBan, User admin) {
        ChatGroup group = findGroupById(groupId);
        
        if (!group.isAdmin(admin)) {
            throw new RuntimeException("Only admins can ban users");
        }
        
        if (group.getCreator().equals(userToBan)) {
            throw new RuntimeException("Cannot ban group creator");
        }
        
        group.banUser(userToBan);
        return chatGroupRepository.save(group);
    }
    
    /**
     * Search groups by name
     */
    @Transactional(readOnly = true)
    public List<ChatGroup> searchGroups(String searchTerm) {
        return chatGroupRepository.searchByName(searchTerm);
    }
    
    /**
     * Get public groups
     */
    @Transactional(readOnly = true)
    public List<ChatGroup> getPublicGroups() {
        return chatGroupRepository.findPublicGroups();
    }
    
    /**
     * Get groups by member
     */
    @Transactional(readOnly = true)
    public List<ChatGroup> getGroupsByMember(User user) {
        return chatGroupRepository.findGroupsByMember(user);
    }
    
    /**
     * Get groups where user is admin
     */
    @Transactional(readOnly = true)
    public List<ChatGroup> getGroupsByAdmin(User user) {
        return chatGroupRepository.findGroupsByAdmin(user);
    }
    
    /**
     * Find group by ID
     */
    @Transactional(readOnly = true)
    public ChatGroup findGroupById(Long groupId) {
        return chatGroupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));
    }
    
    /**
     * Cleanup expired temporary groups
     */
    public void cleanupExpiredGroups() {
        List<ChatGroup> expiredGroups = chatGroupRepository.findExpiredTemporaryGroups(LocalDateTime.now());
        
        for (ChatGroup group : expiredGroups) {
            group.setIsActive(false);
            chatGroupRepository.save(group);
        }
    }
    
    /**
     * Get member count for group
     */
    @Transactional(readOnly = true)
    public Long getMemberCount(Long groupId) {
        return chatGroupRepository.countMembersByGroupId(groupId);
    }
}
