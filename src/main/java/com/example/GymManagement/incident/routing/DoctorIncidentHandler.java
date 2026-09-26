package com.example.GymManagement.incident.routing;

import com.example.GymManagement.incident.entity.Incident;

import java.time.LocalDateTime;

// Concrete product: injuries go to the University Doctor, with tight deadlines.
public class DoctorIncidentHandler implements IncidentHandler {

    @Override
    public String getAssignedRole() {
        return "DOCTOR";
    }

    @Override
    public LocalDateTime calculateResponseDueAt(Incident incident) {

        // 1. Medical issues need a fast response, so the deadlines are short
        int hoursAllowed = switch (incident.getSeverity()) {
            case HIGH -> 1;
            case MEDIUM -> 4;
            case LOW -> 24;
        };

        // 2. The deadline counts from the moment the incident was reported
        return incident.getReportedAt().plusHours(hoursAllowed);
    }
}