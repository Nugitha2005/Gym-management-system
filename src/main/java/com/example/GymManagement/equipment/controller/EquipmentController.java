package com.example.GymManagement.equipment.controller;

import com.example.GymManagement.equipment.dto.EquipmentResponseDto;
import com.example.GymManagement.equipment.entity.Equipment;
import com.example.GymManagement.equipment.service.EquipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.GymManagement.equipment.dto.EquipmentRequestDto;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@CrossOrigin(origins = "http://localhost:5173")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(
            EquipmentService equipmentService) {

        this.equipmentService = equipmentService;
    }


    // GET /api/equipment
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


    // GET /api/equipment/available
    @GetMapping("/available")
    public ResponseEntity<List<EquipmentResponseDto>>
    getAvailableEquipment() {

        List<EquipmentResponseDto> response =
                equipmentService.getAvailableEquipment()
                        .stream()
                        .map(equipmentService::toResponseDto)
                        .toList();

        return ResponseEntity.ok(response);
    }


    // GET /api/equipment/{id}
    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponseDto>
    getEquipmentById(@PathVariable Long id) {

        Equipment equipment =
                equipmentService.getEquipmentById(id);

        return ResponseEntity.ok(
                equipmentService.toResponseDto(equipment)
        );
    }


    // GET /api/equipment/search?name=basket
    @GetMapping("/search")
    public ResponseEntity<List<EquipmentResponseDto>>
    searchEquipment(@RequestParam String name) {

        List<EquipmentResponseDto> response =
                equipmentService.searchEquipment(name)
                        .stream()
                        .map(equipmentService::toResponseDto)
                        .toList();

        return ResponseEntity.ok(response);
    }


    // POST /api/equipment
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


    // PUT /api/equipment/{id}
    @PutMapping("/{id}")
    public ResponseEntity<EquipmentResponseDto>
    updateEquipment(
            @PathVariable Long id,
            @RequestBody Equipment equipment) {

        Equipment updated =
                equipmentService.updateEquipment(
                        id,
                        equipment
                );

        return ResponseEntity.ok(
                equipmentService.toResponseDto(updated)
        );
    }


    // DELETE /api/equipment/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteEquipment(@PathVariable Long id) {

        equipmentService.deleteEquipment(id);

        return ResponseEntity.noContent().build();
    }
}