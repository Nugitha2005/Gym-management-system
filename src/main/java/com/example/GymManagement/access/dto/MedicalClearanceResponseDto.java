package com.example.GymManagement.access.dto;

import com.example.GymManagement.access.entity.ClearanceStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class MedicalClearanceResponseDto {

    private Long clearanceId;
    private Long userId;
    private String studentName;
    private String universityId;
    private ClearanceStatus status;
    private LocalDate submittedDate;
    private LocalDate reviewedDate;
    private LocalDate expiryDate;
    private String rejectionReason;
    private String reviewedByName;
    private boolean valid;
    private String notes;
}
