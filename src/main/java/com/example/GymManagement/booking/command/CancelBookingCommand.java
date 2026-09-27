package com.example.GymManagement.booking.command;

import com.example.GymManagement.booking.dto.BookingResultDto;
import com.example.GymManagement.booking.entity.Booking;
import com.example.GymManagement.booking.entity.BookingStatus;
import com.example.GymManagement.booking.repository.BookingRepository;
import com.example.GymManagement.booking.repository.WaitlistEntryRepository;
import com.example.GymManagement.booking.service.BookingEligibilityChecker;
import com.example.GymManagement.config.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;

public class CancelBookingCommand implements BookingCommand {

    private final Long bookingId;
    private final Long userId;
    private final BookingRepository bookingRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final BookingEligibilityChecker eligibilityChecker;

    public CancelBookingCommand(Long bookingId, Long userId,
                                BookingRepository bookingRepository,
                                WaitlistEntryRepository waitlistEntryRepository,
                                BookingEligibilityChecker eligibilityChecker) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.bookingRepository = bookingRepository;
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.eligibilityChecker = eligibilityChecker;
    }

    @Override
    public BookingResultDto execute() {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking " + bookingId + " not found"));

        if (!booking.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only cancel your own bookings");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only CONFIRMED bookings can be cancelled");
        }

        // A booking can only be cancelled before its session starts.
        // After that the place can't be reused (offers stop at the start time).
        if (!booking.getSession().getStartTime().isAfter(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("This session has already started and can no longer be cancelled");
        }

        booking.cancel();
        bookingRepository.save(booking);

        BookingResultDto offerResult = new OfferFreedPlaceCommand(
                booking.getSession(), waitlistEntryRepository, eligibilityChecker
        ).execute();

        return new BookingResultDto("CANCELLED", "Booking cancelled successfully. " + offerResult.getMessage());
    }

    @Override
    public String describe() {
        return "CancelBookingCommand for booking ID " + bookingId + " by user " + userId;
    }
}