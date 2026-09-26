package com.example.GymManagement.booking.service;

import com.example.GymManagement.booking.command.OfferFreedPlaceCommand;
import com.example.GymManagement.booking.entity.Booking;
import com.example.GymManagement.booking.entity.BookingStatus;
import com.example.GymManagement.booking.entity.WaitlistEntry;
import com.example.GymManagement.booking.entity.WaitlistStatus;
import com.example.GymManagement.booking.repository.BookingRepository;
import com.example.GymManagement.booking.repository.WaitlistEntryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingScheduledTasks {

    private final WaitlistEntryRepository waitlistEntryRepository;
    private final BookingRepository bookingRepository;
    private final BookingEligibilityChecker eligibilityChecker;

    public BookingScheduledTasks(WaitlistEntryRepository waitlistEntryRepository,
                                 BookingRepository bookingRepository,
                                 BookingEligibilityChecker eligibilityChecker) {
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.bookingRepository = bookingRepository;
        this.eligibilityChecker = eligibilityChecker;
    }

    // 3. Scheduled job to expire old waitlist offers every 15 minutes
    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void processExpiredOffers() {
        List<WaitlistEntry> expiredOffers = waitlistEntryRepository
                .findByStatusAndOfferExpiresAtBefore(WaitlistStatus.OFFERED, LocalDateTime.now());

        for (WaitlistEntry entry : expiredOffers) {
            entry.setStatus(WaitlistStatus.EXPIRED);
            waitlistEntryRepository.save(entry);

            // Automatically offer freed place to next person in queue
            new OfferFreedPlaceCommand(entry.getSession(), waitlistEntryRepository, eligibilityChecker).execute();
        }
    }

    // 7. Scheduled job to mark un-attended ended bookings as NO_SHOW every hour
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void processNoShows() {
        List<Booking> endedConfirmedBookings = bookingRepository
                .findByStatusAndSession_EndTimeBefore(BookingStatus.CONFIRMED, LocalDateTime.now());

        for (Booking booking : endedConfirmedBookings) {
            booking.setStatus(BookingStatus.NO_SHOW);
            bookingRepository.save(booking);
        }
    }
}