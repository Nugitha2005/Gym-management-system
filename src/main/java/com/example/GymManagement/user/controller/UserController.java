package com.example.GymManagement.user.controller;

import com.example.GymManagement.user.dto.ChangeRoleRequestDto;
import com.example.GymManagement.user.dto.UserResponseDto;
import com.example.GymManagement.user.entity.User;
import com.example.GymManagement.user.security.UserPrincipal;
import com.example.GymManagement.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ADMIN only — a STUDENT calling this receives 403 Forbidden
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {

        // 1. The service fetches every user and converts them to safe DTOs (no passwords)
        List<UserResponseDto> users = userService.getAllUsers();

        // 2. Return HTTP 200 OK with the list
        return ResponseEntity.ok(users);
    }

    // ADMIN only — change a user's role, e.g. make a lecturer UNIVERSITY_STAFF or a new hire TRAINER
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> changeRole(@PathVariable Long userId,
                                                      @AuthenticationPrincipal UserPrincipal principal,
                                                      @Valid @RequestBody ChangeRoleRequestDto request) {

        // 1. The acting admin comes from the login session, never from the request
        Long adminUserId = principal.getUser().getUserId();

        // 2. The service validates the role and blocks an admin changing their own role
        User updatedUser = userService.changeRole(userId, request.getRoleName(), adminUserId);

        // 3. Return HTTP 200 OK with the user's new details
        return ResponseEntity.ok(new UserResponseDto(updatedUser));
    }
}
