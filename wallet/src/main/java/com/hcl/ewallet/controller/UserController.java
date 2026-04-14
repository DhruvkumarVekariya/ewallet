package com.hcl.ewallet.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcl.ewallet.dto.CreateUserRequest;
import com.hcl.ewallet.dto.UserResponse;
import com.hcl.ewallet.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * Create a new user
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        logger.info("API: Create user request for username: {}", request.getUsername());
        try {
            UserResponse response = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating user with username: {}", request.getUsername(), e);
            throw e;
        }
    }

    /**
     * Get user by ID
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        logger.info("API: Get user: {}", userId);
        try {
            UserResponse response = userService.getUser(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting user: {}", userId, e);
            throw e;
        }
    }

    /**
     * Get user by username
     * GET /api/users/username/{username}
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        logger.info("API: Get user by username: {}", username);
        try {
            UserResponse response = userService.getUserByUsername(username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting user by username: {}", username, e);
            throw e;
        }
    }
}
