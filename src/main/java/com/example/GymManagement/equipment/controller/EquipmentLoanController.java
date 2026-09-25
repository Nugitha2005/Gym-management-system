package com.example.GymManagement.equipment.controller;

import com.example.GymManagement.equipment.dto.LoanRequestDto;
import com.example.GymManagement.equipment.dto.LoanResponseDto;
import com.example.GymManagement.equipment.entity.EquipmentLoan;
import com.example.GymManagement.equipment.service.EquipmentLoanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment-loans")
@CrossOrigin(origins = "http://localhost:5173")
public class EquipmentLoanController {

    private final EquipmentLoanService equipmentLoanService;

    public EquipmentLoanController(
            EquipmentLoanService equipmentLoanService) {

        this.equipmentLoanService = equipmentLoanService;
    }


    // GET /api/equipment-loans
    @GetMapping
    public ResponseEntity<List<LoanResponseDto>>
    getAllLoans() {

        List<LoanResponseDto> response =
                equipmentLoanService.getAllLoans()
                        .stream()
                        .map(equipmentLoanService::toResponseDto)
                        .toList();

        return ResponseEntity.ok(response);
    }


    // GET /api/equipment-loans/{id}
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponseDto>
    getLoanById(@PathVariable Long id) {

        EquipmentLoan loan =
                equipmentLoanService.getLoanById(id);

        return ResponseEntity.ok(
                equipmentLoanService.toResponseDto(loan)
        );
    }


    // GET /api/equipment-loans/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanResponseDto>>
    getUserLoans(@PathVariable Long userId) {

        List<LoanResponseDto> response =
                equipmentLoanService.getUserLoans(userId)
                        .stream()
                        .map(equipmentLoanService::toResponseDto)
                        .toList();

        return ResponseEntity.ok(response);
    }


    // GET /api/equipment-loans/user/{userId}/active
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<LoanResponseDto>>
    getActiveUserLoans(@PathVariable Long userId) {

        List<LoanResponseDto> response =
                equipmentLoanService
                        .getActiveUserLoans(userId)
                        .stream()
                        .map(equipmentLoanService::toResponseDto)
                        .toList();

        return ResponseEntity.ok(response);
    }


    // GET /api/equipment-loans/active
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


    // GET /api/equipment-loans/overdue
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


    // POST /api/equipment-loans
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


    // PUT /api/equipment-loans/{id}/return
    @PutMapping("/{id}/return")
    public ResponseEntity<LoanResponseDto>
    returnEquipment(@PathVariable Long id) {

        EquipmentLoan loan =
                equipmentLoanService.returnEquipment(id);

        return ResponseEntity.ok(
                equipmentLoanService.toResponseDto(loan)
        );
    }
}