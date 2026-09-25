package com.example.GymManagement.fitness.service;

import com.example.GymManagement.config.ResourceNotFoundException;
import com.example.GymManagement.fitness.dto.FitnessProfileRequestDto;
import com.example.GymManagement.fitness.dto.FitnessProfileResponseDto;
import com.example.GymManagement.fitness.entity.FitnessProfile;
import com.example.GymManagement.fitness.repository.FitnessProfileRepository;
import com.example.GymManagement.user.entity.User;
import com.example.GymManagement.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FitnessProfileService {

    private final FitnessProfileRepository fitnessProfileRepository;
    private final UserRepository userRepository;

    public FitnessProfileService(FitnessProfileRepository fitnessProfileRepository,
                                 UserRepository userRepository) {
        this.fitnessProfileRepository = fitnessProfileRepository;
        this.userRepository = userRepository;
    }

    // ---------------- CREATE OR UPDATE ----------------
    // One method handles both, because each user has exactly one profile.
    @Transactional
    public FitnessProfileResponseDto saveMyProfile(Long userId, FitnessProfileRequestDto request) {

        // 1. Load the current user fresh from the database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Reuse the existing profile if there is one, otherwise start a new one.
        //    This enforces "one profile per user" in the business logic,
        //    on top of the unique constraint on user_id in the database.
        FitnessProfile profile = fitnessProfileRepository.findByUser_UserId(userId)
                .orElseGet(() -> {
                    FitnessProfile newProfile = new FitnessProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        // 3. Copy the submitted values onto the entity
        profile.setHeightCm(request.getHeightCm());
        profile.setWeightKg(request.getWeightKg());
        profile.setGoalType(request.getGoalType());
        profile.setTargetWeightKg(request.getTargetWeightKg());
        profile.setMedicalNotes(request.getMedicalNotes());

        // 4. Record when it was last changed
        profile.setLastUpdated(LocalDateTime.now());

        // 5. save() INSERTs a new row or UPDATEs the existing one, depending on whether profileId is set
        FitnessProfile savedProfile = fitnessProfileRepository.save(profile);

        // 6. Convert the saved entity into a response DTO (includes BMI)
        return new FitnessProfileResponseDto(savedProfile);
    }

    // ---------------- READ ----------------
    @Transactional(readOnly = true)
    public FitnessProfileResponseDto getProfileByUserId(Long userId) {

        // 1. Find the profile, or answer 404 if the member has not created one yet
        FitnessProfile profile = fitnessProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No fitness profile found for this user"));

        // 2. Convert to a response DTO
        return new FitnessProfileResponseDto(profile);
    }
}