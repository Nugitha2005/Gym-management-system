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

    public LocalDateTime getOfferExpiresAt() { return offerExpiresAt; }
    public void setOfferExpiresAt(LocalDateTime offerExpiresAt) { this.offerExpiresAt = offerExpiresAt; }
}