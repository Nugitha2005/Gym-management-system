// Student ID: [IT25100934] | Table: check_in_record
package com.example.GymManagement.access.entity;

import com.example.GymManagement.booking.entity.Booking;
import com.example.GymManagement.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_in_record")
@Getter
@Setter
@NoArgsConstructor
public class CheckInRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 20)
    private EntryType entryType;

    @Column(name = "auto_closed", nullable = false)
    private boolean autoClosed = false;

    @ManyToOne
    @JoinColumn(name = "checked_in_by_user_id")
    private User checkedInBy;

    public boolean isInside() {
        // 1. A null exitTime indicates the student is still inside the gym
        return this.exitTime == null;
    }

    public void close(LocalDateTime now) {
        // 1. Prevent closing a record that has already been checked out
        if (this.exitTime != null) {
            throw new IllegalArgumentException("This check-in record is already closed.");
        }
        // 2. Stamp the exit time
        this.exitTime = now;
    }

    public void autoClose(LocalDateTime now) {
        // 1. Automatically stamp the exit time and mark the record as system auto-closed
        this.exitTime = now;
        this.autoClosed = true;
    }

    public boolean isStale(LocalDateTime now) {
        // 1. Only open records can be stale
        if (!isInside() || this.entryTime == null || now == null) {
            return false;
        }
        // 2. Check if the entry was more than 4 hours ago or on a date before today
        boolean moreThanFourHoursAgo = Duration.between(this.entryTime, now).toMinutes() > 240;
        boolean entryBeforeToday = this.entryTime.toLocalDate().isBefore(now.toLocalDate());
        return moreThanFourHoursAgo || entryBeforeToday;
    }

    public long getDurationMinutes() {
        // 1. Return 0 if the person is still inside the gym
        if (this.entryTime == null || this.exitTime == null) {
            return 0L;
        }
        // 2. Calculate and return the total minutes between entryTime and exitTime
        return Math.max(0L, Duration.between(this.entryTime, this.exitTime).toMinutes());
    }
}