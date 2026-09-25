package com.example.GymManagement.booking.service;

import com.example.GymManagement.booking.entity.BookingStatus;
import com.example.GymManagement.booking.entity.SlotType;
import com.example.GymManagement.booking.entity.TrainingSession;
import com.example.GymManagement.booking.entity.WaitlistStatus;
import com.example.GymManagement.booking.repository.BookingRepository;
import com.example.GymManagement.booking.repository.WaitlistEntryRepository;
import com.example.GymManagement.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingEligibilityChecker {

    private final BookingRepository bookingRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;

    public BookingEligibilityChecker(BookingRepository bookingRepository, WaitlistEntryRepository waitlistEntryRepository) {
        this.bookingRepository = bookingRepository;
        this.waitlistEntryRepository = waitlistEntryRepository;
    }

    public boolean canUserBook(User user, TrainingSession session) {
        if (session.isCancelled() || session.getStartTime().isBefore(LocalDateTime.now())) {
            return false;
        }

        boolean alreadyBooked = bookingRepository.existsByUser_UserIdAndSession_SessionIdAndStatusIn(
                user.getUserId(), session.getSessionId(), List.of(BookingStatus.CONFIRMED, BookingStatus.ATTENDED));
        if (alreadyBooked) return false;

        boolean alreadyWaitlisted = waitlistEntryRepository.existsByUser_UserIdAndSession_SessionIdAndStatusIn(
                user.getUserId(), session.getSessionId(), List.of(WaitlistStatus.WAITING, WaitlistStatus.OFFERED));
        if (alreadyWaitlisted) return false;

        if (session.getSlotType() == SlotType.PEAK) {
            long peakBookingsCount = bookingRepository.findByUser_UserIdAndStatusInAndSession_StartTimeAfter(
                    user.getUserId(), List.of(BookingStatus.CONFIRMED), LocalDateTime.now()
            ).stream().filter(b -> b.getSession().getSlotType() == SlotType.PEAK).count();

            if (peakBookingsCount >= 3) return false;
        }

        return true;
    }

    public int calculateAvailablePlaces(TrainingSession session) {
        long confirmed = bookingRepository.countBySession_SessionIdAndStatusIn(
                session.getSessionId(), List.of(BookingStatus.CONFIRMED, BookingStatus.ATTENDED));
        long held = waitlistEntryRepository.countBySession_SessionIdAndStatusIn(
                session.getSessionId(), List.of(WaitlistStatus.OFFERED));

        return session.getMaxCapacity() - (int) (confirmed + held);
    }
}