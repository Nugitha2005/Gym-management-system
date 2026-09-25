package com.example.GymManagement.booking.service;

import com.example.GymManagement.booking.command.*;
import com.example.GymManagement.booking.dto.*;
import com.example.GymManagement.booking.entity.*;
import com.example.GymManagement.booking.repository.*;
import com.example.GymManagement.user.repository.UserRepository;
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