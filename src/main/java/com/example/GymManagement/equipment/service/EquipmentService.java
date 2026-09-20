package com.example.GymManagement.equipment.service;

import com.example.GymManagement.equipment.entity.Equipment;
import com.example.GymManagement.equipment.repository.EquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    // Get all equipment
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    // Get one equipment item
    public Equipment getEquipmentById(Long equipmentId) {

        return equipmentRepository.findById(equipmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Equipment not found with ID: "
                                        + equipmentId
                        )
                );
    }

    // Get only available equipment
    public List<Equipment> getAvailableEquipment() {
        return equipmentRepository.findByAvailable(true);
    }

    // Search equipment by name
    public List<Equipment> searchEquipment(String name) {
        return equipmentRepository
                .findByNameContainingIgnoreCase(name);
    }

    // Add equipment
    public Equipment createEquipment(Equipment equipment) {

        equipment.markAvailable();

        return equipmentRepository.save(equipment);
    }

    // Update equipment
    public Equipment updateEquipment(
            Long equipmentId,
            Equipment updatedEquipment) {

        Equipment existingEquipment =
                getEquipmentById(equipmentId);

        existingEquipment.setName(
                updatedEquipment.getName()
        );

        existingEquipment.setItemCondition(
                updatedEquipment.getItemCondition()
        );

        if (updatedEquipment.isAvailable()) {
            existingEquipment.markAvailable();
        } else {
            existingEquipment.markUnavailable();
        }

        return equipmentRepository.save(existingEquipment);
    }

    // Delete equipment
    public void deleteEquipment(Long equipmentId) {

        Equipment equipment =
                getEquipmentById(equipmentId);

        equipmentRepository.delete(equipment);
    }

    // Mark unavailable
    public Equipment markUnavailable(Long equipmentId) {

        Equipment equipment =
                getEquipmentById(equipmentId);

        equipment.markUnavailable();

        return equipmentRepository.save(equipment);
    }

    // Mark available
    public Equipment markAvailable(Long equipmentId) {

        Equipment equipment =
                getEquipmentById(equipmentId);

        equipment.markAvailable();

        return equipmentRepository.save(equipment);
    }
}