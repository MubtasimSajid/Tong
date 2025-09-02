package com.tongchat.backend.service;

import com.tongchat.backend.entity.User;
import com.tongchat.backend.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * User Service - Business logic for user operations
 */
@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // Constructor injection
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    // Anonymous name generators
    private static final String[] ADJECTIVES = {
        "Swift", "Mystic", "Crystal", "Silent", "Golden", "Shadow", "Brave", "Cosmic",
        "Electric", "Fierce", "Gentle", "Hidden", "Noble", "Quick", "Royal", "Smart",
        "Vibrant", "Wild", "Zen", "Bright", "Dark", "Epic", "Fast", "Great", "Happy",
        "Iron", "Jade", "Kind", "Lucky", "Magic", "Nova", "Ocean", "Pure", "Quiet"
    };
    
    private static final String[] NOUNS = {
        "Wolf", "Tiger", "Dragon", "Phoenix", "Eagle", "Lion", "Falcon", "Panther",
        "Bear", "Hawk", "Shark", "Raven", "Fox", "Lynx", "Jaguar", "Cobra", "Owl",
        "Leopard", "Stallion", "Thunder", "Lightning", "Storm", "Blaze", "Frost",
        "Star", "Moon", "Sun", "Wind", "Fire", "Ice", "Stone", "Steel", "Gem"
    };
    
    /**
     * Create anonymous user account
     */
    public User createAnonymousUser() {
        String username = generateAnonymousUsername();
        String password = generateSecurePassword();
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setDisplayName(username);
        user.setIsAnonymous(true);
        user.setStatus(User.UserStatus.ONLINE);
        user.updateLastSeen();
        
        User savedUser = userRepository.save(user);
        
        // Return user with plain password for display (one-time only)
        savedUser.setPassword(password);
        return savedUser;
    }
    
    /**
     * Authenticate user
     */
    public Optional<User> authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                user.setStatus(User.UserStatus.ONLINE);
                user.updateLastSeen();
                return Optional.of(userRepository.save(user));
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Search users by username or display name
     */
    @Transactional(readOnly = true)
    public List<User> searchUsers(String searchTerm) {
        return userRepository.searchByUsernameOrDisplayName(searchTerm);
    }
    
    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Update user status
     */
    public User updateUserStatus(Long userId, User.UserStatus status) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setStatus(status);
        user.updateLastSeen();
        
        return userRepository.save(user);
    }
    
    /**
     * Get online users count
     */
    @Transactional(readOnly = true)
    public Long getOnlineUsersCount() {
        return userRepository.countOnlineUsers();
    }
    
    /**
     * Get active users since specific time
     */
    @Transactional(readOnly = true)
    public List<User> getActiveUsersSince(LocalDateTime since) {
        return userRepository.findActiveUsersSince(since);
    }
    
    /**
     * Check if username exists
     */
    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Generate unique anonymous username
     */
    private String generateAnonymousUsername() {
        Random random = new Random();
        String username;
        int attempts = 0;
        
        do {
            String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
            String noun = NOUNS[random.nextInt(NOUNS.length)];
            int number = random.nextInt(999) + 1;
            
            username = adjective + noun + number;
            attempts++;
            
            // Fallback if too many attempts
            if (attempts > 10) {
                username = "User" + System.currentTimeMillis() % 10000;
                break;
            }
            
        } while (usernameExists(username));
        
        return username;
    }
    
    /**
     * Generate secure random password
     */
    private String generateSecurePassword() {
        String upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
        String lowerCaseLetters = RandomStringUtils.random(4, 97, 122, true, true);
        String numbers = RandomStringUtils.randomNumeric(3);
        String specialChar = RandomStringUtils.random(3, 33, 47, false, false);
        
        String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
                                             .concat(numbers)
                                             .concat(specialChar);
        
        List<Character> pwdChars = combinedChars.chars()
                                               .mapToObj(c -> (char) c)
                                               .collect(java.util.stream.Collectors.toList());
        
        java.util.Collections.shuffle(pwdChars);
        
        return pwdChars.stream()
                      .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                      .toString();
    }
}
