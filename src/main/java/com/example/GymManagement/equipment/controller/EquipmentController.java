package com.example.GymManagement.equipment.controller;

import com.example.GymManagement.equipment.dto.EquipmentRequestDto;
import com.example.GymManagement.equipment.dto.EquipmentResponseDto;
import com.example.GymManagement.equipment.entity.Equipment;
import com.example.GymManagement.equipment.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(
            EquipmentService equipmentService) {

        this.equipmentService = equipmentService;
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @GetMapping
    public ResponseEntity<List<EquipmentResponseDto>>
    getAllEquipment() {

        List<EquipmentResponseDto> response =
                equipmentService.getAllEquipment()
                        .stream()
                        .map(equipmentService::toResponseDto)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // GET AVAILABLE
    // =========================================================

    @GetMapping("/available")
    public ResponseEntity<List<EquipmentResponseDto>>
    getAvailableEquipment() {

        List<EquipmentResponseDto> response =
                equipmentService
                        .getAvailableEquipment()
                        .stream()
                        .map(equipmentService::toResponseDto)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponseDto>
    getEquipmentById(@PathVariable Long id) {

        Equipment equipment =
                equipmentService.getEquipmentById(id);

        return ResponseEntity.ok(
                equipmentService.toResponseDto(equipment)
        );
    }

    // =========================================================
    // SEARCH
    // =========================================================

    @GetMapping("/search")
    public ResponseEntity<List<EquipmentResponseDto>>
    searchEquipment(
            @RequestParam String name) {

        List<EquipmentResponseDto> response =
                equipmentService
                        .searchEquipment(name)
                        .stream()
                        .map(equipmentService::toResponseDto)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // CREATE
    // TRAINER / ADMIN ONLY
    // =========================================================

    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<EquipmentResponseDto>
    createEquipment(
            @Valid @RequestBody EquipmentRequestDto request) {

        Equipment saved =
                equipmentService.createEquipment(request);

        return ResponseEntity.ok(
                equipmentService.toResponseDto(saved)
        );
    }

    // =========================================================
    // UPDATE
    // TRAINER / ADMIN ONLY
    // =========================================================

    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EquipmentResponseDto>
    updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentRequestDto request) {

        Equipment updated =
                equipmentService.updateEquipment(
                        id,
                        request
                );

        return ResponseEntity.ok(
                equipmentService.toResponseDto(updated)
        );
    }

    // =========================================================
    // DELETE
    // TRAINER / ADMIN ONLY
    // =========================================================

    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteEquipment(@PathVariable Long id) {

        equipmentService.deleteEquipment(id);

        return ResponseEntity.noContent().build();
    }
}