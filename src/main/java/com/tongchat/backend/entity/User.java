package com.tongchat.backend.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User Entity - Represents anonymous users in the chat system
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "display_name", length = 100)
    private String displayName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ONLINE;
    
    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous = true;
    
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Message> receivedMessages = new HashSet<>();
    
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Message> sentMessages = new HashSet<>();
    
    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    private Set<ChatGroup> groups = new HashSet<>();
    
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ChatGroup> createdGroups = new HashSet<>();
    
    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ChatRequest> sentRequests = new HashSet<>();
    
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ChatRequest> receivedRequests = new HashSet<>();
    
    public enum UserStatus {
        ONLINE, OFFLINE, AWAY, BUSY
    }
    
    // Constructors
    public User() {}
    
    public User(String username, String password, String displayName) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.isAnonymous = true;
        this.status = UserStatus.ONLINE;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    
    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean isAnonymous) { this.isAnonymous = isAnonymous; }
    
    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Set<Message> getReceivedMessages() { return receivedMessages; }
    public void setReceivedMessages(Set<Message> receivedMessages) { this.receivedMessages = receivedMessages; }
    
    public Set<Message> getSentMessages() { return sentMessages; }
    public void setSentMessages(Set<Message> sentMessages) { this.sentMessages = sentMessages; }
    
    public Set<ChatGroup> getGroups() { return groups; }
    public void setGroups(Set<ChatGroup> groups) { this.groups = groups; }
    
    public Set<ChatGroup> getCreatedGroups() { return createdGroups; }
    public void setCreatedGroups(Set<ChatGroup> createdGroups) { this.createdGroups = createdGroups; }
    
    public Set<ChatRequest> getSentRequests() { return sentRequests; }
    public void setSentRequests(Set<ChatRequest> sentRequests) { this.sentRequests = sentRequests; }
    
    public Set<ChatRequest> getReceivedRequests() { return receivedRequests; }
    public void setReceivedRequests(Set<ChatRequest> receivedRequests) { this.receivedRequests = receivedRequests; }
    
    // Helper methods
    public void updateLastSeen() {
        this.lastSeen = LocalDateTime.now();
    }
    
    public boolean isOnline() {
        return status == UserStatus.ONLINE;
    }
    
    public boolean isAnonymous() {
        return isAnonymous != null && isAnonymous;
    }
}
