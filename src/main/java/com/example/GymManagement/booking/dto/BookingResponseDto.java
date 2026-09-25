package com.example.GymManagement.booking.dto;

import com.example.GymManagement.booking.entity.BookingStatus;
import java.time.LocalDateTime;

public class BookingResponseDto {
    private Long bookingId;
    private Long sessionId;
    private Long userId;
    private BookingStatus status;
    private LocalDateTime bookingTime;

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }
}