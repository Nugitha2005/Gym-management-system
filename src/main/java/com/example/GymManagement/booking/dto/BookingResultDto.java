package com.example.GymManagement.booking.dto;

public class BookingResultDto {
    private String outcome;
    private String message;
    private BookingResponseDto booking;
    private WaitlistEntryResponseDto waitlistEntry;

    public BookingResultDto() {}

    public BookingResultDto(String outcome, String message) {
        this.outcome = outcome;
        this.message = message;
    }

    public BookingResultDto(String outcome, String message, BookingResponseDto booking) {
        this.outcome = outcome;
        this.message = message;
        this.booking = booking;
    }

    public BookingResultDto(String outcome, String message, WaitlistEntryResponseDto waitlistEntry) {
        this.outcome = outcome;
        this.message = message;
        this.waitlistEntry = waitlistEntry;
    }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public BookingResponseDto getBooking() { return booking; }
    public void setBooking(BookingResponseDto booking) { this.booking = booking; }

    public WaitlistEntryResponseDto getWaitlistEntry() { return waitlistEntry; }
    public void setWaitlistEntry(WaitlistEntryResponseDto waitlistEntry) { this.waitlistEntry = waitlistEntry; }
}