package com.tongchat.backend.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * GroupJoinRequest Entity - Represents requests to join groups
 */
@Entity
@Table(name = "group_join_requests")
@EntityListeners(AuditingEntityListener.class)
public class GroupJoinRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ChatGroup group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;
    
    public enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }
    
    // Constructors
    public GroupJoinRequest() {}
    
    public GroupJoinRequest(User user, ChatGroup group, String message) {
        this.user = user;
        this.group = group;
        this.message = message;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public ChatGroup getGroup() { return group; }
    public void setGroup(ChatGroup group) { this.group = group; }
    
    public User getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(User reviewedBy) { this.reviewedBy = reviewedBy; }
    
    // Helper methods
    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }
    
    public void approve(User admin) {
        this.status = RequestStatus.APPROVED;
        this.reviewedBy = admin;
        this.respondedAt = LocalDateTime.now();
    }
    
    public void reject(User admin) {
        this.status = RequestStatus.REJECTED;
        this.reviewedBy = admin;
        this.respondedAt = LocalDateTime.now();
    }
}
