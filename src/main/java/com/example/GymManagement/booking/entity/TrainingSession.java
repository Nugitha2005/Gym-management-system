package com.example.GymManagement.booking.entity;

import jakarta.persistence.*;
        import java.time.LocalDateTime;

// Student ID: [IT25100947] | Table: training_session
@Entity
@Table(name = "training_session")
public class TrainingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String instructor;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Integer maxCapacity;

    @Column(nullable = false)
    private Integer currentCapacity = 0;

    public TrainingSession() {}

    public TrainingSession(String title, String instructor, LocalDateTime startTime, LocalDateTime endTime, Integer maxCapacity) {
        this.title = title;
        this.instructor = instructor;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxCapacity = maxCapacity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }
    public Integer getCurrentCapacity() { return currentCapacity; }
    public void setCurrentCapacity(Integer currentCapacity) { this.currentCapacity = currentCapacity; }
}