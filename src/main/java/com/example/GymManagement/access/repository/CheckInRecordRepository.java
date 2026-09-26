package com.example.GymManagement.access.repository;

import com.example.GymManagement.access.entity.CheckInRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRecordRepository extends JpaRepository<CheckInRecord, Long> {

    // 1. A user's open record (exitTime IS NULL), most recent first
    Optional<CheckInRecord> findFirstByUser_UserIdAndExitTimeIsNullOrderByEntryTimeDesc(Long userId);

    // 2. All open records currently inside the gym
    List<CheckInRecord> findByExitTimeIsNullOrderByEntryTimeAsc();

    // 3. A user's check-in history records, newest first
    List<CheckInRecord> findByUser_UserIdOrderByEntryTimeDesc(Long userId);

    // 4. Records linked to a given booking id
    List<CheckInRecord> findByBooking_BookingId(Long bookingId);
}