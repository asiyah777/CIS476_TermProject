package com.driveshare.controller;

import com.driveshare.model.User;
import com.driveshare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserRepository userRepository;

    /** Returns the display name for a given user ID. Only exposes id and username — no sensitive fields. */
    @GetMapping("/{id}/name")
    public ResponseEntity<Map<String, Object>> getUserName(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(Map.of("id", id, "username", "User " + id));
        }
        String name = (user.getUsername() != null && !user.getUsername().isBlank())
                ? user.getUsername() : "User " + id;
        return ResponseEntity.ok(Map.of("id", id, "username", name));
    }
}
