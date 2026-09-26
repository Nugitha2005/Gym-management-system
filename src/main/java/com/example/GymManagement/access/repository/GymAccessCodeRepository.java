package com.example.GymManagement.access.repository;

import com.example.GymManagement.access.entity.GymAccessCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GymAccessCodeRepository extends JpaRepository<GymAccessCode, Long> {

    // 1. Find the single active gym QR code
    Optional<GymAccessCode> findFirstByActiveTrue();

    // 2. Find all active codes so they can be deactivated during a code reset
    List<GymAccessCode> findByActiveTrue();
}