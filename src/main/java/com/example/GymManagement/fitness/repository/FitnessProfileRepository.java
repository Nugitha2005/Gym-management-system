package com.example.GymManagement.fitness.repository;

import com.example.GymManagement.fitness.entity.FitnessProfile;
import com.example.GymManagement.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FitnessProfileRepository extends JpaRepository<FitnessProfile, Long> {

    // 1. Load the logged-in member's own profile
    Optional<FitnessProfile> findByUser(User user);

    // 2. Same lookup by id, used when the trainer views a specific member
    Optional<FitnessProfile> findByUser_UserId(Long userId);

    // 3. Used before creating a profile, so a member cannot end up with two
    boolean existsByUser_UserId(Long userId);
}
