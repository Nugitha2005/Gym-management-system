package com.example.GymManagement.booking.repository;

import com.example.GymManagement.booking.entity.Booking;
import com.example.GymManagement.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    long countBySession_SessionIdAndStatusIn(Long sessionId, List<BookingStatus> statuses);

    boolean existsByUser_UserIdAndSession_SessionIdAndStatusIn(Long userId, Long sessionId, List<BookingStatus> statuses);

    Optional<Booking> findByUser_UserIdAndSession_SessionIdAndStatusIn(Long userId, Long sessionId, List<BookingStatus> statuses);

    List<Booking> findByUser_UserIdAndStatusInAndSession_StartTimeAfter(
            Long userId, List<BookingStatus> statuses, LocalDateTime now);

    // Used by Member B's check-in: a user's active booking whose session covers the given moment.
    // JOIN FETCH loads the session in the same query, so its title and times are available afterwards.
    @Query("SELECT b FROM Booking b JOIN FETCH b.session s " +
            "WHERE b.user.userId = :userId " +
            "AND b.status IN :statuses " +
            "AND s.cancelled = false " +
            "AND s.startTime <= :latestStart " +
            "AND s.endTime > :now " +
            "ORDER BY s.startTime ASC")
    List<Booking> findActiveBookingsCoveringTime(@Param("userId") Long userId,
                                                 @Param("statuses") List<BookingStatus> statuses,
                                                 @Param("latestStart") LocalDateTime latestStart,
                                                 @Param("now") LocalDateTime now);
}