package com.DigitalDining.demo.service;

import com.DigitalDining.demo.dto.RegisterRequest;
import com.DigitalDining.demo.dto.UserResponse;
import com.DigitalDining.demo.model.User;

public interface UserService {
	
	UserResponse registerUser(RegisterRequest request);

	boolean authenticate(String username, String password);

	UserResponse findByUsername(String username);
	
	User findEntityByUsername(String username);
	
	boolean existsByUsernameAndEmail(String username, String email);

    void resetPassword(String username, String email, String newPassword);
}
