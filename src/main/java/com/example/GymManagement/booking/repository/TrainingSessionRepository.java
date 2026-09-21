package com.example.GymManagement.booking.repository;

import com.example.GymManagement.booking.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    // Query 1: Upcoming sessions with available slots
    @Query("SELECT s FROM TrainingSession s WHERE s.startTime > CURRENT_TIMESTAMP AND s.currentCapacity < s.maxCapacity")
    List<TrainingSession> findAvailableUpcomingSessions();

    // Query 4: Most popular sessions by booking count
    @Query("SELECT s FROM TrainingSession s ORDER BY s.currentCapacity DESC")
    List<TrainingSession> findMostPopularSessions();
}