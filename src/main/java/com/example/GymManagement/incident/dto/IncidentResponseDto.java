package com.example.GymManagement.incident.dto;

import com.example.GymManagement.incident.entity.Incident;
import com.example.GymManagement.incident.entity.IncidentSeverity;
import com.example.GymManagement.incident.entity.IncidentStatus;
import com.example.GymManagement.incident.entity.IncidentType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class IncidentResponseDto {

    private Long incidentId;
    private IncidentType incidentType;
    private IncidentSeverity severity;
    private String description;
    private String location;
    private LocalDateTime reportedAt;
    private IncidentStatus status;
    private String assignedRole;
    private LocalDateTime responseDueAt;
    private boolean responseOverdue;
    private Long reportedByUserId;
    private String reportedByName;
    private List<IncidentActionResponseDto> actions;

    public IncidentResponseDto(Incident incident) {

        // 1. Copy the incident's own fields
        this.incidentId = incident.getIncidentId();
        this.incidentType = incident.getIncidentType();
        this.severity = incident.getSeverity();
        this.description = incident.getDescription();
        this.location = incident.getLocation();
        this.reportedAt = incident.getReportedAt();
        this.status = incident.getStatus();

        // 2. Routing result decided by the factory
        this.assignedRole = incident.getAssignedRole();
        this.responseDueAt = incident.getResponseDueAt();

        // 3. Computed value from the entity method
        this.responseOverdue = incident.isResponseOverdue();

        // 4. Only safe details about the reporter
        this.reportedByUserId = incident.getReportedBy().getUserId();
        this.reportedByName = incident.getReportedBy().getName();

        // 5. Convert every action into its own DTO. The action DTOs do not point back
        //    to this incident, so the JSON cannot loop forever.
        this.actions = incident.getActions()
                .stream()
                .map(IncidentActionResponseDto::new)
                .toList();
    }
}