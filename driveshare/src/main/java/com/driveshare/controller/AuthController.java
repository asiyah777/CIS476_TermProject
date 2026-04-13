package com.driveshare.controller;

import com.driveshare.dto.LoginRequest;
import com.driveshare.dto.RegisterRequest;
import com.driveshare.model.User;
import com.driveshare.repository.UserRepository;
import com.driveshare.service.UserService;
import com.driveshare.patterns.singleton.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserService    userService;
    @Autowired private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userService.authenticate(request.getEmail(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        // Create a per-user session token via the Singleton SessionManager
        String token = SessionManager.getInstance().createSession(user);

        // Return both the user and their session token so the frontend can store it
        return ResponseEntity.ok(Map.of(
            "token",    token,
            "userId",   user.getId(),
            "username", user.getUsername() != null ? user.getUsername() : ""
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        userService.registerUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getSecurityAnswer1(),
                request.getSecurityAnswer2(),
                request.getSecurityAnswer3()
        );
        return ResponseEntity.ok("Registration successful");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, Object> req) {
        try {
            String email = req.get("email").toString();
            Object answersObj = req.get("answers");
            String newPassword = req.get("newPassword").toString();

            // Validate input
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required.");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("New password is required.");
            }
            if (!(answersObj instanceof List)) {
                return ResponseEntity.badRequest().body("Security answers must be provided as a list.");
            }

            @SuppressWarnings("unchecked")
            List<String> answers = (List<String>) answersObj;

            if (answers.size() != 3) {
                return ResponseEntity.badRequest().body("Exactly 3 security answers are required.");
            }

            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body("No account found with that email address.");
            }

            boolean success = userService.resetPassword(user, answers, newPassword);
            if (success) {
                // Password is already set by the Chain of Responsibility in UserService.resetPassword()
                userRepository.save(user); // Save the updated user
                return ResponseEntity.ok("Password reset successful!");
            }
            return ResponseEntity.badRequest().body("One or more security answers were incorrect.");
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error during password reset: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("An error occurred during password reset. Please try again.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "X-Session-Token", required = false) String token) {
        SessionManager.getInstance().removeSession(token);
        return ResponseEntity.ok("Logged out");
    }
}
