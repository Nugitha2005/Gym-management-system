package com.example.GymManagement.user.service;

import com.example.GymManagement.user.entity.Role;
import com.example.GymManagement.user.entity.User;
import com.example.GymManagement.user.repository.RoleRepository;
import com.example.GymManagement.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
}
