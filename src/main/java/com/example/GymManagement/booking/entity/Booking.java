package com.example.GymManagement.booking.entity;

import com.example.GymManagement.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

// Student ID: [IT25100947] | Table: booking
@Entity
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private TrainingSession session;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime bookingTime = LocalDateTime.now();
    private String status = "BOOKED";

    public Booking() {}

    public Booking(TrainingSession session, User user) {
        this.session = session;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TrainingSession getSession() { return session; }
    public void setSession(TrainingSession session) { this.session = session; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}