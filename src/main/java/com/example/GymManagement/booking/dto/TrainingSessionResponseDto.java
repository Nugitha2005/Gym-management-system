package com.example.GymManagement.booking.dto;

import com.example.GymManagement.booking.entity.SlotType;
import java.time.LocalDateTime;

public class TrainingSessionResponseDto {
    private Long sessionId;
    private String title;
    private String instructor;
    private SlotType slotType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int maxCapacity;
    private int bookedCount;
    private int heldCount;
    private int availablePlaces;
    private boolean cancelled;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

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

    public int getBookedCount() { return bookedCount; }
    public void setBookedCount(int bookedCount) { this.bookedCount = bookedCount; }

    public int getHeldCount() { return heldCount; }
    public void setHeldCount(int heldCount) { this.heldCount = heldCount; }

    public int getAvailablePlaces() { return availablePlaces; }
    public void setAvailablePlaces(int availablePlaces) { this.availablePlaces = availablePlaces; }

    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}