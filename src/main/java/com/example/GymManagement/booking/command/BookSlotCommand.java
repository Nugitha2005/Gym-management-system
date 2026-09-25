package com.example.GymManagement.booking.command;

import com.example.GymManagement.booking.dto.*;
import com.example.GymManagement.booking.entity.*;
import com.example.GymManagement.booking.repository.*;
import com.example.GymManagement.booking.service.BookingEligibilityChecker;
import com.example.GymManagement.config.ResourceNotFoundException;
import com.example.GymManagement.user.entity.User;
import com.example.GymManagement.user.repository.UserRepository;

public class BookSlotCommand implements BookingCommand {

    private final Long sessionId;
    private final Long userId;
    private final TrainingSessionRepository sessionRepository;
    private final BookingRepository bookingRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final UserRepository userRepository;
    private final BookingEligibilityChecker eligibilityChecker;

    public BookSlotCommand(Long sessionId, Long userId,
                           TrainingSessionRepository sessionRepository,
                           BookingRepository bookingRepository,
                           WaitlistEntryRepository waitlistEntryRepository,
                           UserRepository userRepository,
                           BookingEligibilityChecker eligibilityChecker) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.sessionRepository = sessionRepository;
        this.bookingRepository = bookingRepository;
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.userRepository = userRepository;
        this.eligibilityChecker = eligibilityChecker;
    }

    @Override
    public BookingResultDto execute() {
        TrainingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session " + sessionId + " not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " not found"));

        if (!eligibilityChecker.canUserBook(user, session)) {
            throw new IllegalArgumentException("User is not eligible to book or join the waitlist for this session");
        }

        int available = eligibilityChecker.calculateAvailablePlaces(session);

        if (available > 0) {
            Booking booking = new Booking(session, user);
            Booking saved = bookingRepository.save(booking);

            BookingResponseDto dto = new BookingResponseDto();
            dto.setBookingId(saved.getBookingId());
            dto.setSessionId(session.getSessionId());
            dto.setUserId(user.getUserId());
            dto.setStatus(saved.getStatus());
            dto.setBookingTime(saved.getBookingTime());
            dto.setSessionTitle(session.getTitle());
            dto.setStartTime(session.getStartTime());
            dto.setEndTime(session.getEndTime());

            return new BookingResultDto("BOOKED", "Slot booked successfully", dto);
        } else {
            WaitlistEntry entry = new WaitlistEntry(session, user);
            WaitlistEntry saved = waitlistEntryRepository.save(entry);

            WaitlistEntryResponseDto dto = new WaitlistEntryResponseDto();
            dto.setWaitlistId(saved.getWaitlistId());
            dto.setSessionId(session.getSessionId());
            dto.setUserId(user.getUserId());
            dto.setStatus(saved.getStatus());
            dto.setRequestedAt(saved.getRequestedAt());

            return new BookingResultDto("WAITLISTED", "Session full. Added to waitlist.", dto);
        }
    }

    @Override
    public String describe() {
        return "BookSlotCommand for session " + sessionId + " by user " + userId;
    }
}