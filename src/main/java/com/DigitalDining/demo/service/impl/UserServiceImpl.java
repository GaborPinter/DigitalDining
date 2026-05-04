package com.DigitalDining.demo.service.impl;

import java.util.Optional;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.DigitalDining.demo.dto.RegisterRequest;
import com.DigitalDining.demo.dto.UserResponse;
import com.DigitalDining.demo.exception.ResourceAlreadyExistsException;
import com.DigitalDining.demo.model.Role;
import com.DigitalDining.demo.model.User;
import com.DigitalDining.demo.repository.UserRepository;
import com.DigitalDining.demo.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public UserResponse registerUser(RegisterRequest request) {
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new ResourceAlreadyExistsException("Felhasználónév már foglalt");
		}
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new ResourceAlreadyExistsException("Email cím már használatban van");
		}

		User user = new User();
		user.setUsername(request.getUsername());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(Role.USER);

		User saved = userRepository.save(user);

		return new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getRole().name(), saved.getRole().getDisplayName(),
				user.isWeeklyMenuSubscribed());
	}

	@Override
	public boolean authenticate(String username, String password) {
		if (username == null || username.isBlank() || password == null || password.isBlank()) {
			return false;
		}

		Optional<User> opt = userRepository.findByUsername(username);
		if (opt.isEmpty()) {
			return false;
		}

		User user = opt.get();
		return passwordEncoder.matches(password, user.getPassword());
	}

	@Override
	public UserResponse findByUsername(String username) {
		if (username == null || username.isBlank()) {
			return null;
		}

		return userRepository.findByUsername(username)
				.map(user -> new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name(), user.getRole().getDisplayName(),
						user.isWeeklyMenuSubscribed()))
				.orElse(null);
	}
	
	@Override
	public User findEntityByUsername(String username) {
	    return userRepository.findByUsername(username)
	            .orElseThrow(() -> new UsernameNotFoundException("Felhasználó nem található: " + username));
	}
	
	@Override
    @Transactional(readOnly = true)
    public boolean existsByUsernameAndEmail(String username, String email) {
        if (username == null || username.isBlank() || email == null || email.isBlank()) {
            return false;
        }
        return userRepository.findByUsernameAndEmail(username, email).isPresent();
    }
	
	@Override
    @Transactional
    public void resetPassword(String username, String email, String newPassword) {
        User user = userRepository.findByUsernameAndEmail(username, email)
                .orElseThrow(() -> new IllegalStateException("A megadott felhasználó nem található."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
	
	@Transactional
	public void subscribeToWeeklyMenu(String username) {
	    User user = userRepository.findByUsername(username)
	            .orElseThrow(() -> new IllegalArgumentException("Felhasználó nem található"));

	    user.setWeeklyMenuSubscribed(true);
	    userRepository.save(user);
	}
	
	@Transactional
	public void unsubscribeFromWeeklyMenu(String username) {
	    User user = userRepository.findByUsername(username)
	            .orElseThrow(() -> new IllegalArgumentException("Felhasználó nem található"));

	    user.setWeeklyMenuSubscribed(false);
	    userRepository.save(user);
	}
}
