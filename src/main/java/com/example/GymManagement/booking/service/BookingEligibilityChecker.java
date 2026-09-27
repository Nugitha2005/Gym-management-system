package com.example.GymManagement.booking.service;

import com.example.GymManagement.booking.entity.BookingStatus;
import com.example.GymManagement.booking.entity.SlotType;
import com.example.GymManagement.booking.entity.TrainingSession;
import com.example.GymManagement.booking.entity.WaitlistStatus;
import com.example.GymManagement.booking.repository.BookingRepository;
import com.example.GymManagement.booking.repository.WaitlistEntryRepository;
import com.example.GymManagement.access.service.MedicalClearanceService;
import com.example.GymManagement.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class BookingEligibilityChecker {

    private final BookingRepository bookingRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final MedicalClearanceService medicalClearanceService;

    public BookingEligibilityChecker(BookingRepository bookingRepository,
                                     WaitlistEntryRepository waitlistEntryRepository,
                                     MedicalClearanceService medicalClearanceService) {
        this.bookingRepository = bookingRepository;
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.medicalClearanceService = medicalClearanceService;
    }

    public boolean canUserBook(User user, TrainingSession session) {
        if (!isBasicEligibilityMet(user, session)) {
            return false;
        }

        boolean alreadyWaitlisted = waitlistEntryRepository.existsByUser_UserIdAndSession_SessionIdAndStatusIn(
                user.getUserId(), session.getSessionId(), List.of(WaitlistStatus.WAITING, WaitlistStatus.OFFERED));
        if (alreadyWaitlisted) return false;

        return checkPeakLimit(user);
    }

    public boolean canWaitlistedUserBeOffered(User user, TrainingSession session) {
        if (!isBasicEligibilityMet(user, session)) {
            return false;
        }

        return checkPeakLimit(user);
    }

    private boolean isBasicEligibilityMet(User user, TrainingSession session) {
        if (session.isCancelled() || session.getStartTime().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 5. Medical clearance check
        if (!medicalClearanceService.hasValidClearance(user.getUserId())) {
            return false;
        }

        // 6. Access hours check
        LocalTime sessionStart = session.getStartTime().toLocalTime();
        LocalTime sessionEnd = session.getEndTime().toLocalTime();
        LocalTime accessStart = user.getRole().getAccessStartTime();
        LocalTime accessEnd = user.getRole().getAccessEndTime();

        if (accessStart != null && accessEnd != null) {
            if (sessionStart.isBefore(accessStart) || sessionEnd.isAfter(accessEnd)) {
                return false;
            }
        }

        boolean alreadyBooked = bookingRepository.existsByUser_UserIdAndSession_SessionIdAndStatusIn(
                user.getUserId(), session.getSessionId(), List.of(BookingStatus.CONFIRMED, BookingStatus.ATTENDED));
        return !alreadyBooked;
    }

    private boolean checkPeakLimit(User user) {
        if (user.getRole() == null) return true;

        long peakBookingsCount = bookingRepository.findByUser_UserIdAndStatusInAndSession_StartTimeAfter(
                user.getUserId(), List.of(BookingStatus.CONFIRMED), LocalDateTime.now()
        ).stream().filter(b -> b.getSession().getSlotType() == SlotType.PEAK).count();

        return peakBookingsCount < 3;
    }

    public int calculateAvailablePlaces(TrainingSession session) {
        long confirmed = bookingRepository.countBySession_SessionIdAndStatusIn(
                session.getSessionId(), List.of(BookingStatus.CONFIRMED, BookingStatus.ATTENDED));
        long held = waitlistEntryRepository.countBySession_SessionIdAndStatusIn(
                session.getSessionId(), List.of(WaitlistStatus.OFFERED));

        return session.getMaxCapacity() - (int) (confirmed + held);
    }
}