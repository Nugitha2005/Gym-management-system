package com.example.GymManagement.incident.controller;

import com.example.GymManagement.incident.dto.IncidentActionRequestDto;
import com.example.GymManagement.incident.dto.IncidentRequestDto;
import com.example.GymManagement.incident.dto.IncidentResponseDto;
import com.example.GymManagement.incident.dto.IncidentStatusUpdateDto;
import com.example.GymManagement.incident.service.IncidentService;
import com.example.GymManagement.user.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    // ---------------- REPORT ----------------
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IncidentResponseDto> reportIncident(@AuthenticationPrincipal UserPrincipal principal,
                                                              @Valid @RequestBody IncidentRequestDto request) {

        // 1. The reporter is always the logged-in user, never taken from the request body
        Long reporterId = principal.getUser().getUserId();

        // 2. The service creates it and routes it through the factory
        IncidentResponseDto response = incidentService.reportIncident(reporterId, request);

        // 3. Return HTTP 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ---------------- MY REPORTS ----------------
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<IncidentResponseDto>> getMyIncidents(@AuthenticationPrincipal UserPrincipal principal) {

        // 1. Only this user's own reports
        List<IncidentResponseDto> response = incidentService.getMyIncidents(principal.getUser().getUserId());

        // 2. Return HTTP 200 OK
        return ResponseEntity.ok(response);
    }

    // ---------------- REVIEW QUEUE ----------------
    @GetMapping("/queue")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'ADMIN')")
    public ResponseEntity<List<IncidentResponseDto>> getReviewQueue(@AuthenticationPrincipal UserPrincipal principal) {

        // 1. The service picks the right queue from the handler's role and sorts it by urgency
        List<IncidentResponseDto> response = incidentService.getReviewQueue(principal.getUser().getUserId());

        // 2. Return HTTP 200 OK
        return ResponseEntity.ok(response);
    }

    // ---------------- VIEW ONE ----------------
    @GetMapping("/{incidentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IncidentResponseDto> getIncident(@PathVariable Long incidentId,
                                                           @AuthenticationPrincipal UserPrincipal principal) {

        // 1. The service checks the viewer is the reporter, the assigned handler, or an admin
        IncidentResponseDto response = incidentService.getIncident(incidentId, principal.getUser().getUserId());

        // 2. Return HTTP 200 OK
        return ResponseEntity.ok(response);
    }

    // ---------------- RECORD AN ACTION ----------------
    @PostMapping("/{incidentId}/actions")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'ADMIN')")
    public ResponseEntity<IncidentResponseDto> addAction(@PathVariable Long incidentId,
                                                         @AuthenticationPrincipal UserPrincipal principal,
                                                         @Valid @RequestBody IncidentActionRequestDto request) {

        // 1. The service checks the handler's role matches the incident's assigned role
        IncidentResponseDto response = incidentService.addAction(incidentId, principal.getUser().getUserId(), request);

        // 2. Return HTTP 201 Created, because a new action row was created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ---------------- CHANGE STATUS ----------------
    // PUT rather than PATCH, because your CORS config only allows GET, POST, PUT, DELETE
    @PutMapping("/{incidentId}/status")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'ADMIN')")
    public ResponseEntity<IncidentResponseDto> updateStatus(@PathVariable Long incidentId,
                                                            @AuthenticationPrincipal UserPrincipal principal,
                                                            @Valid @RequestBody IncidentStatusUpdateDto request) {

        // 1. The service enforces the one-step-forward workflow rule
        IncidentResponseDto response = incidentService.updateStatus(
                incidentId, principal.getUser().getUserId(), request.getStatus());

        // 2. Return HTTP 200 OK
        return ResponseEntity.ok(response);
    }
}