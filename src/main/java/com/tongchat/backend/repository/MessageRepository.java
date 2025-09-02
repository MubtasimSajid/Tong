package com.tongchat.backend.repository;

import com.tongchat.backend.entity.Message;
import com.tongchat.backend.entity.User;
import com.tongchat.backend.entity.ChatGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Message Repository for database operations
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Private messages between two users
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender = :user1 AND m.recipient = :user2) OR " +
           "(m.sender = :user2 AND m.recipient = :user1) " +
           "ORDER BY m.createdAt ASC")
    List<Message> findPrivateMessagesBetweenUsers(@Param("user1") User user1, 
                                                 @Param("user2") User user2);
    
    // Group messages
    @Query("SELECT m FROM Message m WHERE m.group = :group ORDER BY m.createdAt ASC")
    List<Message> findMessagesByGroup(@Param("group") ChatGroup group);
    
    // Paginated group messages
    @Query("SELECT m FROM Message m WHERE m.group = :group ORDER BY m.createdAt DESC")
    Page<Message> findMessagesByGroupPaginated(@Param("group") ChatGroup group, Pageable pageable);
    
    // Messages sent by a user
    List<Message> findBySenderOrderByCreatedAtDesc(User sender);
    
    // Recent messages
    @Query("SELECT m FROM Message m WHERE m.createdAt >= :since ORDER BY m.createdAt DESC")
    List<Message> findRecentMessages(@Param("since") LocalDateTime since);
    
    // Notice messages in a group
    @Query("SELECT m FROM Message m WHERE m.group = :group AND m.type = 'NOTICE' ORDER BY m.createdAt DESC")
    List<Message> findNoticesByGroup(@Param("group") ChatGroup group);
    
    // Expired messages
    @Query("SELECT m FROM Message m WHERE m.expiresAt < :now")
    List<Message> findExpiredMessages(@Param("now") LocalDateTime now);
    
    // Unread messages for a user
    @Query("SELECT m FROM Message m WHERE m.recipient = :user AND m.status != 'READ'")
    List<Message> findUnreadMessagesForUser(@Param("user") User user);
    
    // Count messages in a group
    @Query("SELECT COUNT(m) FROM Message m WHERE m.group = :group")
    Long countMessagesByGroup(@Param("group") ChatGroup group);
    
    // Count private messages between users
    @Query("SELECT COUNT(m) FROM Message m WHERE " +
           "(m.sender = :user1 AND m.recipient = :user2) OR " +
           "(m.sender = :user2 AND m.recipient = :user1)")
    Long countPrivateMessagesBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
    
    // Search messages by content
    @Query("SELECT m FROM Message m WHERE m.content LIKE %:searchTerm% ORDER BY m.createdAt DESC")
    List<Message> searchMessagesByContent(@Param("searchTerm") String searchTerm);
}
