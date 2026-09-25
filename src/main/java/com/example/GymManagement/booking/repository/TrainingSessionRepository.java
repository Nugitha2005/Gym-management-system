package com.example.GymManagement.booking.repository;

import com.example.GymManagement.booking.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    @Query("SELECT s FROM TrainingSession s WHERE s.startTime >= :now AND s.cancelled = false ORDER BY s.startTime ASC")
    List<TrainingSession> findUpcomingSessions(@Param("now") LocalDateTime now);
}