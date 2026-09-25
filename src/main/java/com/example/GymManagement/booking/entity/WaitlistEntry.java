package com.example.GymManagement.booking.entity;

import com.example.GymManagement.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "waitlist_entry")
public class WaitlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long waitlistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private TrainingSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime requestedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaitlistStatus status = WaitlistStatus.WAITING;

    private LocalDateTime offeredAt;
    private LocalDateTime offerExpiresAt;

    public WaitlistEntry() {}

    public WaitlistEntry(TrainingSession session, User user) {
        this.session = session;
        this.user = user;
        this.status = WaitlistStatus.WAITING;
        this.requestedAt = LocalDateTime.now();
    }

    public Long getWaitlistId() { return waitlistId; }
    public void setWaitlistId(Long waitlistId) { this.waitlistId = waitlistId; }

    public TrainingSession getSession() { return session; }
    public void setSession(TrainingSession session) { this.session = session; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public WaitlistStatus getStatus() { return status; }
    public void setStatus(WaitlistStatus status) { this.status = status; }

    public LocalDateTime getOfferedAt() { return offeredAt; }
    public void setOfferedAt(LocalDateTime offeredAt) { this.offeredAt = offeredAt; }

    public LocalDateTime getOfferExpiresAt() { return offerExpiresAt; }
    public void setOfferExpiresAt(LocalDateTime offerExpiresAt) { this.offerExpiresAt = offerExpiresAt; }
}