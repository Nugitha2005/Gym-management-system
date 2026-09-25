package com.example.GymManagement.access.service;

import com.example.GymManagement.access.dto.GymQrCodeResponseDto;
import com.example.GymManagement.access.entity.GymAccessCode;
import com.example.GymManagement.access.repository.GymAccessCodeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Design Pattern: Singleton Pattern (Head First Design Patterns, Chapter 5).
 * There must be exactly one active gym QR code in the entire system with a single global point of access.
 * Spring @Service beans are singletons by default, so the Spring IoC container guarantees that only one
 * instance of GymAccessCodeService exists at runtime. A classic private constructor with a static getInstance()
 * method is intentionally not used here because doing so would prevent Spring from constructor-injecting
 * the GymAccessCodeRepository and managing transactions.
 */
@Service
public class GymAccessCodeService {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int RANDOM_PART_LENGTH = 8;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final GymAccessCodeRepository gymAccessCodeRepository;
    private final AtomicReference<GymAccessCode> currentActiveCode = new AtomicReference<>();

    public GymAccessCodeService(GymAccessCodeRepository gymAccessCodeRepository) {
        this.gymAccessCodeRepository = gymAccessCodeRepository;
    }

    @PostConstruct
    @Transactional
    public void init() {
        // 1. Load the active gym QR code from the database on startup, or create the initial code if none exists
        GymAccessCode activeCode = gymAccessCodeRepository.findFirstByActiveTrue()
                .orElseGet(this::createAndSaveNewActiveCode);

        // 2. Cache the active code in memory so QR scans never need to query MySQL
        currentActiveCode.set(activeCode);
    }

    public boolean isValidCode(String scanned) {
        // 1. Reject null or blank scanned strings immediately
        if (scanned == null || scanned.isBlank()) {
            return false;
        }
        // 2. Compare the scanned code against the in-memory singleton code
        GymAccessCode active = currentActiveCode.get();
        return active != null && active.getCode().equals(scanned.trim());
    }

    @Transactional(readOnly = true)
    public GymQrCodeResponseDto getCurrentQrCode() {
        // 1. Retrieve the current active code from memory (or initialize if needed)
        GymAccessCode active = currentActiveCode.get();
        if (active == null) {
            active = gymAccessCodeRepository.findFirstByActiveTrue()
                    .orElseThrow(() -> new IllegalStateException("No active gym QR code is configured."));
            currentActiveCode.set(active);
        }
        // 2. Convert to response DTO
        return toDto(active);
    }

    @Transactional
    public GymQrCodeResponseDto resetCode() {
        // 1. Deactivate all previously active rows in gym_access_code
        List<GymAccessCode> activeRows = gymAccessCodeRepository.findByActiveTrue();
        for (GymAccessCode row : activeRows) {
            row.setActive(false);
        }
        gymAccessCodeRepository.saveAll(activeRows);

        // 2. Generate and persist a brand-new active QR code row
        GymAccessCode newCode = createAndSaveNewActiveCode();

        // 3. Update the in-memory cached value and return the DTO
        currentActiveCode.set(newCode);
        return toDto(newCode);
    }

    private GymAccessCode createAndSaveNewActiveCode() {
        // 1. Generate a random code in the format GYM-XXXXXXXX
        StringBuilder sb = new StringBuilder("GYM-");
        for (int i = 0; i < RANDOM_PART_LENGTH; i++) {
            int index = SECURE_RANDOM.nextInt(ALPHANUMERIC.length());
            sb.append(ALPHANUMERIC.charAt(index));
        }

        // 2. Create and save the new active GymAccessCode entity
        GymAccessCode entity = new GymAccessCode();
        entity.setCode(sb.toString());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setActive(true);
        return gymAccessCodeRepository.save(entity);
    }

    private GymQrCodeResponseDto toDto(GymAccessCode entity) {
        // 1. Map the entity fields to GymQrCodeResponseDto
        GymQrCodeResponseDto dto = new GymQrCodeResponseDto();
        dto.setCode(entity.getCode());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}