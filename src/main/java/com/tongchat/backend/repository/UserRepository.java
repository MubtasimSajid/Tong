package com.tongchat.backend.repository;

import com.tongchat.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Repository for database operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Boolean existsByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE u.username LIKE %:searchTerm% OR u.displayName LIKE %:searchTerm%")
    List<User> searchByUsernameOrDisplayName(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT u FROM User u WHERE u.status = :status")
    List<User> findByStatus(@Param("status") User.UserStatus status);
    
    @Query("SELECT u FROM User u WHERE u.isAnonymous = true")
    List<User> findAllAnonymousUsers();
    
    @Query("SELECT u FROM User u WHERE u.lastSeen > :since")
    List<User> findActiveUsersSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'ONLINE'")
    Long countOnlineUsers();
    
    @Query("SELECT u FROM User u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
}
