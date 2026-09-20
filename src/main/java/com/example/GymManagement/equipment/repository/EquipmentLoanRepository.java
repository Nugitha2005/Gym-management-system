package com.example.GymManagement.equipment.repository;

import com.example.GymManagement.equipment.entity.Equipment;
import com.example.GymManagement.equipment.entity.EquipmentLoan;
import com.example.GymManagement.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentLoanRepository
        extends JpaRepository<EquipmentLoan, Long> {

    List<EquipmentLoan> findByStatus(String status);

    List<EquipmentLoan> findByEquipment(Equipment equipment);

    List<EquipmentLoan> findByUser(User user);

    List<EquipmentLoan> findByEquipmentAndStatus(
            Equipment equipment,
            String status
    );

    List<EquipmentLoan> findByUserAndStatus(
            User user,
            String status
    );
}