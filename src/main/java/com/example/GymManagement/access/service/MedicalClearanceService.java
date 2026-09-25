package com.example.GymManagement.access.service;

import com.example.GymManagement.access.dto.ClearanceApproveRequestDto;
import com.example.GymManagement.access.dto.ClearanceRejectRequestDto;
import com.example.GymManagement.access.dto.MedicalClearanceResponseDto;
import com.example.GymManagement.access.entity.ClearanceStatus;
import com.example.GymManagement.access.entity.MedicalClearance;
import com.example.GymManagement.access.repository.MedicalClearanceRepository;
import com.example.GymManagement.config.ResourceNotFoundException;
import com.example.GymManagement.user.entity.User;
import com.example.GymManagement.user.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public class MedicalClearanceService {

    private final MedicalClearanceRepository medicalClearanceRepository;
    private final UserRepository userRepository;
    private final DocumentStorageService documentStorageService;

    public MedicalClearanceService(
            MedicalClearanceRepository medicalClearanceRepository,
            UserRepository userRepository,
            DocumentStorageService documentStorageService
    ) {
        this.medicalClearanceRepository = medicalClearanceRepository;
        this.userRepository = userRepository;
        this.documentStorageService = documentStorageService;
    }

    @Transactional
    public MedicalClearanceResponseDto submitClearance(Long userId, MultipartFile file, String notes) {
        // 1. Load the submitting user from the database
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // 2. Check that the student does not already have a PENDING clearance or an active valid clearance
        boolean hasPending = medicalClearanceRepository.existsBySubmittedBy_UserIdAndStatusIn(
                userId,
                List.of(ClearanceStatus.PENDING)
        );
        if (hasPending || hasValidClearance(userId)) {
            throw new IllegalArgumentException("You already have a clearance in progress or approved");
        }

        // 3. Validate optional notes length if provided
        if (notes != null && notes.length() > 500) {
            throw new IllegalArgumentException("Notes cannot exceed 500 characters.");
        }

        // 4. Validate and save the uploaded document under a random filename in uploads/clearances/
        String storedFileName = documentStorageService.storeClearanceFile(file);

        // 5. Create and persist the MedicalClearance entity with status PENDING
        MedicalClearance clearance = new MedicalClearance();
        clearance.setSubmittedBy(student);
        clearance.setSubmittedDate(LocalDate.now());
        clearance.setStatus(ClearanceStatus.PENDING);
        clearance.setDocumentPath(storedFileName);
        clearance.setNotes(notes != null && !notes.isBlank() ? notes.trim() : null);

        MedicalClearance saved = medicalClearanceRepository.save(clearance);
        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<MedicalClearanceResponseDto> getMyClearances(Long userId) {
        // 1. Retrieve all clearances submitted by the logged-in user, newest first
        return medicalClearanceRepository
                .findBySubmittedBy_UserIdOrderBySubmittedDateDescClearanceIdDesc(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MedicalClearanceResponseDto> getPendingClearances() {
        // 1. Retrieve all PENDING clearances ordered from oldest submitted to newest
        return medicalClearanceRepository
                .findByStatusOrderBySubmittedDateAscClearanceIdAsc(ClearanceStatus.PENDING)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public MedicalClearanceResponseDto getClearanceById(Long clearanceId, User actingUser) {
        // 1. Load the requested medical clearance
        MedicalClearance clearance = findClearanceOrThrow(clearanceId);

        // 2. Verify that the acting user is the owner, a DOCTOR, or an ADMIN
        verifyCanViewClearance(clearance, actingUser);

        // 3. Map and return the DTO
        return toResponseDto(clearance);
    }

    @Transactional(readOnly = true)
    public DocumentPayload getClearanceDocument(Long clearanceId, User actingUser) {
        // 1. Load the requested medical clearance
        MedicalClearance clearance = findClearanceOrThrow(clearanceId);

        // 2. Verify that the acting user is the owner, a DOCTOR, or an ADMIN
        verifyCanViewClearance(clearance, actingUser);

        // 3. Load the raw file bytes and media type from DocumentStorageService
        byte[] bytes = documentStorageService.loadDocumentBytes(clearance.getDocumentPath());
        MediaType mediaType = documentStorageService.resolveMediaType(clearance.getDocumentPath());
        return new DocumentPayload(bytes, mediaType, clearance.getDocumentPath());
    }

    @Transactional
    public MedicalClearanceResponseDto approveClearance(
            Long clearanceId,
            Long doctorUserId,
            ClearanceApproveRequestDto requestDto
    ) {
        // 1. Validate that the expiry date is in the future and no more than 1 year from today
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = requestDto.getExpiryDate();
        if (expiryDate == null || !expiryDate.isAfter(today)) {
            throw new IllegalArgumentException("Expiry date must be in the future.");
        }
        if (expiryDate.isAfter(today.plusYears(1))) {
            throw new IllegalArgumentException("Expiry date cannot be more than 1 year from today.");
        }

        // 2. Load the reviewing doctor from the database using the session user id
        User doctor = userRepository.findById(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor user not found."));

        // 3. Load the clearance and call its domain approve method
        MedicalClearance clearance = findClearanceOrThrow(clearanceId);
        clearance.approve(doctor, expiryDate);

        // 4. Save and return the updated DTO
        return toResponseDto(medicalClearanceRepository.save(clearance));
    }

    @Transactional
    public MedicalClearanceResponseDto rejectClearance(
            Long clearanceId,
            Long doctorUserId,
            ClearanceRejectRequestDto requestDto
    ) {
        // 1. Load the reviewing doctor from the database using the session user id
        User doctor = userRepository.findById(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor user not found."));

        // 2. Load the clearance and call its domain reject method
        MedicalClearance clearance = findClearanceOrThrow(clearanceId);
        clearance.reject(doctor, requestDto.getReason());

        // 3. Save and return the updated DTO
        return toResponseDto(medicalClearanceRepository.save(clearance));
    }

    @Transactional(readOnly = true)
    public boolean hasValidClearance(Long userId) {
        // 1. Query for any APPROVED clearance whose expiryDate is today or later
        LocalDate today = LocalDate.now();
        return medicalClearanceRepository
                .findBySubmittedBy_UserIdAndStatusAndExpiryDateGreaterThanEqual(
                        userId,
                        ClearanceStatus.APPROVED,
                        today
                )
                .stream()
                .anyMatch(clearance -> clearance.isValid(today));
    }

    @Transactional
    public void revokeClearance(Long userId, String reason) {
        // 1. Find all currently valid clearances for the user
        LocalDate today = LocalDate.now();
        List<MedicalClearance> validClearances = medicalClearanceRepository
                .findBySubmittedBy_UserIdAndStatusAndExpiryDateGreaterThanEqual(
                        userId,
                        ClearanceStatus.APPROVED,
                        today
                );

        // 2. Revoke each valid clearance; do nothing if the user has none
        for (MedicalClearance clearance : validClearances) {
            if (clearance.isValid(today)) {
                clearance.revoke(reason);
                medicalClearanceRepository.save(clearance);
            }
        }
    }

    private MedicalClearance findClearanceOrThrow(Long clearanceId) {
        // 1. Fetch clearance by id or throw 404 ResourceNotFoundException
        return medicalClearanceRepository.findById(clearanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical clearance not found with id: " + clearanceId));
    }

    private void verifyCanViewClearance(MedicalClearance clearance, User actingUser) {
        // 1. Check if the acting user is the student who submitted the clearance
        boolean isOwner = clearance.getSubmittedBy().getUserId().equals(actingUser.getUserId());

        // 2. Check if the acting user holds the DOCTOR or ADMIN role
        String roleName = actingUser.getRole() != null ? actingUser.getRole().getRoleName() : "";
        boolean isDoctorOrAdmin = "DOCTOR".equalsIgnoreCase(roleName) || "ADMIN".equalsIgnoreCase(roleName);

        // 3. Deny access with 403 if neither condition is satisfied
        if (!isOwner && !isDoctorOrAdmin) {
            throw new AccessDeniedException("You do not have permission to view this medical clearance.");
        }
    }

    private MedicalClearanceResponseDto toResponseDto(MedicalClearance entity) {
        // 1. Map MedicalClearance entity fields to MedicalClearanceResponseDto without exposing User entities
        MedicalClearanceResponseDto dto = new MedicalClearanceResponseDto();
        dto.setClearanceId(entity.getClearanceId());
        dto.setUserId(entity.getSubmittedBy().getUserId());
        dto.setStudentName(entity.getSubmittedBy().getName());
        dto.setUniversityId(entity.getSubmittedBy().getUniversityId());
        dto.setStatus(entity.getStatus());
        dto.setSubmittedDate(entity.getSubmittedDate());
        dto.setReviewedDate(entity.getReviewedDate());
        dto.setExpiryDate(entity.getExpiryDate());
        dto.setRejectionReason(entity.getRejectionReason());
        dto.setReviewedByName(entity.getReviewedBy() != null ? entity.getReviewedBy().getName() : null);
        dto.setValid(entity.isValid(LocalDate.now()));
        dto.setNotes(entity.getNotes());
        return dto;
    }

    public record DocumentPayload(byte[] bytes, MediaType mediaType, String fileName) {
    }
}