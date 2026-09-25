// Student ID: [IT25100934] | Table: medical_clearance
package com.example.GymManagement.access.entity;

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

import java.time.LocalDate;

@Entity
@Table(name = "medical_clearance")
@Getter
@Setter
@NoArgsConstructor
public class MedicalClearance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clearance_id")
    private Long clearanceId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "submitted_by_user_id", nullable = false)
    private User submittedBy;

    @ManyToOne
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    @Column(name = "submitted_date", nullable = false)
    private LocalDate submittedDate;

    @Column(name = "reviewed_date")
    private LocalDate reviewedDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ClearanceStatus status = ClearanceStatus.PENDING;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "document_path", nullable = false, length = 255)
    private String documentPath;

    @Column(name = "notes", length = 500)
    private String notes;

    public void approve(User doctor, LocalDate expiry) {
        // 1. Ensure only a PENDING clearance can be approved
        if (this.status != ClearanceStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING medical clearances can be approved.");
        }
        // 2. Validate that the doctor and expiry date are provided
        if (doctor == null || expiry == null) {
            throw new IllegalArgumentException("Doctor and expiry date are required for approval.");
        }
        // 3. Update status, reviewer metadata, and expiry date
        this.status = ClearanceStatus.APPROVED;
        this.reviewedBy = doctor;
        this.reviewedDate = LocalDate.now();
        this.expiryDate = expiry;
        this.rejectionReason = null;
    }

    public void reject(User doctor, String reason) {
        // 1. Ensure only a PENDING clearance can be rejected
        if (this.status != ClearanceStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING medical clearances can be rejected.");
        }
        // 2. Validate that a rejection reason is provided
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A rejection reason is required.");
        }
        // 3. Update status, reviewer metadata, and rejection reason
        this.status = ClearanceStatus.REJECTED;
        this.reviewedBy = doctor;
        this.reviewedDate = LocalDate.now();
        this.rejectionReason = reason.trim();
    }

    public void revoke(String reason) {
        // 1. Ensure only an APPROVED clearance can be revoked
        if (this.status != ClearanceStatus.APPROVED) {
            throw new IllegalArgumentException("Only APPROVED medical clearances can be revoked.");
        }
        // 2. Set status to REVOKED and record the revocation reason
        this.status = ClearanceStatus.REVOKED;
        this.rejectionReason = (reason != null && !reason.isBlank()) ? reason.trim() : "Revoked due to incident";
    }

    public boolean isValid(LocalDate today) {
        // 1. Verify the status is APPROVED and an expiry date exists
        if (this.status != ClearanceStatus.APPROVED || this.expiryDate == null || today == null) {
            return false;
        }
        // 2. Check that the expiry date is today or in the future
        return !this.expiryDate.isBefore(today);
    }
}