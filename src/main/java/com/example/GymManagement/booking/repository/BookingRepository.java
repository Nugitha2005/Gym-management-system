package com.example.GymManagement.booking.repository;

import com.example.GymManagement.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Query 2: Booking count per session
    @Query("SELECT b.session.id, COUNT(b) FROM Booking b GROUP BY b.session.id")
    List<Object[]> countBookingsPerSession();

    // Query 3: A user's bookings with full session details
    @Query("SELECT b FROM Booking b JOIN FETCH b.session WHERE b.user.userId = :userId")
    List<Booking> findByUserIdWithSessionDetails(@Param("userId") Long userId);
}