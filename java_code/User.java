package com.example.model;

import java.util.Date;
import java.util.UUID;

/**
 * User entity class representing a system user
 * Contains user information and authentication details
 */
public class User {

    private UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private String role;
    private boolean isActive;
    private Date createdAt;
    private Date lastLogin;

    /**
     * Default constructor
     */
    public User() {
        this.id = UUID.randomUUID();
        this.createdAt = new Date();
        this.isActive = true;
    }

    /**
     * Constructor with username and email
     * @param username The username
     * @param email The user's email address
     */
    public User(String username, String email) {
        this();
        this.username = username;
        this.email = email;
        this.role = "USER"; // Default role
    }

    /**
     * Constructor with all fields
     */
    public User(String username, String email, String passwordHash, String role) {
        this(username, email);
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    /**
     * Validates user data before saving
     * @return true if user data is valid, false otherwise
     */
    public boolean validate() {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (email == null || !email.contains("@")) {
            return false;
        }
        if (passwordHash == null || passwordHash.length() < 60) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the user has admin privileges
     * @return true if user is an admin
     */
    public boolean isAdmin() {
        return "ADMIN".equals(this.role) || "SUPERADMIN".equals(this.role);
    }

    /**
     * Updates the last login timestamp to current time
     */
    public void updateLastLogin() {
        this.lastLogin = new Date();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
