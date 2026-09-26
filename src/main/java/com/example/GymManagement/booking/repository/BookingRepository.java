package com.example.GymManagement.booking.repository;

import com.example.GymManagement.booking.entity.Booking;
import com.example.GymManagement.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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

    // 7. Used to query ended sessions that are still CONFIRMED
    List<Booking> findByStatusAndSession_EndTimeBefore(BookingStatus status, LocalDateTime now);
}