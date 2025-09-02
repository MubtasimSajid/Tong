package com.tongchat.backend.repository;

import com.tongchat.backend.entity.ChatGroup;
import com.tongchat.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ChatGroup Repository for database operations
 */
@Repository
public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
    
    List<ChatGroup> findByCreator(User creator);
    
    @Query("SELECT g FROM ChatGroup g WHERE g.name LIKE %:searchTerm%")
    List<ChatGroup> searchByName(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT g FROM ChatGroup g WHERE g.type = :type")
    List<ChatGroup> findByType(@Param("type") ChatGroup.GroupType type);
    
    @Query("SELECT g FROM ChatGroup g WHERE g.visibility = :visibility")
    List<ChatGroup> findByVisibility(@Param("visibility") ChatGroup.GroupVisibility visibility);
    
    @Query("SELECT g FROM ChatGroup g WHERE g.isActive = true")
    List<ChatGroup> findAllActiveGroups();
    
    @Query("SELECT g FROM ChatGroup g WHERE g.expiresAt < :now AND g.type = 'TEMPORARY'")
    List<ChatGroup> findExpiredTemporaryGroups(@Param("now") LocalDateTime now);
    
    @Query("SELECT g FROM ChatGroup g JOIN g.members m WHERE m = :user")
    List<ChatGroup> findGroupsByMember(@Param("user") User user);
    
    @Query("SELECT g FROM ChatGroup g JOIN g.admins a WHERE a = :user OR g.creator = :user")
    List<ChatGroup> findGroupsByAdmin(@Param("user") User user);
    
    @Query("SELECT g FROM ChatGroup g WHERE g.visibility = 'PUBLIC' AND g.isActive = true")
    List<ChatGroup> findPublicGroups();
    
    @Query("SELECT COUNT(m) FROM ChatGroup g JOIN g.members m WHERE g.id = :groupId")
    Long countMembersByGroupId(@Param("groupId") Long groupId);
}
