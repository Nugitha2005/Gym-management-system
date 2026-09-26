package com.example.GymManagement.booking.dto;

import com.example.GymManagement.booking.entity.SlotType;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class TrainingSessionRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Instructor name is required")
    private String instructor;

    @NotNull(message = "Slot type is required")
    private SlotType slotType;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    // 8. Max capacity capped at 10 pre-booked places
    @NotNull(message = "Max capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10, message = "Maximum allowed bookings per session is 10")
    private int maxCapacity;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public SlotType getSlotType() { return slotType; }
    public void setSlotType(SlotType slotType) { this.slotType = slotType; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
}