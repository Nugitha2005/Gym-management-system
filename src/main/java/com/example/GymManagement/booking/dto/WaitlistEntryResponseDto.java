package com.example.GymManagement.booking.dto;

import com.example.GymManagement.booking.entity.WaitlistStatus;
import java.time.LocalDateTime;

public class WaitlistEntryResponseDto {
    private Long waitlistId;
    private Long sessionId;
    private Long userId;
    private WaitlistStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime offerExpiresAt;

    // Which session this entry is for, so the student can recognise it in "my waitlist"
    private String sessionTitle;
    private LocalDateTime sessionStartTime;

    public Long getWaitlistId() { return waitlistId; }
    public void setWaitlistId(Long waitlistId) { this.waitlistId = waitlistId; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public WaitlistStatus getStatus() { return status; }
    public void setStatus(WaitlistStatus status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public String getSessionTitle() { return sessionTitle; }
    public void setSessionTitle(String sessionTitle) { this.sessionTitle = sessionTitle; }

    public LocalDateTime getSessionStartTime() { return sessionStartTime; }
    public void setSessionStartTime(LocalDateTime sessionStartTime) { this.sessionStartTime = sessionStartTime; }

    public LocalDateTime getOfferExpiresAt() { return offerExpiresAt; }
    public void setOfferExpiresAt(LocalDateTime offerExpiresAt) { this.offerExpiresAt = offerExpiresAt; }
}