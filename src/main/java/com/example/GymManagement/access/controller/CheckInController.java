package com.example.GymManagement.access.controller;

import com.example.GymManagement.access.dto.CheckInRecordResponseDto;
import com.example.GymManagement.access.dto.CheckInResultDto;
import com.example.GymManagement.access.dto.GymQrCodeResponseDto;
import com.example.GymManagement.access.dto.InsideNowResponseDto;
import com.example.GymManagement.access.dto.ScanRequestDto;
import com.example.GymManagement.access.service.CheckInService;
import com.example.GymManagement.access.service.GymAccessCodeService;
import com.example.GymManagement.user.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService checkInService;
    private final GymAccessCodeService gymAccessCodeService;

    public CheckInController(CheckInService checkInService, GymAccessCodeService gymAccessCodeService) {
        this.checkInService = checkInService;
        this.gymAccessCodeService = gymAccessCodeService;
    }

    @PostMapping("/scan")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckInResultDto> scanQr(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ScanRequestDto scanRequestDto
    ) {
        // 1. Process the QR code scan for the logged-in student
        Long userId = principal.getUser().getUserId();
        return ResponseEntity.ok(checkInService.scanQr(userId, scanRequestDto.getCode()));
    }

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckInRecordResponseDto> getStatus(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // 1. Return the user's open check-in record, or 404 if not currently inside
        Long userId = principal.getUser().getUserId();
        return ResponseEntity.ok(checkInService.getCurrentStatus(userId));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CheckInRecordResponseDto>> getMyCheckInHistory(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // 1. Return the logged-in user's check-in history, newest first
        Long userId = principal.getUser().getUserId();
        return ResponseEntity.ok(checkInService.getMyHistory(userId));
    }

    @PostMapping("/manual/{userId}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<CheckInResultDto> manualCheckInOrOut(
            @PathVariable("userId") Long studentUserId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // 1. Perform manual staff check-in or check-out for a student without a phone
        Long staffUserId = principal.getUser().getUserId();
        return ResponseEntity.ok(checkInService.manualCheckInOrOut(studentUserId, staffUserId));
    }

    @GetMapping("/inside")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<InsideNowResponseDto> getCurrentlyInside() {
        // 1. Return the live list of people inside the gym along with capacity metrics
        return ResponseEntity.ok(checkInService.getInsideNow());
    }

    @PutMapping("/records/{recordId}/force-checkout")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<CheckInRecordResponseDto> forceCheckout(
            @PathVariable("recordId") Long recordId
    ) {
        // 1. Force-close an open check-in record selected by staff or admin
        return ResponseEntity.ok(checkInService.forceCheckout(recordId));
    }

    @GetMapping("/qr")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<GymQrCodeResponseDto> getCurrentQrCode() {
        // 1. Return the current active gym QR code text for printing at the front door
        return ResponseEntity.ok(gymAccessCodeService.getCurrentQrCode());
    }

    @PutMapping("/qr/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GymQrCodeResponseDto> resetQrCode() {
        // 1. Deactivate the old QR code and generate a new active code
        return ResponseEntity.ok(gymAccessCodeService.resetCode());
    }
}
