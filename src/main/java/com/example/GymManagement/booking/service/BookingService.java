package com.example.GymManagement.booking.service;

import com.example.GymManagement.booking.command.*;
import com.example.GymManagement.booking.dto.*;
import com.example.GymManagement.booking.entity.*;
import com.example.GymManagement.booking.repository.*;
import com.example.GymManagement.config.ResourceNotFoundException;
import com.example.GymManagement.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final TrainingSessionRepository sessionRepository;
    private final BookingRepository bookingRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final UserRepository userRepository;
    private final BookingEligibilityChecker eligibilityChecker;
    private final BookingCommandInvoker commandInvoker;

    public BookingService(TrainingSessionRepository sessionRepository,
                          BookingRepository bookingRepository,
                          WaitlistEntryRepository waitlistEntryRepository,
                          UserRepository userRepository,
                          BookingEligibilityChecker eligibilityChecker,
                          BookingCommandInvoker commandInvoker) {
        this.sessionRepository = sessionRepository;
        this.bookingRepository = bookingRepository;
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.userRepository = userRepository;
        this.eligibilityChecker = eligibilityChecker;
        this.commandInvoker = commandInvoker;
    }

    @Transactional
    public BookingResultDto bookSlot(Long sessionId, Long userId) {
        BookingCommand command = new BookSlotCommand(
                sessionId, userId, sessionRepository, bookingRepository,
                waitlistEntryRepository, userRepository, eligibilityChecker
        );
        return commandInvoker.executeCommand(command);
    }

    @Transactional
    public BookingResultDto cancelBooking(Long bookingId, Long userId) {
        BookingCommand command = new CancelBookingCommand(
                bookingId, userId, bookingRepository, waitlistEntryRepository, eligibilityChecker
        );
        return commandInvoker.executeCommand(command);
    }

    public List<TrainingSessionResponseDto> getUpcomingSessions() {
        return sessionRepository.findUpcomingSessions(LocalDateTime.now())
                .stream()
                .map(this::mapToSessionDto)
                .collect(Collectors.toList());
    }

    // =====================================================================
    // Methods used by Member B's check-in feature (spec section 9.1)
    // =====================================================================

    // How early a booked student may scan in before their session starts
    private static final int EARLY_ENTRY_MINUTES = 10;

    @Transactional(readOnly = true)
    public Optional<BookingResponseDto> findBookingCoveringNow(Long userId, LocalDateTime now) {

        // 1. A booking "covers now" if its session starts within the next 10 minutes
        //    (or has already started) and has not ended yet
        LocalDateTime latestStart = now.plusMinutes(EARLY_ENTRY_MINUTES);

        // 2. Only CONFIRMED or ATTENDED bookings count (a student re-entering after a short break is ATTENDED)
        List<Booking> matches = bookingRepository.findActiveBookingsCoveringTime(
                userId, List.of(BookingStatus.CONFIRMED, BookingStatus.ATTENDED), latestStart, now);

        // 3. Return the earliest match as a DTO, or empty if the student has no booking right now
        return matches.stream().findFirst().map(this::mapToBookingDto);
    }

    @Transactional
    public void markAttended(Long bookingId) {

        // 1. Load the booking, or fail with 404
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking " + bookingId + " not found"));

        // 2. Only a CONFIRMED booking becomes ATTENDED. Anything else (already ATTENDED, CANCELLED)
        //    is left unchanged, so scanning in twice during one slot is harmless
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            booking.setStatus(BookingStatus.ATTENDED);
            bookingRepository.save(booking);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Integer> getCapacityAt(LocalDateTime now) {

        // 1. Find the session running right now and return its capacity.
        //    Every session is gym floor time (PEAK, OFF_PEAK and STANDARD only mark the time of day),
        //    so the session running now always describes the gym as a whole.
        return findSessionRunningAt(now).map(TrainingSession::getMaxCapacity);
    }

    @Transactional(readOnly = true)
    public int getReservedPlacesNotYetArrived(LocalDateTime now) {

        // 1. No session running means no places are reserved
        Optional<TrainingSession> running = findSessionRunningAt(now);
        if (running.isEmpty()) {
            return 0;
        }
        Long sessionId = running.get().getSessionId();

        // 2. Booked but not yet checked in (CONFIRMED only; ATTENDED students are already inside)
        long confirmedNotArrived = bookingRepository.countBySession_SessionIdAndStatusIn(
                sessionId, List.of(BookingStatus.CONFIRMED));

        // 3. Places held for students with an open waitlist offer
        long heldForOffers = waitlistEntryRepository.countBySession_SessionIdAndStatusIn(
                sessionId, List.of(WaitlistStatus.OFFERED));

        // 4. Both groups have a place reserved that walk-ins must not take
        return (int) (confirmedNotArrived + heldForOffers);
    }

    private Optional<TrainingSession> findSessionRunningAt(LocalDateTime now) {
        // Earliest-starting non-cancelled session covering this moment
        return sessionRepository.findSessionsRunningAt(now).stream().findFirst();
    }

    private BookingResponseDto mapToBookingDto(Booking booking) {
        TrainingSession session = booking.getSession();

        BookingResponseDto dto = new BookingResponseDto();
        dto.setBookingId(booking.getBookingId());
        dto.setSessionId(session.getSessionId());
        dto.setUserId(booking.getUser().getUserId());
        dto.setStatus(booking.getStatus());
        dto.setBookingTime(booking.getBookingTime());
        dto.setSessionTitle(session.getTitle());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        return dto;
    }

    private TrainingSessionResponseDto mapToSessionDto(TrainingSession session) {
        long confirmed = bookingRepository.countBySession_SessionIdAndStatusIn(
                session.getSessionId(), List.of(BookingStatus.CONFIRMED, BookingStatus.ATTENDED));
        long held = waitlistEntryRepository.countBySession_SessionIdAndStatusIn(
                session.getSessionId(), List.of(WaitlistStatus.OFFERED));

        TrainingSessionResponseDto dto = new TrainingSessionResponseDto();
        dto.setSessionId(session.getSessionId());
        dto.setTitle(session.getTitle());
        dto.setInstructor(session.getInstructor());
        dto.setSlotType(session.getSlotType());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setMaxCapacity(session.getMaxCapacity());
        dto.setBookedCount((int) confirmed);
        dto.setHeldCount((int) held);
        dto.setAvailablePlaces(session.getMaxCapacity() - (int)(confirmed + held));
        dto.setCancelled(session.isCancelled());
        return dto;
    }
}