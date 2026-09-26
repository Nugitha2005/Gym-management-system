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
    @Transactional
    public BookingResultDto acceptWaitlistOffer(Long waitlistId, Long userId) {
        WaitlistEntry entry = waitlistEntryRepository.findById(waitlistId)
                .orElseThrow(() -> new ResourceNotFoundException("WaitlistEntry", "waitlistId", waitlistId));

        if (!entry.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only accept your own waitlist offers");
        }

        if (entry.getStatus() != WaitlistStatus.OFFERED) {
            throw new IllegalArgumentException("Waitlist entry is not in OFFERED status");
        }

        if (entry.getOfferExpiresAt().isBefore(LocalDateTime.now())) {
            entry.setStatus(WaitlistStatus.EXPIRED);
            waitlistEntryRepository.save(entry);
            throw new IllegalArgumentException("Waitlist offer has expired");
        }

        entry.setStatus(WaitlistStatus.CONVERTED);
        waitlistEntryRepository.save(entry);

        Booking booking = new Booking(entry.getSession(), entry.getUser());
        Booking savedBooking = bookingRepository.save(booking);

        BookingResponseDto dto = new BookingResponseDto();
        dto.setBookingId(savedBooking.getBookingId());
        dto.setSessionId(savedBooking.getSession().getSessionId());
        dto.setUserId(savedBooking.getUser().getUserId());
        dto.setStatus(savedBooking.getStatus());
        dto.setBookingTime(savedBooking.getBookingTime());

        return new BookingResultDto("CONVERTED", "Waitlist offer accepted and booking created", dto);
    }

    // 3. Decline Waitlist Offer
    @Transactional
    public BookingResultDto declineWaitlistOffer(Long waitlistId, Long userId) {
        WaitlistEntry entry = waitlistEntryRepository.findById(waitlistId)
                .orElseThrow(() -> new ResourceNotFoundException("WaitlistEntry", "waitlistId", waitlistId));

        if (!entry.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only decline your own waitlist offers");
        }

        if (entry.getStatus() != WaitlistStatus.OFFERED) {
            throw new IllegalArgumentException("Waitlist entry is not in OFFERED status");
        }

        entry.setStatus(WaitlistStatus.CANCELLED);
        waitlistEntryRepository.save(entry);

        // Re-trigger offer for next person in line
        BookingResultDto offerResult = new OfferFreedPlaceCommand(
                entry.getSession(), waitlistEntryRepository, eligibilityChecker
        ).execute();

        return new BookingResultDto("CANCELLED", "Waitlist offer declined. " + offerResult.getMessage());
    }

    // 4. Create Session (Trainer / Admin)
    @Transactional
    public TrainingSessionResponseDto createSession(TrainingSessionRequestDto dto) {
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
        TrainingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingSession", "sessionId", sessionId));
        session.setCancelled(true);
        sessionRepository.save(session);
    }

    public List<TrainingSessionResponseDto> getUpcomingSessions() {
        return sessionRepository.findUpcomingSessions(LocalDateTime.now())
                .stream()
                .map(this::mapToSessionDto)
                .collect(Collectors.toList());
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