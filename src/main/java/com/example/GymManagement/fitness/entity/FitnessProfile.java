package com.example.GymManagement.fitness.entity;

import com.example.GymManagement.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Student ID: [IT25100964] | Table: fitness_profile
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fitness_profile")
public class FitnessProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    // 1. One profile belongs to exactly one user.
    //    @OneToOne is correct here (unlike Role, which is @ManyToOne) because a user
    //    can never have two fitness profiles — JPA adds a unique constraint on user_id.
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 2. Physical measurements used for BMI
    @Column(nullable = false)
    private Double heightCm;

    @Column(nullable = false)
    private Double weightKg;

    // 3. What the member is training towards.
    //    EnumType.STRING stores "WEIGHT_LOSS" in MySQL instead of a meaningless number,
    //    so the database stays readable and safe to reorder the enum later.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GoalType goalType = GoalType.GENERAL_FITNESS;

    private Double targetWeightKg;

    // 4. Health conditions the trainer must know about (asthma, injuries, allergies)
    @Column(length = 500)
    private String medicalNotes;

    @Column(nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    // 5. Domain logic that belongs on the entity, because it only reads this object's own fields.
    //    Keeping it here (instead of in the service) is proper encapsulation — worth noting in the viva.
    public Double calculateBmi() {

        // 5a. Guard against missing or zero height to avoid a divide-by-zero
        if (heightCm == null || weightKg == null || heightCm <= 0) {
            return null;
        }

        // 5b. BMI formula expects height in metres, so convert from cm
        double heightMetres = heightCm / 100.0;
        double bmi = weightKg / (heightMetres * heightMetres);

        // 5c. Round to one decimal place for display
        return Math.round(bmi * 10.0) / 10.0;
    }

    // 6. Converts the numeric BMI into the standard WHO category shown in the UI
    public String getBmiCategory() {

        Double bmi = calculateBmi();

        if (bmi == null) {
            return "UNKNOWN";
        }
        if (bmi < 18.5) {
            return "UNDERWEIGHT";
        }
        if (bmi < 25.0) {
            return "NORMAL";
        }
        if (bmi < 30.0) {
            return "OVERWEIGHT";
        }
        return "OBESE";
    }
}
