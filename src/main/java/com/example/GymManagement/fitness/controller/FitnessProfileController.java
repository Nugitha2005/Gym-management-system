package com.example.GymManagement.fitness.controller;

import com.example.GymManagement.fitness.dto.FitnessProfileRequestDto;
import com.example.GymManagement.fitness.dto.FitnessProfileResponseDto;
import com.example.GymManagement.fitness.service.FitnessProfileService;
import com.example.GymManagement.user.security.UserPrincipal;
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

@RestController
@RequestMapping("/api/fitness-profiles")
public class FitnessProfileController {

    private final FitnessProfileService fitnessProfileService;

    public FitnessProfileController(FitnessProfileService fitnessProfileService) {
        this.fitnessProfileService = fitnessProfileService;
    }

    // ---------------- MY PROFILE ----------------
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FitnessProfileResponseDto> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {

        // 1. The user id comes from the login session, never from the URL.
        //    So a student cannot read someone else's profile by changing a number.
        Long myUserId = principal.getUser().getUserId();

        // 2. Ask the service for the profile
        FitnessProfileResponseDto response = fitnessProfileService.getProfileByUserId(myUserId);

        // 3. Return HTTP 200 OK
        return ResponseEntity.ok(response);
    }

    // ---------------- CREATE OR UPDATE MY PROFILE ----------------
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FitnessProfileResponseDto> saveMyProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                                   @Valid @RequestBody FitnessProfileRequestDto request) {

        // 1. Identify the logged-in member
        Long myUserId = principal.getUser().getUserId();

        // 2. The service creates the profile the first time and updates it after that
        FitnessProfileResponseDto response = fitnessProfileService.saveMyProfile(myUserId, request);

        // 3. Return HTTP 200 OK with the saved profile
        return ResponseEntity.ok(response);
    }

    // ---------------- VIEW A MEMBER'S PROFILE ----------------
    // Trainer, doctor or admin only. medicalNotes is sensitive health information.
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('TRAINER', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<FitnessProfileResponseDto> getProfileForUser(@PathVariable Long userId) {

        // 1. Look up the requested member's profile
        FitnessProfileResponseDto response = fitnessProfileService.getProfileByUserId(userId);

        // 2. Return HTTP 200 OK
        return ResponseEntity.ok(response);
    }
}