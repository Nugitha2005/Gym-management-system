package com.example.GymManagement.access;

import com.example.GymManagement.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.Duration;

// Student ID : IT25100934
// Name : Artigala A.V.N.M
@Entity
@Table(name = "check_in_record")
public class CheckInRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    private LocalDateTime entryTime;
    private LocalDateTime exitTime;

    // Foreign key linked to the User class
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public CheckInRecord() {}

    public void checkIn() {
        this.entryTime = LocalDateTime.now();
        this.exitTime = null;
    }

    public void checkOut() {
        this.exitTime = LocalDateTime.now();
    }

    public long getDuration() {
        if (entryTime != null && exitTime != null) {
            return Duration.between(entryTime, exitTime).toMinutes();
        }
        return 0;
    }

    public boolean isInsideGym() {
        return entryTime != null && exitTime == null;
    }


    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}