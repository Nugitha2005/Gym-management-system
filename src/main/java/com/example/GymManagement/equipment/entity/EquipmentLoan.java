package com.example.GymManagement.equipment.entity;

import com.example.GymManagement.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_loan")
public class EquipmentLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    private LocalDateTime receivedTime;

    private LocalDateTime dueTime;

    private LocalDateTime returnedTime;

    private String status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    // Default constructor required by JPA
    public EquipmentLoan() {
    }


    // Constructor
    public EquipmentLoan(
            Equipment equipment,
            User user,
            LocalDateTime dueTime) {

        this.equipment = equipment;
        this.user = user;
        this.dueTime = dueTime;
    }


    // Issue equipment
    public void issueItem() {

        if (!equipment.isAvailable()) {
            throw new IllegalStateException(
                    "Equipment is currently unavailable."
            );
        }

        this.receivedTime = LocalDateTime.now();
        this.status = "BORROWED";

        equipment.markUnavailable();
    }


    // Return equipment
    public void returnItem() {

        if ("RETURNED".equals(this.status)) {
            throw new IllegalStateException(
                    "Equipment has already been returned."
            );
        }

        this.returnedTime = LocalDateTime.now();
        this.status = "RETURNED";

        equipment.markAvailable();
    }


    // Check whether loan is overdue
    public boolean isOverdue() {

        if (returnedTime != null) {
            return false;
        }

        return dueTime != null &&
                LocalDateTime.now().isAfter(dueTime);
    }


    // Getters and setters

    public Long getLoanId() {
        return loanId;
    }

    public LocalDateTime getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(LocalDateTime receivedTime) {
        this.receivedTime = receivedTime;
    }

    public LocalDateTime getDueTime() {
        return dueTime;
    }

    public void setDueTime(LocalDateTime dueTime) {
        this.dueTime = dueTime;
    }

    public LocalDateTime getReturnedTime() {
        return returnedTime;
    }

    public void setReturnedTime(LocalDateTime returnedTime) {
        this.returnedTime = returnedTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}