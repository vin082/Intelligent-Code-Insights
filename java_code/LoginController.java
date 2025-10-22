package com.example.controller;

import com.example.service.AuthenticationService;
import com.example.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Login Controller
 * Handles user login and logout operations
 */
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * User login endpoint
     * @param username The username
     * @param password The password
     * @return JWT token if successful
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username,
                                       @RequestParam String password) {
        // Call authenticate method from AuthenticationService
        var authResult = authenticationService.authenticate(username, password);

        if (authResult.isPresent()) {
            return ResponseEntity.ok(authResult.get());
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    /**
     * User logout endpoint
     * @param token The JWT token to invalidate
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        authenticationService.logout(token);
        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * Check if user is logged in
     * @param token The JWT token
     * @return User details if valid
     */
    @GetMapping("/validate")
    public ResponseEntity<User> validateSession(@RequestHeader("Authorization") String token) {
        var userResult = authenticationService.validateToken(token);

        if (userResult.isPresent()) {
            return ResponseEntity.ok(userResult.get());
        } else {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Helper method to extract token from header
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }
}
