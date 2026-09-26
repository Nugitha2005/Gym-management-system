package com.example.GymManagement.equipment.repository;

import com.example.GymManagement.equipment.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentRepository
        extends JpaRepository<Equipment, Long> {

    List<Equipment> findByAvailable(boolean available);

    List<Equipment> findByNameContainingIgnoreCase(String name);
}