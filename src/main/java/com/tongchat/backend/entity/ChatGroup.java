package com.tongchat.backend.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * ChatGroup Entity - Represents chat groups with admin controls
 */
@Entity
@Table(name = "chat_groups")
@EntityListeners(AuditingEntityListener.class)
public class ChatGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupType type = GroupType.PERMANENT;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupVisibility visibility = GroupVisibility.PUBLIC;
    
    @Column(name = "max_members")
    private Integer maxMembers = 100;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "group_admins",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> admins = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "group_banned_users",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> bannedUsers = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "group_muted_users",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> mutedUsers = new HashSet<>();
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Message> messages = new HashSet<>();
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<GroupJoinRequest> joinRequests = new HashSet<>();
    
    public enum GroupType {
        TEMPORARY, PERMANENT
    }
    
    public enum GroupVisibility {
        PUBLIC, PRIVATE, INVITE_ONLY
    }
    
    // Constructors
    public ChatGroup() {}
    
    public ChatGroup(String name, String description, User creator) {
        this.name = name;
        this.description = description;
        this.creator = creator;
        this.members.add(creator);
        this.admins.add(creator);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public GroupType getType() { return type; }
    public void setType(GroupType type) { this.type = type; }
    
    public GroupVisibility getVisibility() { return visibility; }
    public void setVisibility(GroupVisibility visibility) { this.visibility = visibility; }
    
    public Integer getMaxMembers() { return maxMembers; }
    public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }
    
    public Set<User> getMembers() { return members; }
    public void setMembers(Set<User> members) { this.members = members; }
    
    public Set<User> getAdmins() { return admins; }
    public void setAdmins(Set<User> admins) { this.admins = admins; }
    
    public Set<User> getBannedUsers() { return bannedUsers; }
    public void setBannedUsers(Set<User> bannedUsers) { this.bannedUsers = bannedUsers; }
    
    public Set<User> getMutedUsers() { return mutedUsers; }
    public void setMutedUsers(Set<User> mutedUsers) { this.mutedUsers = mutedUsers; }
    
    public Set<Message> getMessages() { return messages; }
    public void setMessages(Set<Message> messages) { this.messages = messages; }
    
    public Set<GroupJoinRequest> getJoinRequests() { return joinRequests; }
    public void setJoinRequests(Set<GroupJoinRequest> joinRequests) { this.joinRequests = joinRequests; }
    
    // Helper methods
    public boolean isTemporary() {
        return type == GroupType.TEMPORARY;
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isMember(User user) {
        return members.contains(user);
    }
    
    public boolean isAdmin(User user) {
        return admins.contains(user) || creator.equals(user);
    }
    
    public boolean isBanned(User user) {
        return bannedUsers.contains(user);
    }
    
    public boolean isMuted(User user) {
        return mutedUsers.contains(user);
    }
    
    public void addMember(User user) {
        members.add(user);
        user.getGroups().add(this);
    }
    
    public void removeMember(User user) {
        members.remove(user);
        admins.remove(user);
        user.getGroups().remove(this);
    }
    
    public void addAdmin(User user) {
        if (isMember(user)) {
            admins.add(user);
        }
    }
    
    public void removeAdmin(User user) {
        if (!creator.equals(user)) {
            admins.remove(user);
        }
    }
    
    public void banUser(User user) {
        removeMember(user);
        bannedUsers.add(user);
    }
    
    public void muteUser(User user) {
        if (isMember(user) && !isAdmin(user)) {
            mutedUsers.add(user);
        }
    }
    
    public void unmuteUser(User user) {
        mutedUsers.remove(user);
    }
}
