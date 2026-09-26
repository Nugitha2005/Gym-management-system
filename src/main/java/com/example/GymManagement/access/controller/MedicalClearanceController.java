package com.example.GymManagement.access.controller;

import com.example.GymManagement.access.dto.ClearanceApproveRequestDto;
import com.example.GymManagement.access.dto.ClearanceRejectRequestDto;
import com.example.GymManagement.access.dto.MedicalClearanceResponseDto;
import com.example.GymManagement.access.service.MedicalClearanceService;
import com.example.GymManagement.user.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/clearances")
public class MedicalClearanceController {

    private final MedicalClearanceService medicalClearanceService;

    public MedicalClearanceController(MedicalClearanceService medicalClearanceService) {
        this.medicalClearanceService = medicalClearanceService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MedicalClearanceResponseDto> submitClearance(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "notes", required = false) String notes
    ) {
        // 1. Extract the logged-in user's id from the session principal
        Long userId = principal.getUser().getUserId();

        // 2. Submit the medical clearance form and return 201 Created
        MedicalClearanceResponseDto created = medicalClearanceService.submitClearance(userId, file, notes);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MedicalClearanceResponseDto>> getMyClearances(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // 1. Return the logged-in user's clearances ordered newest first
        Long userId = principal.getUser().getUserId();
        return ResponseEntity.ok(medicalClearanceService.getMyClearances(userId));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<MedicalClearanceResponseDto>> getPendingClearances() {
        // 1. Return all PENDING clearances ordered oldest first for the doctor
        return ResponseEntity.ok(medicalClearanceService.getPendingClearances());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MedicalClearanceResponseDto> getClearanceById(
            @PathVariable("id") Long clearanceId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // 1. Verify permission in the service (owner, DOCTOR, or ADMIN) and return the clearance DTO
        return ResponseEntity.ok(medicalClearanceService.getClearanceById(clearanceId, principal.getUser()));
    }

    @GetMapping("/{id}/document")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getClearanceDocument(
            @PathVariable("id") Long clearanceId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // 1. Load the document bytes and content type if the caller is the owner, DOCTOR, or ADMIN
        MedicalClearanceService.DocumentPayload payload =
                medicalClearanceService.getClearanceDocument(clearanceId, principal.getUser());

        // 2. Return the raw file bytes with the appropriate Content-Type header
        return ResponseEntity.ok()
                .contentType(payload.mediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + payload.fileName() + "\"")
                .body(payload.bytes());
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalClearanceResponseDto> approveClearance(
            @PathVariable("id") Long clearanceId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ClearanceApproveRequestDto requestDto
    ) {
        // 1. Approve the clearance using the logged-in doctor's userId
        Long doctorId = principal.getUser().getUserId();
        return ResponseEntity.ok(medicalClearanceService.approveClearance(clearanceId, doctorId, requestDto));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalClearanceResponseDto> rejectClearance(
            @PathVariable("id") Long clearanceId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ClearanceRejectRequestDto requestDto
    ) {
        // 1. Reject the clearance with the doctor's reason and logged-in userId
        Long doctorId = principal.getUser().getUserId();
        return ResponseEntity.ok(medicalClearanceService.rejectClearance(clearanceId, doctorId, requestDto));
    }
}