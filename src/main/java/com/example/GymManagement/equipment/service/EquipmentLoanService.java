package com.example.GymManagement.equipment.service;

import com.example.GymManagement.config.ResourceNotFoundException;
import com.example.GymManagement.equipment.dto.LoanResponseDto;
import com.example.GymManagement.equipment.entity.Equipment;
import com.example.GymManagement.equipment.entity.EquipmentLoan;
import com.example.GymManagement.equipment.entity.LoanStatus;
import com.example.GymManagement.equipment.repository.EquipmentLoanRepository;
import com.example.GymManagement.equipment.repository.EquipmentRepository;
import com.example.GymManagement.user.entity.User;
import com.example.GymManagement.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        this.equipmentLoanRepository =
                equipmentLoanRepository;

        this.equipmentRepository =
                equipmentRepository;

        this.userRepository =
                userRepository;
    }

    // =========================================================
    // BORROW EQUIPMENT
    // =========================================================

    @Transactional
    public EquipmentLoan borrowEquipment(
            Long equipmentId,
            Long userId,
            LocalDateTime dueTime) {

        // -----------------------------------------------------
        // Validate due time
        // -----------------------------------------------------

        if (dueTime == null
                || !dueTime.isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Due time must be in the future."
            );
        }

        // -----------------------------------------------------
        // Find equipment
        // -----------------------------------------------------

        Equipment equipment =
                equipmentRepository.findById(equipmentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Equipment not found with ID: "
                                                + equipmentId
                                )
                        );

        // -----------------------------------------------------
        // Check availability
        // -----------------------------------------------------

        if (!equipment.isAvailable()) {

            throw new IllegalArgumentException(
                    "Equipment is currently unavailable."
            );
        }

        // -----------------------------------------------------
        // Find user
        // -----------------------------------------------------

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with ID: "
                                                + userId
                                )
                        );

        // -----------------------------------------------------
        // MEDICAL CLEARANCE CHECK
        //
        // We will connect Nugitha's hasValidClearance()
        // here once that service is provided.
        // -----------------------------------------------------

        // -----------------------------------------------------
        // Create loan
        // -----------------------------------------------------

        EquipmentLoan loan =
                new EquipmentLoan(
                        equipment,
                        user,
                        dueTime
                );

        // -----------------------------------------------------
        // Issue equipment
        // -----------------------------------------------------

        loan.issueItem();

        // -----------------------------------------------------
        // Save equipment
        // -----------------------------------------------------

        equipmentRepository.save(equipment);

        // -----------------------------------------------------
        // Save loan
        // -----------------------------------------------------

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
                                new ResourceNotFoundException(
                                        "Equipment loan not found with ID: "
                                                + loanId
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

        refreshLateStatuses();

        return equipmentLoanRepository.findAll();
    }

    // =========================================================
    // GET LOAN BY ID
    // =========================================================

    public EquipmentLoan getLoanById(Long loanId) {

        EquipmentLoan loan =
                equipmentLoanRepository.findById(loanId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Equipment loan not found with ID: "
                                                + loanId
                                )
                        );

        refreshLateStatus(loan);

        return loan;
    }

    // =========================================================
    // GET USER LOANS
    // TRAINER / ADMIN
    // =========================================================

    public List<EquipmentLoan> getUserLoans(Long userId) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with ID: "
                                                + userId
                                )
                        );

        List<EquipmentLoan> loans =
                equipmentLoanRepository.findByUser(user);

        refreshLateStatuses(loans);

        return loans;
    }

    // =========================================================
    // GET ACTIVE USER LOANS
    // =========================================================

    public List<EquipmentLoan> getActiveUserLoans(
            Long userId) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with ID: "
                                                + userId
                                )
                        );

        List<EquipmentLoan> loans =
                equipmentLoanRepository
                        .findByUserAndStatusIn(
                                user,
                                List.of(
                                        LoanStatus.BORROWED,
                                        LoanStatus.LATE
                                )
                        );

        refreshLateStatuses(loans);

        return loans;
    }

    // =========================================================
    // GET ACTIVE LOANS
    // =========================================================

    public List<EquipmentLoan> getActiveLoans() {

        refreshLateStatuses();

        return equipmentLoanRepository.findByStatusIn(
                List.of(
                        LoanStatus.BORROWED,
                        LoanStatus.LATE
                )
        );
    }

    // =========================================================
    // GET OVERDUE LOANS
    // =========================================================

    public List<EquipmentLoan> getOverdueLoans() {

        refreshLateStatuses();

        return equipmentLoanRepository.findByStatus(
                LoanStatus.LATE
        );
    }

    // =========================================================
    // REFRESH ONE LATE STATUS
    // =========================================================

    private void refreshLateStatus(
            EquipmentLoan loan) {

        if (loan.getStatus() == LoanStatus.BORROWED
                && loan.isOverdue()) {

            loan.setStatus(LoanStatus.LATE);

            equipmentLoanRepository.save(loan);
        }
    }

    // =========================================================
    // REFRESH ALL LATE STATUSES
    // =========================================================

    private void refreshLateStatuses() {

        List<EquipmentLoan> borrowedLoans =
                equipmentLoanRepository.findByStatus(
                        LoanStatus.BORROWED
                );

        refreshLateStatuses(borrowedLoans);
    }

    private void refreshLateStatuses(
            List<EquipmentLoan> loans) {

        for (EquipmentLoan loan : loans) {

            if (loan.getStatus() == LoanStatus.BORROWED
                    && loan.isOverdue()) {

                loan.setStatus(LoanStatus.LATE);
            }
        }

        equipmentLoanRepository.saveAll(loans);
    }

    // =========================================================
    // ENTITY -> DTO
    // =========================================================

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

                loan.getStatus().name(),

                loan.isOverdue()
        );
    }
}