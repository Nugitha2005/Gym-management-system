package com.example.GymManagement.user.controller;

import com.example.GymManagement.user.dto.UserResponseDto;
import com.example.GymManagement.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ADMIN only — a STUDENT calling this receives 403 Forbidden
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {

        // 1. Fetch every user from the database
        // 2. Convert each User entity into a safe Response DTO (no passwords)
        List<UserResponseDto> users = userRepository.findAll()
                .stream()
                .map(UserResponseDto::new)
                .toList();

        // 3. Return HTTP 200 OK with the list
        return ResponseEntity.ok(users);
    }
}