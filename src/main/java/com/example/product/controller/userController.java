package com.example.product.controller;

import com.example.product.dto.LoginRequest;
import com.example.product.dto.LoginResponse;
import com.example.product.dto.SignupRequest;
import com.example.product.model.userModel;
import com.example.product.service.userService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class userController {

    @Autowired
    private userService userService;

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        LoginResponse response = userService.signup(signupRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = userService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<userModel> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        userModel user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/")
    public String greet() {
        return "hello";
    }
}
