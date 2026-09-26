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

    @Column(nullable = false)
    private LocalDateTime receivedTime;

    @Column(nullable = false)
    private LocalDateTime dueTime;

    private LocalDateTime returnedTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public EquipmentLoan() {
    }

    public EquipmentLoan(
            Equipment equipment,
            User user,
            LocalDateTime dueTime) {

        this.equipment = equipment;
        this.user = user;
        this.dueTime = dueTime;
        this.status = LoanStatus.BORROWED;
    }

    /**
     * Issues the equipment to the user.
     */
    public void issueItem() {

        if (!equipment.isAvailable()) {
            throw new IllegalArgumentException(
                    "Equipment is currently unavailable."
            );
        }

        this.receivedTime = LocalDateTime.now();
        this.status = LoanStatus.BORROWED;

        equipment.markUnavailable();
    }

    /**
     * Returns the equipment.
     */
    public void returnItem() {

        if (this.status == LoanStatus.RETURNED) {
            throw new IllegalArgumentException(
                    "Equipment has already been returned."
            );
        }

        this.returnedTime = LocalDateTime.now();
        this.status = LoanStatus.RETURNED;

        equipment.markAvailable();
    }

    /**
     * Checks whether the loan is currently overdue.
     */
    public boolean isOverdue() {

        if (returnedTime != null) {
            return false;
        }

        return dueTime != null
                && LocalDateTime.now().isAfter(dueTime);
    }

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

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
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