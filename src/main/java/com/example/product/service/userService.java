package com.example.product.service;

import com.example.product.dto.LoginRequest;
import com.example.product.dto.LoginResponse;
import com.example.product.dto.SignupRequest;
import com.example.product.exception.InvalidCredentialsException;
import com.example.product.exception.UserAlreadyExistsException;
import com.example.product.jwt.JwtUtils;
import com.example.product.model.Role;
import com.example.product.model.userModel;
import com.example.product.repo.userRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class userService {

    @Autowired
    private userRepo userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    public LoginResponse signup(SignupRequest signupRequest) {
        // Check if email already exists
        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered: " + signupRequest.getEmail());
        }

        // Create new user
        userModel user = userModel.builder()
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .role(Role.USER)
                .build();

        userModel savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtils.generateJwtTokenFromEmail(savedUser.getEmail());

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .role(savedUser.getRole().name())
                .build();
    }

    public LoginResponse login(LoginRequest loginRequest) {
        // Find user by email
        userModel user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Validate password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtUtils.generateJwtTokenFromEmail(user.getEmail());

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    public userModel getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
    }

    public userModel getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
    }
}
