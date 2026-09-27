package com.example.GymManagement.equipment.service;

import com.example.GymManagement.access.service.MedicalClearanceService;
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
import java.util.Set;

@Service
public class EquipmentLoanService {

    // Loans that are still out (not yet returned)
    private static final List<LoanStatus> ACTIVE_STATUSES =
            List.of(LoanStatus.BORROWED, LoanStatus.LATE);

    // Same rule as gym check-in: these roles work at the gym, so they don't need a medical clearance
    private static final Set<String> ROLES_EXEMPT_FROM_CLEARANCE =
            Set.of("TRAINER", "DOCTOR", "ADMIN");

    private final EquipmentLoanRepository equipmentLoanRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final MedicalClearanceService medicalClearanceService;

    public EquipmentLoanService(
            EquipmentLoanRepository equipmentLoanRepository,
            EquipmentRepository equipmentRepository,
            UserRepository userRepository,
            MedicalClearanceService medicalClearanceService) {

        this.equipmentLoanRepository =
                equipmentLoanRepository;

        this.equipmentRepository =
                equipmentRepository;

        this.userRepository =
                userRepository;

        this.medicalClearanceService =
                medicalClearanceService;
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
        // Medical clearance check (Nugitha's MedicalClearanceService)
        // Students and university staff need a valid clearance to
        // borrow equipment. Trainer, doctor and admin are exempt.
        // -----------------------------------------------------

        String roleName = user.getRole() != null
                ? user.getRole().getRoleName()
                : "";

        boolean exemptFromClearance =
                ROLES_EXEMPT_FROM_CLEARANCE.contains(roleName);

        if (!exemptFromClearance
                && !medicalClearanceService.hasValidClearance(userId)) {

            throw new IllegalArgumentException(
                    "This member does not have a valid medical clearance."
            );
        }

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
    // READ METHODS
    // These only read. Lateness is worked out when the response
    // is built (see toResponseDto), so viewing loans never
    // changes the database.
    // =========================================================

    // GET ALL LOANS
    @Transactional(readOnly = true)
    public List<EquipmentLoan> getAllLoans() {

        return equipmentLoanRepository.findAll();
    }

    // GET LOAN BY ID
    @Transactional(readOnly = true)
    public EquipmentLoan getLoanById(Long loanId) {

        return equipmentLoanRepository.findById(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Equipment loan not found with ID: "
                                        + loanId
                        )
                );
    }

    // GET USER LOANS (TRAINER / ADMIN, and the student's own /my)
    @Transactional(readOnly = true)
    public List<EquipmentLoan> getUserLoans(Long userId) {

        User user = findUser(userId);

        return equipmentLoanRepository.findByUser(user);
    }

    // GET ACTIVE USER LOANS
    @Transactional(readOnly = true)
    public List<EquipmentLoan> getActiveUserLoans(
            Long userId) {

        User user = findUser(userId);

        return equipmentLoanRepository
                .findByUserAndStatusIn(
                        user,
                        ACTIVE_STATUSES
                );
    }

    // GET ACTIVE LOANS
    @Transactional(readOnly = true)
    public List<EquipmentLoan> getActiveLoans() {

        return equipmentLoanRepository.findByStatusIn(
                ACTIVE_STATUSES
        );
    }

    // GET OVERDUE LOANS: still out and past the due time
    @Transactional(readOnly = true)
    public List<EquipmentLoan> getOverdueLoans() {

        return equipmentLoanRepository
                .findByStatusIn(ACTIVE_STATUSES)
                .stream()
                .filter(EquipmentLoan::isOverdue)
                .toList();
    }

    private User findUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: "
                                        + userId
                        )
                );
    }

    // =========================================================
    // ENTITY -> DTO
    // =========================================================

    public LoanResponseDto toResponseDto(
            EquipmentLoan loan) {

        // A loan that is still out and past its due time is shown as LATE,
        // worked out at the moment the response is built
        String displayStatus =
                loan.getStatus() == LoanStatus.BORROWED && loan.isOverdue()
                        ? LoanStatus.LATE.name()
                        : loan.getStatus().name();

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

                displayStatus,

                loan.isOverdue()
        );
    }
}
