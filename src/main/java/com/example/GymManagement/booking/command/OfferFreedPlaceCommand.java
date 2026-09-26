package com.example.GymManagement.booking.command;

import com.example.GymManagement.booking.dto.BookingResultDto;
import com.example.GymManagement.booking.dto.WaitlistEntryResponseDto;
import com.example.GymManagement.booking.entity.TrainingSession;
import com.example.GymManagement.booking.entity.WaitlistEntry;
import com.example.GymManagement.booking.entity.WaitlistStatus;
import com.example.GymManagement.booking.repository.WaitlistEntryRepository;
import com.example.GymManagement.booking.service.BookingEligibilityChecker;

import java.time.LocalDateTime;
import java.util.List;

public class OfferFreedPlaceCommand implements BookingCommand {

    private final TrainingSession session;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final BookingEligibilityChecker eligibilityChecker;

    public OfferFreedPlaceCommand(TrainingSession session,
                                  WaitlistEntryRepository waitlistEntryRepository,
                                  BookingEligibilityChecker eligibilityChecker) {
        this.session = session;
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.eligibilityChecker = eligibilityChecker;
    }

    @Override
    public BookingResultDto execute() {
        int available = eligibilityChecker.calculateAvailablePlaces(session);
        if (available <= 0) {
            return new BookingResultDto("NO_ACTION", "No capacity freed");
        }

        List<WaitlistEntry> waitingEntries = waitlistEntryRepository
                .findBySession_SessionIdAndStatusOrderByRequestedAtAsc(session.getSessionId(), WaitlistStatus.WAITING);

        for (WaitlistEntry entry : waitingEntries) {
            if (eligibilityChecker.canUserBook(entry.getUser(), session)) {
                entry.setStatus(WaitlistStatus.OFFERED);
                entry.setOfferedAt(LocalDateTime.now());
                entry.setOfferExpiresAt(LocalDateTime.now().plusHours(24));
                WaitlistEntry saved = waitlistEntryRepository.save(entry);

                WaitlistEntryResponseDto dto = new WaitlistEntryResponseDto();
                dto.setWaitlistId(saved.getWaitlistId());
                dto.setSessionId(session.getSessionId());
                dto.setUserId(saved.getUser().getUserId());
                dto.setStatus(saved.getStatus());
                dto.setRequestedAt(saved.getRequestedAt());
                dto.setOfferExpiresAt(saved.getOfferExpiresAt());

                return new BookingResultDto("OFFERED", "Freed place offered to waitlisted user ID: " + saved.getUser().getUserId(), dto);
            }
        }

        return new BookingResultDto("NO_ACTION", "No eligible waitlisted user found");
    }

    @Override
    public String describe() {
        return "OfferFreedPlaceCommand for session " + session.getSessionId();
    }
}