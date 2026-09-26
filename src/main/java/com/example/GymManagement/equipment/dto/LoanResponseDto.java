package com.example.GymManagement.equipment.dto;

import java.time.LocalDateTime;

public class LoanResponseDto {

    private Long loanId;

    private Long equipmentId;
    private String equipmentName;

    private Long userId;
    private String universityId;
    private String userName;

    private LocalDateTime receivedTime;
    private LocalDateTime dueTime;
    private LocalDateTime returnedTime;

    private String status;
    private boolean overdue;

    public LoanResponseDto() {
    }

    public LoanResponseDto(
            Long loanId,
            Long equipmentId,
            String equipmentName,
            Long userId,
            String universityId,
            String userName,
            LocalDateTime receivedTime,
            LocalDateTime dueTime,
            LocalDateTime returnedTime,
            String status,
            boolean overdue) {

        this.loanId = loanId;
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;

        this.userId = userId;
        this.universityId = universityId;
        this.userName = userName;

        this.receivedTime = receivedTime;
        this.dueTime = dueTime;
        this.returnedTime = returnedTime;

        this.status = status;
        this.overdue = overdue;
    }

    public Long getLoanId() {
        return loanId;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUniversityId() {
        return universityId;
    }

    public String getUserName() {
        return userName;
    }

    public LocalDateTime getReceivedTime() {
        return receivedTime;
    }

    public LocalDateTime getDueTime() {
        return dueTime;
    }

    public LocalDateTime getReturnedTime() {
        return returnedTime;
    }

    public String getStatus() {
        return status;
    }

    public boolean isOverdue() {
        return overdue;
    }
}