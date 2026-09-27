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

    // Used by Member B's check-in: non-cancelled sessions running at the given moment, earliest first
    @Query("SELECT s FROM TrainingSession s WHERE s.cancelled = false " +
            "AND s.startTime <= :now AND s.endTime > :now ORDER BY s.startTime ASC")
    List<TrainingSession> findSessionsRunningAt(@Param("now") LocalDateTime now);

    // There is only one gym floor, so two non-cancelled sessions may not overlap in time
    @Query("SELECT COUNT(s) > 0 FROM TrainingSession s WHERE s.cancelled = false " +
            "AND s.startTime < :end AND s.endTime > :start")
    boolean existsOverlappingSession(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);
}