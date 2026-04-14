package com.hcl.ewallet.service;

import com.hcl.ewallet.dto.CreateUserRequest;
import com.hcl.ewallet.dto.UserResponse;

public interface UserService {

    /**
     * Create a new user
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Get user by ID
     */
    UserResponse getUser(Long userId);

    /**
     * Get user by username
     */
    UserResponse getUserByUsername(String username);
}
