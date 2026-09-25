package com.example.GymManagement.user.service;

import com.example.GymManagement.user.entity.Role;
import com.example.GymManagement.user.entity.User;
import com.example.GymManagement.user.repository.RoleRepository;
import com.example.GymManagement.user.repository.UserRepository;
import com.example.GymManagement.config.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.GymManagement.user.dto.RegisterRequestDto;

import java.util.UUID;

@Service
public class UserService {
    private static final String DEFAULT_ROLE = "STUDENT";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (userRepository.existsByUniversityId(request.getUniversityId())) {
            throw new IllegalArgumentException("University ID already registered");
        }

        Role studentRole = roleRepository.findByRoleName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException(
                        "Default role " + DEFAULT_ROLE + " not found. Check data.sql seed data."));

        User user = new User();
        user.setName(request.getName());
        user.setUniversityId(request.getUniversityId());
        user.setEmail(request.getEmail());
        user.setContactNo(request.getContactNo());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setQrCode(UUID.randomUUID().toString());
        user.setRole(studentRole);
        user.setActive(true);

        return userRepository.save(user);
    }

    // Admin changes a user's role, e.g. STUDENT -> UNIVERSITY_STAFF or STUDENT -> TRAINER
    @Transactional
    public User changeRole(Long targetUserId, String roleName, Long adminUserId) {

        // 1. An admin may not change their own role, so the system can never be left without an admin
        if (targetUserId.equals(adminUserId)) {
            throw new IllegalArgumentException("You cannot change your own role");
        }

        // 2. Load the user whose role is changing, or fail with 404
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + targetUserId + " not found"));

        // 3. Look up the new role by name (case-insensitive input, stored names are uppercase)
        String normalisedRoleName = roleName.trim().toUpperCase();
        Role newRole = roleRepository.findByRoleName(normalisedRoleName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + normalisedRoleName));

        // 4. Assign it and save. The user's new role applies from their next login,
        //    because the role is read into the session at login time.
        user.setRole(newRole);
        return userRepository.save(user);
    }
}
