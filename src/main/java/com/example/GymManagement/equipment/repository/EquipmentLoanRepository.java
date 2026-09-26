package com.example.GymManagement.equipment.repository;

import com.example.GymManagement.equipment.entity.Equipment;
import com.example.GymManagement.equipment.entity.EquipmentLoan;
import com.example.GymManagement.equipment.entity.LoanStatus;
import com.example.GymManagement.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentLoanRepository
        extends JpaRepository<EquipmentLoan, Long> {

    List<EquipmentLoan> findByStatus(LoanStatus status);

    List<EquipmentLoan> findByStatusIn(
            List<LoanStatus> statuses
    );

    List<EquipmentLoan> findByEquipment(
            Equipment equipment
    );

    List<EquipmentLoan> findByUser(
            User user
    );

    List<EquipmentLoan> findByEquipmentAndStatus(
            Equipment equipment,
            LoanStatus status
    );

    List<EquipmentLoan> findByUserAndStatus(
            User user,
            LoanStatus status
    );

    List<EquipmentLoan> findByUserAndStatusIn(
            User user,
            List<LoanStatus> statuses
    );
}