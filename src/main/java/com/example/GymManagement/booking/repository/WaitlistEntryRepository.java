package com.example.GymManagement.booking.repository;

import com.example.GymManagement.booking.entity.WaitlistEntry;
import com.example.GymManagement.booking.entity.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {

    long countBySession_SessionIdAndStatusIn(Long sessionId, List<WaitlistStatus> statuses);

    boolean existsByUser_UserIdAndSession_SessionIdAndStatusIn(Long userId, Long sessionId, List<WaitlistStatus> statuses);

    List<WaitlistEntry> findBySession_SessionIdAndStatusOrderByRequestedAtAsc(Long sessionId, WaitlistStatus status);

    Optional<WaitlistEntry> findByUser_UserIdAndSession_SessionIdAndStatusIn(
            Long userId, Long sessionId, List<WaitlistStatus> statuses);
}