package com.example.GymManagement.equipment.service;

import com.example.GymManagement.equipment.entity.Equipment;
import com.example.GymManagement.equipment.entity.EquipmentLoan;
import com.example.GymManagement.equipment.repository.EquipmentLoanRepository;
import com.example.GymManagement.equipment.repository.EquipmentRepository;
import com.example.GymManagement.user.entity.User;
import com.example.GymManagement.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.GymManagement.equipment.dto.LoanResponseDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EquipmentLoanService {

    private final EquipmentLoanRepository equipmentLoanRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    public EquipmentLoanService(
            EquipmentLoanRepository equipmentLoanRepository,
            EquipmentRepository equipmentRepository,
            UserRepository userRepository) {

        this.equipmentLoanRepository = equipmentLoanRepository;
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
    }


    // =========================================================
    // BORROW EQUIPMENT
    // =========================================================

    @Transactional
    public EquipmentLoan borrowEquipment(
            Long equipmentId,
            Long userId,
            LocalDateTime dueTime) {

        // Find equipment
        Equipment equipment =
                equipmentRepository.findById(equipmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Equipment not found."
                                )
                        );

        // Check availability
        if (!equipment.isAvailable()) {
            throw new IllegalStateException(
                    "Equipment is currently unavailable."
            );
        }

        // Find user
        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found."
                                )
                        );

        // Create loan
        EquipmentLoan loan =
                new EquipmentLoan(
                        equipment,
                        user,
                        dueTime
                );

        // Issue equipment
        loan.issueItem();

        // Save equipment
        equipmentRepository.save(equipment);

        // Save loan
        return equipmentLoanRepository.save(loan);
    }


    // =========================================================
    // RETURN EQUIPMENT
    // =========================================================

    @Transactional
    public EquipmentLoan returnEquipment(Long loanId) {

        EquipmentLoan loan =
                equipmentLoanRepository.findById(loanId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Equipment loan not found."
                                )
                        );

        loan.returnItem();

        equipmentRepository.save(
                loan.getEquipment()
        );

        return equipmentLoanRepository.save(loan);
    }


    // =========================================================
    // GET ALL LOANS
    // =========================================================

    public List<EquipmentLoan> getAllLoans() {

        return equipmentLoanRepository.findAll();
    }


    // =========================================================
    // GET LOAN BY ID
    // =========================================================

    public EquipmentLoan getLoanById(Long loanId) {

        return equipmentLoanRepository.findById(loanId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Equipment loan not found."
                        )
                );
    }


    // =========================================================
    // GET USER LOANS
    // =========================================================

    public List<EquipmentLoan> getUserLoans(Long userId) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found."
                                )
                        );

        return equipmentLoanRepository.findByUser(user);
    }


    // =========================================================
    // GET ACTIVE USER LOANS
    // =========================================================

    public List<EquipmentLoan> getActiveUserLoans(
            Long userId) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found."
                                )
                        );

        return equipmentLoanRepository
                .findByUserAndStatus(
                        user,
                        "BORROWED"
                );
    }


    // =========================================================
    // GET ALL ACTIVE LOANS
    // =========================================================

    public List<EquipmentLoan> getActiveLoans() {

        return equipmentLoanRepository
                .findByStatus("BORROWED");
    }


    // =========================================================
    // GET OVERDUE LOANS
    // =========================================================

    public List<EquipmentLoan> getOverdueLoans() {

        List<EquipmentLoan> activeLoans =
                equipmentLoanRepository
                        .findByStatus("BORROWED");

        return activeLoans.stream()
                .filter(EquipmentLoan::isOverdue)
                .toList();
    }

    public LoanResponseDto toResponseDto(
            EquipmentLoan loan) {

        return new LoanResponseDto(
                loan.getLoanId(),

                loan.getEquipment().getEquipmentId(),
                loan.getEquipment().getName(),

                loan.getUser().getUserId(),
                loan.getUser().getUniversityId(),
                loan.getUser().getName(),

                loan.getReceivedTime(),
                loan.getDueTime(),
                loan.getReturnedTime(),

                loan.getStatus(),
                loan.isOverdue()
        );
    }
}