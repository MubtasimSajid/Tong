package com.tongchat.backend.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * ChatRequest Entity - Represents temporary/permanent chat requests
 */
@Entity
@Table(name = "chat_requests")
@EntityListeners(AuditingEntityListener.class)
public class ChatRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestType type = RequestType.PERMANENT;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;
    
    public enum RequestType {
        TEMPORARY, PERMANENT
    }
    
    public enum RequestStatus {
        PENDING, ACCEPTED, DECLINED, EXPIRED
    }
    
    // Constructors
    public ChatRequest() {}
    
    public ChatRequest(User requester, User recipient, RequestType type, String message) {
        this.requester = requester;
        this.recipient = recipient;
        this.type = type;
        this.message = message;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public RequestType getType() { return type; }
    public void setType(RequestType type) { this.type = type; }
    
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
    
    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }
    
    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    
    // Helper methods
    public boolean isTemporary() {
        return type == RequestType.TEMPORARY;
    }
    
    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public void accept() {
        this.status = RequestStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }
    
    public void decline() {
        this.status = RequestStatus.DECLINED;
        this.respondedAt = LocalDateTime.now();
    }
    
    public void expire() {
        this.status = RequestStatus.EXPIRED;
    }
}
