package com.example.GymManagement.incident.service;

import com.example.GymManagement.config.ResourceNotFoundException;
import com.example.GymManagement.incident.dto.IncidentActionRequestDto;
import com.example.GymManagement.incident.dto.IncidentRequestDto;
import com.example.GymManagement.incident.dto.IncidentResponseDto;
import com.example.GymManagement.incident.entity.Incident;
import com.example.GymManagement.incident.entity.IncidentAction;
import com.example.GymManagement.incident.entity.IncidentStatus;
import com.example.GymManagement.incident.repository.IncidentRepository;
import com.example.GymManagement.incident.routing.IncidentHandler;
import com.example.GymManagement.incident.routing.IncidentHandlerFactory;
import com.example.GymManagement.user.entity.User;
import com.example.GymManagement.user.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class IncidentService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final IncidentHandlerFactory incidentHandlerFactory;

    public IncidentService(IncidentRepository incidentRepository,
                           UserRepository userRepository,
                           IncidentHandlerFactory incidentHandlerFactory) {
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
        this.incidentHandlerFactory = incidentHandlerFactory;
    }

    // ---------------- REPORT AN INCIDENT ----------------
    @Transactional
    public IncidentResponseDto reportIncident(Long reporterUserId, IncidentRequestDto request) {

        // 1. Load the person reporting it
        User reporter = loadUser(reporterUserId);

        // 2. Build the incident from the request
        Incident incident = new Incident();
        incident.setReportedBy(reporter);
        incident.setIncidentType(request.getIncidentType());
        incident.setSeverity(request.getSeverity());
        incident.setDescription(request.getDescription());
        incident.setLocation(request.getLocation());
        incident.setReportedAt(LocalDateTime.now());
        incident.setStatus(IncidentStatus.REPORTED);

        // 3. FACTORY PATTERN: ask the factory which handler this incident needs.
        //    The service does not know or care whether it gets a doctor or a staff handler.
        IncidentHandler handler = incidentHandlerFactory.createHandler(incident);

        // 4. POLYMORPHISM: the same two calls behave differently depending on the handler type
        incident.setAssignedRole(handler.getAssignedRole());
        incident.setResponseDueAt(handler.calculateResponseDueAt(incident));

        // 5. Save and convert to a response DTO
        Incident savedIncident = incidentRepository.save(incident);
        return new IncidentResponseDto(savedIncident);
    }

    // ---------------- MY REPORTS ----------------
    @Transactional(readOnly = true)
    public List<IncidentResponseDto> getMyIncidents(Long userId) {

        // 1. Fetch only incidents reported by this user, newest first
        // 2. Convert each one to a DTO
        return incidentRepository.findByReportedBy_UserIdOrderByReportedAtDesc(userId)
                .stream()
                .map(IncidentResponseDto::new)
                .toList();
    }

    // ---------------- REVIEW QUEUE (doctor / staff / admin) ----------------
    @Transactional(readOnly = true)
    public List<IncidentResponseDto> getReviewQueue(Long handlerUserId) {

        // 1. Find out which role the logged-in handler has
        User handler = loadUser(handlerUserId);
        String roleName = handler.getRole().getRoleName();

        // 2. The admin sees every open incident. Doctor and staff only see their own queue.
        List<Incident> openIncidents;
        if (ROLE_ADMIN.equals(roleName)) {
            openIncidents = incidentRepository.findByStatusNot(IncidentStatus.CLOSED);
        } else {
            openIncidents = incidentRepository.findByAssignedRoleAndStatusNot(roleName, IncidentStatus.CLOSED);
        }

        // 3. Most urgent first: HIGH before MEDIUM before LOW, then oldest first within each level
        Comparator<Incident> mostUrgentFirst = Comparator
                .comparing(Incident::getSeverity).reversed()
                .thenComparing(Incident::getReportedAt);

        // 4. Sort, then convert to DTOs
        return openIncidents.stream()
                .sorted(mostUrgentFirst)
                .map(IncidentResponseDto::new)
                .toList();
    }

    // ---------------- VIEW ONE INCIDENT ----------------
    @Transactional(readOnly = true)
    public IncidentResponseDto getIncident(Long incidentId, Long viewerUserId) {

        // 1. Load the incident and the person asking
        Incident incident = loadIncident(incidentId);
        User viewer = loadUser(viewerUserId);

        // 2. Only the reporter, the assigned handler role, or an admin may see it
        boolean isReporter = incident.getReportedBy().getUserId().equals(viewer.getUserId());
        if (!isReporter && !canHandle(viewer, incident)) {
            throw new AccessDeniedException("You cannot view this incident");
        }

        // 3. Convert to a DTO
        return new IncidentResponseDto(incident);
    }

    // ---------------- RECORD AN ACTION ----------------
    @Transactional
    public IncidentResponseDto addAction(Long incidentId, Long handlerUserId, IncidentActionRequestDto request) {

        // 1. Load the incident and the person acting on it
        Incident incident = loadIncident(incidentId);
        User handler = loadUser(handlerUserId);

        // 2. Only the role the incident was routed to (or an admin) may act on it
        if (!canHandle(handler, incident)) {
            throw new AccessDeniedException("This incident is assigned to " + incident.getAssignedRole());
        }

        // 3. A closed incident is final
        if (incident.getStatus() == IncidentStatus.CLOSED) {
            throw new IllegalArgumentException("Cannot add an action to a closed incident");
        }

        // 4. Build the action
        IncidentAction action = new IncidentAction();
        action.setHandledBy(handler);
        action.setActionTaken(request.getActionTaken());
        action.setOutcome(request.getOutcome());
        action.setActionDate(LocalDateTime.now());

        // 5. addAction() links both sides so incident_id is saved correctly
        incident.addAction(action);

        // 6. The first response automatically moves the incident into review
        if (incident.getStatus() == IncidentStatus.REPORTED) {
            incident.setStatus(IncidentStatus.IN_REVIEW);
        }

        // 7. Saving the incident also saves the new action (cascade = ALL)
        Incident savedIncident = incidentRepository.save(incident);
        return new IncidentResponseDto(savedIncident);
    }

    // ---------------- CHANGE STATUS ----------------
    @Transactional
    public IncidentResponseDto updateStatus(Long incidentId, Long handlerUserId, IncidentStatus newStatus) {

        // 1. Load the incident and the person changing it
        Incident incident = loadIncident(incidentId);
        User handler = loadUser(handlerUserId);

        // 2. Same permission rule as recording an action
        if (!canHandle(handler, incident)) {
            throw new AccessDeniedException("This incident is assigned to " + incident.getAssignedRole());
        }

        // 3. Workflow rule: only one step forward, never skip or go back
        if (!incident.canTransitionTo(newStatus)) {
            throw new IllegalArgumentException(
                    "Cannot move an incident from " + incident.getStatus() + " to " + newStatus);
        }

        // 4. Cannot resolve without recording what was actually done
        if (newStatus == IncidentStatus.RESOLVED && incident.getActions().isEmpty()) {
            throw new IllegalArgumentException("Record at least one action before resolving the incident");
        }

        // 5. Closing uses the entity's own rule
        if (newStatus == IncidentStatus.CLOSED && !incident.canBeClosed()) {
            throw new IllegalArgumentException("The incident must be RESOLVED with at least one action before closing");
        }

        // 6. Apply the change and save
        incident.setStatus(newStatus);
        Incident savedIncident = incidentRepository.save(incident);
        return new IncidentResponseDto(savedIncident);
    }

    // ---------------- HELPERS ----------------

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Incident loadIncident(Long incidentId) {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident " + incidentId + " not found"));
    }

    // Admins can handle anything. Otherwise the user's role must match the incident's assigned role.
    private boolean canHandle(User user, Incident incident) {
        String roleName = user.getRole().getRoleName();
        return ROLE_ADMIN.equals(roleName) || roleName.equals(incident.getAssignedRole());
    }
}