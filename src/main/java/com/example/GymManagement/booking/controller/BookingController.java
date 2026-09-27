package com.example.GymManagement.booking.controller;

import com.example.GymManagement.booking.dto.BookingResultDto;
import com.example.GymManagement.booking.dto.TrainingSessionRequestDto;
import com.example.GymManagement.booking.dto.TrainingSessionResponseDto;
import com.example.GymManagement.booking.service.BookingService;
import com.example.GymManagement.user.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/sessions/upcoming")
    public ResponseEntity<List<TrainingSessionResponseDto>> getUpcomingSessions() {
        return ResponseEntity.ok(bookingService.getUpcomingSessions());
    }

    @PostMapping("/book")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResultDto> bookSlot(@RequestParam Long sessionId,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        BookingResultDto result = bookingService.bookSlot(sessionId, principal.getUser().getUserId());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{bookingId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResultDto> cancelBooking(@PathVariable Long bookingId,
                                                          @AuthenticationPrincipal UserPrincipal principal) {
        BookingResultDto result = bookingService.cancelBooking(bookingId, principal.getUser().getUserId());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/waitlist/{waitlistId}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResultDto> acceptWaitlistOffer(@PathVariable Long waitlistId,
                                                                @AuthenticationPrincipal UserPrincipal principal) {
        BookingResultDto result = bookingService.acceptWaitlistOffer(waitlistId, principal.getUser().getUserId());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/waitlist/{waitlistId}/decline")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResultDto> declineWaitlistOffer(@PathVariable Long waitlistId,
                                                                 @AuthenticationPrincipal UserPrincipal principal) {
        BookingResultDto result = bookingService.declineWaitlistOffer(waitlistId, principal.getUser().getUserId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sessions")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<TrainingSessionResponseDto> createSession(@Valid @RequestBody TrainingSessionRequestDto dto) {
        return ResponseEntity.ok(bookingService.createSession(dto));
    }

    @DeleteMapping("/sessions/{sessionId}/cancel")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<Void> cancelSession(@PathVariable Long sessionId) {
        bookingService.cancelSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}