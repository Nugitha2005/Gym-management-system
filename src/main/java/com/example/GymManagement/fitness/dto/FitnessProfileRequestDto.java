package com.example.GymManagement.fitness.dto;

import com.example.GymManagement.fitness.entity.GoalType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Data the member sends when creating or updating their profile.
// No profileId, no user, no lastUpdated: the server decides those.
@Getter
@Setter
@NoArgsConstructor
public class FitnessProfileRequestDto {

    // 1. Realistic ranges stop typos like 1750 cm or 7 kg reaching the database
    @NotNull(message = "Height is required")
    @DecimalMin(value = "100.0", message = "Height must be at least 100 cm")
    @DecimalMax(value = "250.0", message = "Height must be at most 250 cm")
    private Double heightCm;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "30.0", message = "Weight must be at least 30 kg")
    @DecimalMax(value = "250.0", message = "Weight must be at most 250 kg")
    private Double weightKg;

    // 2. Must be one of WEIGHT_LOSS, MUSCLE_GAIN, GENERAL_FITNESS
    @NotNull(message = "Goal type is required")
    private GoalType goalType;

    // 3. Optional: a member doing GENERAL_FITNESS may not have a target weight
    @DecimalMin(value = "30.0", message = "Target weight must be at least 30 kg")
    @DecimalMax(value = "250.0", message = "Target weight must be at most 250 kg")
    private Double targetWeightKg;

    @Size(max = 500, message = "Medical notes must be 500 characters or fewer")
    private String medicalNotes;
}