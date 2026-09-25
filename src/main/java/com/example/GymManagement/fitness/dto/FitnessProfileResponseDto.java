package com.example.GymManagement.fitness.dto;

import com.example.GymManagement.fitness.entity.FitnessProfile;
import com.example.GymManagement.fitness.entity.GoalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// What the API sends back. Includes the computed BMI values, never the User entity itself.
@Getter
@Setter
@NoArgsConstructor
public class FitnessProfileResponseDto {

    private Long profileId;
    private Long userId;
    private String userName;
    private Double heightCm;
    private Double weightKg;
    private GoalType goalType;
    private Double targetWeightKg;
    private String medicalNotes;
    private Double bmi;
    private String bmiCategory;
    private LocalDateTime lastUpdated;

    public FitnessProfileResponseDto(FitnessProfile profile) {

        // 1. Copy the stored fields
        this.profileId = profile.getProfileId();
        this.heightCm = profile.getHeightCm();
        this.weightKg = profile.getWeightKg();
        this.goalType = profile.getGoalType();
        this.targetWeightKg = profile.getTargetWeightKg();
        this.medicalNotes = profile.getMedicalNotes();
        this.lastUpdated = profile.getLastUpdated();

        // 2. Only the owner's id and name. Never the whole User, which would leak the password hash.
        this.userId = profile.getUser().getUserId();
        this.userName = profile.getUser().getName();

        // 3. BMI values are methods on the entity, not fields, so they must be copied in here
        //    or they would never appear in the JSON response
        this.bmi = profile.calculateBmi();
        this.bmiCategory = profile.getBmiCategory();
    }
}