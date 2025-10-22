package com.example.service;

import com.example.model.User;
import com.example.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User Service
 * Handles user-related business logic
 */
@Service
public class UserService {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Login user with credentials
     * This is a wrapper around authentication service
     */
    public User login(String username, String password) {
        // Call authenticate from AuthenticationService
        var tokenResult = authenticationService.authenticate(username, password);

        if (tokenResult.isPresent()) {
            var userResult = userRepository.findByUsername(username);
            return userResult.orElse(null);
        }

        return null;
    }

    /**
     * Get user by ID with authentication check
     */
    public User getUserById(Long userId, String authToken) {
        // Validate the token first
        var validatedUser = authenticationService.validateToken(authToken);

        if (validatedUser.isEmpty()) {
            throw new SecurityException("Invalid authentication token");
        }

        return userRepository.findById(userId).orElse(null);
    }

    /**
     * Update user profile
     */
    public User updateUser(Long userId, User updatedUser, String authToken) {
        // Authenticate first
        var validatedUser = authenticationService.validateToken(authToken);

        if (validatedUser.isEmpty()) {
            throw new SecurityException("Not authenticated");
        }

        // Validate user data
        if (!updatedUser.validate()) {
            throw new IllegalArgumentException("Invalid user data");
        }

        // Update in repository
        return userRepository.save(updatedUser);
    }

    /**
     * Delete user account
     */
    public boolean deleteUser(Long userId, String password, String authToken) {
        // Validate authentication
        var validatedUser = authenticationService.validateToken(authToken);

        if (validatedUser.isEmpty()) {
            return false;
        }

        // Re-authenticate with password for sensitive operation
        User user = validatedUser.get();
        var reauth = authenticationService.authenticate(user.getUsername(), password);

        if (reauth.isEmpty()) {
            return false;
        }

        userRepository.deleteById(userId);
        return true;
    }

    /**
     * Change password
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        return authenticationService.changePassword(username, oldPassword, newPassword);
    }
}
