package com.example.GymManagement.access;

import com.example.GymManagement.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDate;

//IT25100934 || Artigala A.V.N.M
@Entity
@Table(name="medical_clearance")
public class MedicalClearance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clearanceId;

    private LocalDate submittedDate;
    private LocalDate approvedDate;
    private LocalDate expiryDate;

    private String status;
    private String documentPath;

    @ManyToOne
    @JoinColumn(name = "submitted_by_user_id")
    private User submittedBy;

    @ManyToOne
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    public MedicalClearance() {}

    public void approve(LocalDate expiry){
        this.status = "APPROVED";
        this.approvedDate = LocalDate.now();
        this.expiryDate = expiry;
    }


    public void reject(String reason){
        this.status = "REJECTED";
        this.approvedDate = LocalDate.now();
    }

    public boolean isValid() {
        if ("APPROVED".equals(status) && expiryDate != null) {
            return LocalDate.now().isBefore(expiryDate) || LocalDate.now().isEqual(expiryDate);
        }
        return false;
    }


    public Long getClearanceId() { return clearanceId; }
    public void setClearanceId(Long clearanceId) { this.clearanceId = clearanceId; }

    public LocalDate getSubmittedDate() { return submittedDate; }
    public void setSubmittedDate(LocalDate submittedDate) { this.submittedDate = submittedDate; }

    public LocalDate getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDate approvedDate) { this.approvedDate = approvedDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }

    public User getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(User submittedBy) { this.submittedBy = submittedBy; }

    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }

}