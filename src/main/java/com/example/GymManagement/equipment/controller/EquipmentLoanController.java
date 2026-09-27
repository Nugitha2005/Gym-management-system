package com.example.GymManagement.equipment.controller;

import com.example.GymManagement.equipment.dto.LoanRequestDto;
import com.example.GymManagement.equipment.dto.LoanResponseDto;
import com.example.GymManagement.equipment.entity.EquipmentLoan;
import com.example.GymManagement.equipment.service.EquipmentLoanService;
import com.example.GymManagement.user.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment-loans")
public class EquipmentLoanController {

    private final EquipmentLoanService equipmentLoanService;

    public EquipmentLoanController(
            EquipmentLoanService equipmentLoanService) {

        this.equipmentLoanService =
                equipmentLoanService;
    }

    // =========================================================
    // GET ALL
    // TRAINER / ADMIN
    // =========================================================

    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<LoanResponseDto>>
    getAllLoans() {

        List<LoanResponseDto> response =
                equipmentLoanService
                        .getAllLoans()
                        .stream()
                        .map(equipmentLoanService::toResponseDto)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // GET BY ID
    // TRAINER / ADMIN
    // =========================================================

    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponseDto>
    getLoanById(@PathVariable Long id) {

        EquipmentLoan loan =
                equipmentLoanService.getLoanById(id);

        return ResponseEntity.ok(
                equipmentLoanService.toResponseDto(loan)
        );
    }

    // =========================================================
    // GET USER LOANS
    // TRAINER / ADMIN
    // =========================================================

    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanResponseDto>>
    getUserLoans(
            @PathVariable Long userId) {

        List<LoanResponseDto> response =
                equipmentLoanService
                        .getUserLoans(userId)
                        .stream()
                        .map(equipmentLoanService::toResponseDto)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // GET ACTIVE USER LOANS
    // TRAINER / ADMIN
    // =========================================================

    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<LoanResponseDto>>
    getActiveUserLoans(
            @PathVariable Long userId) {

        List<LoanResponseDto> response =
                equipmentLoanService
                        .getActiveUserLoans(userId)
                        .stream()
                        .map(equipmentLoanService::toResponseDto)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // MY LOANS
    // AUTHENTICATED USER
    // =========================================================

    @GetMapping("/my")
    public ResponseEntity<List<LoanResponseDto>>
    getMyLoans(
            @AuthenticationPrincipal UserPrincipal principal) {

        Long userId =
                principal.getUser().getUserId();

        List<LoanResponseDto> response =
                equipmentLoanService
                        .getUserLoans(userId)
                        .stream()
                        .map(equipmentLoanService::toResponseDto)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // ACTIVE LOANS
    // TRAINER / ADMIN
    // =========================================================

    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @GetMapping("/active")
    public ResponseEntity<List<LoanResponseDto>>
    getActiveLoans() {

        List<LoanResponseDto> response =
                equipmentLoanService
                        .getActiveLoans()
                        .stream()
                        .map(equipmentLoanService::toResponseDto)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // OVERDUE / LATE LOANS
    // TRAINER / ADMIN
    // =========================================================

    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @GetMapping("/overdue")
    public ResponseEntity<List<LoanResponseDto>>
    getOverdueLoans() {

        List<LoanResponseDto> response =
                equipmentLoanService
                        .getOverdueLoans()
                        .stream()
                        .map(equipmentLoanService::toResponseDto)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // BORROW / ISSUE EQUIPMENT
    // TRAINER / ADMIN
    // =========================================================

    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<LoanResponseDto>
    borrowEquipment(
            @Valid @RequestBody LoanRequestDto request) {

        EquipmentLoan loan =
                equipmentLoanService.borrowEquipment(
                        request.getEquipmentId(),
                        request.getUserId(),
                        request.getDueTime()
                );

        return ResponseEntity.ok(
                equipmentLoanService.toResponseDto(loan)
        );
    }

    // =========================================================
    // RETURN EQUIPMENT
    // TRAINER / ADMIN
    // =========================================================

    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @PutMapping("/{id}/return")
    public ResponseEntity<LoanResponseDto>
    returnEquipment(
            @PathVariable Long id) {

        EquipmentLoan loan =
                equipmentLoanService.returnEquipment(id);

        return ResponseEntity.ok(
                equipmentLoanService.toResponseDto(loan)
        );
    }
}