package com.example.GymManagement.booking.service;

import com.example.GymManagement.booking.command.*;
import com.example.GymManagement.booking.dto.*;
import com.example.GymManagement.booking.entity.*;
import com.example.GymManagement.booking.repository.*;
import com.example.GymManagement.config.ResourceNotFoundException;
import com.example.GymManagement.user.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
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

    // 3. Accept Waitlist Offer
    //    NOTE: once a status has changed, this method RETURNS a result instead of throwing.
    //    Throwing inside @Transactional rolls everything back, which would undo the EXPIRED/CANCELLED
    //    status and the offer to the next person.
    @Transactional
    public BookingResultDto acceptWaitlistOffer(Long waitlistId, Long userId) {

        // 1. Load the entry, or fail with 404
        WaitlistEntry entry = waitlistEntryRepository.findById(waitlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry " + waitlistId + " not found"));

        // 2. Only the student the offer was made to can accept it
        if (!entry.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only accept your own waitlist offers");
        }

        // 3. Only an open offer can be accepted
        if (entry.getStatus() != WaitlistStatus.OFFERED) {
            throw new IllegalArgumentException("Waitlist entry is not in OFFERED status");
        }

        // 4. Offer ran out: mark it EXPIRED and pass the place to the next person
        if (entry.getOfferExpiresAt().isBefore(LocalDateTime.now())) {
            entry.setStatus(WaitlistStatus.EXPIRED);
            waitlistEntryRepository.save(entry);
            offerPlaceToNextPerson(entry.getSession());
            return new BookingResultDto("EXPIRED", "This offer has expired and was passed to the next person.");
        }

        // 5. Re-check eligibility: clearance may have expired, or the limits changed since the offer
        if (!eligibilityChecker.canWaitlistedUserBeOffered(entry.getUser(), entry.getSession())) {
            entry.setStatus(WaitlistStatus.CANCELLED);
            waitlistEntryRepository.save(entry);
            offerPlaceToNextPerson(entry.getSession());
            return new BookingResultDto("NOT_ELIGIBLE",
                    "You are no longer eligible for this session (check your medical clearance and booking limits).");
        }

        // 6. Accept: close the waitlist entry and create the confirmed booking
        entry.setStatus(WaitlistStatus.CONVERTED);
        waitlistEntryRepository.save(entry);

        Booking booking = new Booking(entry.getSession(), entry.getUser());
        Booking savedBooking = bookingRepository.save(booking);

        // 7. Same response shape as every other booking (includes session title and times)
        return new BookingResultDto("CONVERTED", "Waitlist offer accepted and booking created",
                mapToBookingDto(savedBooking));
    }

    // 3. Decline Waitlist Offer
    @Transactional
    public BookingResultDto declineWaitlistOffer(Long waitlistId, Long userId) {
        WaitlistEntry entry = waitlistEntryRepository.findById(waitlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry " + waitlistId + " not found"));

        if (!entry.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only decline your own waitlist offers");
        }

        if (entry.getStatus() != WaitlistStatus.OFFERED) {
            throw new IllegalArgumentException("Waitlist entry is not in OFFERED status");
        }

        entry.setStatus(WaitlistStatus.CANCELLED);
        waitlistEntryRepository.save(entry);

        // Re-trigger offer for next person in line
        BookingResultDto offerResult = offerPlaceToNextPerson(entry.getSession());

        return new BookingResultDto("CANCELLED", "Waitlist offer declined. " + offerResult.getMessage());
    }

    // 4. Create Session (Trainer / Admin)
    @Transactional
    public TrainingSessionResponseDto createSession(TrainingSessionRequestDto dto) {

        // 1. A session must end after it starts
        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // 2. There is only one gym floor, so sessions may not overlap
        if (sessionRepository.existsOverlappingSession(dto.getStartTime(), dto.getEndTime())) {
            throw new IllegalArgumentException("Another session already covers this time");
        }

        // 3. Create and save the session
        TrainingSession session = new TrainingSession(
                dto.getTitle(),
                dto.getInstructor(),
                dto.getSlotType(),
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getMaxCapacity()
        );
        TrainingSession saved = sessionRepository.save(session);
        return mapToSessionDto(saved);
    }

    // 4. Cancel Session (Trainer / Admin)
    @Transactional
    public void cancelSession(Long sessionId) {

        // 1. Load the session, or fail with 404
        TrainingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session " + sessionId + " not found"));

        // 2. Can't cancel twice, or cancel something that already happened
        if (session.isCancelled()) {
            throw new IllegalArgumentException("This session is already cancelled");
        }
        if (session.getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This session has already ended");
        }

        // 3. Mark the session cancelled
        session.setCancelled(true);
        sessionRepository.save(session);

        // 4. Cancel its confirmed bookings. Otherwise the no-show job would mark these
        //    students NO_SHOW for a session that never happened.
        List<Booking> confirmedBookings = bookingRepository.findBySession_SessionIdAndStatusIn(
                sessionId, List.of(BookingStatus.CONFIRMED));
        for (Booking booking : confirmedBookings) {
            booking.cancel();
        }
        bookingRepository.saveAll(confirmedBookings);

        // 5. Close its waitlist, including any open offers. No place is offered: the session is gone.
        List<WaitlistEntry> openEntries = waitlistEntryRepository.findBySession_SessionIdAndStatusIn(
                sessionId, List.of(WaitlistStatus.WAITING, WaitlistStatus.OFFERED));
        for (WaitlistEntry entry : openEntries) {
            entry.setStatus(WaitlistStatus.CANCELLED);
        }
        waitlistEntryRepository.saveAll(openEntries);
    }

    public List<TrainingSessionResponseDto> getUpcomingSessions() {
        return sessionRepository.findUpcomingSessions(LocalDateTime.now())
                .stream()
                .map(this::mapToSessionDto)
                .collect(Collectors.toList());
    }

    // =====================================================================
    // Methods used by Member B's check-in feature (spec section 9.1)
    // DO NOT REMOVE: access/service/CheckInService calls all four.
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

    // =====================================================================
    // Helpers
    // =====================================================================

    private BookingResultDto offerPlaceToNextPerson(TrainingSession session) {
        return new OfferFreedPlaceCommand(session, waitlistEntryRepository, eligibilityChecker).execute();
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
