package com.example.GymManagement.booking.controller;

import com.example.GymManagement.booking.dto.BookingResultDto;
import com.example.GymManagement.booking.dto.TrainingSessionResponseDto;
import com.example.GymManagement.booking.service.BookingService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<BookingResultDto> bookSlot(@RequestParam Long sessionId, @RequestParam Long userId) {
        BookingResultDto result = bookingService.bookSlot(sessionId, userId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResultDto> cancelBooking(@PathVariable Long bookingId, @RequestParam Long userId) {
        BookingResultDto result = bookingService.cancelBooking(bookingId, userId);
        return ResponseEntity.ok(result);
    }
}