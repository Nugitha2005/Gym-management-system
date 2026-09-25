package com.example.GymManagement.access.repository;

import com.example.GymManagement.access.entity.ClearanceStatus;
import com.example.GymManagement.access.entity.MedicalClearance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface MedicalClearanceRepository extends JpaRepository<MedicalClearance, Long> {

    // 1. A user's clearances, newest first
    List<MedicalClearance> findBySubmittedBy_UserIdOrderBySubmittedDateDescClearanceIdDesc(Long userId);

    // 2. Clearances by status, oldest submitted first
    List<MedicalClearance> findByStatusOrderBySubmittedDateAscClearanceIdAsc(ClearanceStatus status);

    // 3. Exists a clearance for a user with status in a collection
    boolean existsBySubmittedBy_UserIdAndStatusIn(Long userId, Collection<ClearanceStatus> statuses);

    // 4. A user's APPROVED clearances with expiryDate on or after a given date
    List<MedicalClearance> findBySubmittedBy_UserIdAndStatusAndExpiryDateGreaterThanEqual(
            Long userId,
            ClearanceStatus status,
            LocalDate date
    );
}