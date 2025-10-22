package com.example.service;

import com.example.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Authentication Service
 * Handles user authentication, password validation, and session management
 */
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    /**
     * Constructor with dependency injection
     */
    public AuthenticationService(UserRepository userRepository,
                                 BCryptPasswordEncoder passwordEncoder,
                                 TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    /**
     * Authenticates a user with username and password
     * @param username The username
     * @param password The plain text password
     * @return Authentication token if successful, empty Optional otherwise
     */
    public Optional<String> authenticate(String username, String password) {
        // Input validation
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // Find user by username
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            // User not found - return empty to prevent user enumeration
            return Optional.empty();
        }

        User user = userOptional.get();

        // Check if user is active
        if (!user.isActive()) {
            throw new SecurityException("User account is deactivated");
        }

        // Validate password using BCrypt
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            // Password doesn't match
            return Optional.empty();
        }

        // Update last login timestamp
        user.updateLastLogin();
        userRepository.save(user);

        // Generate and return JWT token
        String token = tokenService.generateToken(user);
        return Optional.of(token);
    }

    /**
     * Registers a new user in the system
     * @param username The desired username
     * @param email The user's email
     * @param password The plain text password
     * @return The created User object
     */
    public User register(String username, String email, String password) {
        // Validate inputs
        validateRegistrationInputs(username, email, password);

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create new user
        User user = new User(username, email);

        // Hash the password using BCrypt (10 rounds)
        String hashedPassword = passwordEncoder.encode(password);
        user.setPasswordHash(hashedPassword);

        // Set default role
        user.setRole("USER");

        // Save and return
        return userRepository.save(user);
    }

    /**
     * Validates a JWT token
     * @param token The JWT token to validate
     * @return User associated with the token if valid, empty Optional otherwise
     */
    public Optional<User> validateToken(String token) {
        try {
            // Validate token structure and signature
            if (!tokenService.isTokenValid(token)) {
                return Optional.empty();
            }

            // Extract username from token
            String username = tokenService.extractUsername(token);

            // Load user from database
            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty() || !userOptional.get().isActive()) {
                return Optional.empty();
            }

            return userOptional;
        } catch (Exception e) {
            // Token validation failed
            return Optional.empty();
        }
    }

    /**
     * Changes user password
     * @param username The username
     * @param oldPassword The current password
     * @param newPassword The new password
     * @return true if password changed successfully
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        // Authenticate with old password first
        Optional<String> authResult = authenticate(username, oldPassword);

        if (authResult.isEmpty()) {
            return false;
        }

        // Validate new password strength
        if (!isPasswordStrong(newPassword)) {
            throw new IllegalArgumentException("Password does not meet strength requirements");
        }

        // Get user and update password
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String newHashedPassword = passwordEncoder.encode(newPassword);
            user.setPasswordHash(newHashedPassword);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    /**
     * Validates registration inputs
     */
    private void validateRegistrationInputs(String username, String email, String password) {
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!isPasswordStrong(password)) {
            throw new IllegalArgumentException("Password must be at least 8 characters with uppercase, lowercase, and number");
        }
    }

    /**
     * Checks if password meets strength requirements
     * - At least 8 characters
     * - Contains uppercase letter
     * - Contains lowercase letter
     * - Contains number
     */
    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        return hasUpper && hasLower && hasDigit;
    }

    /**
     * Logs out a user by invalidating their token
     * @param token The JWT token to invalidate
     */
    public void logout(String token) {
        tokenService.invalidateToken(token);
    }
}
