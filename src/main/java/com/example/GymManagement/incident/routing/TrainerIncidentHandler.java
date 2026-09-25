package com.example.GymManagement.incident.routing;

import com.example.GymManagement.incident.entity.Incident;

import java.time.LocalDateTime;

// Concrete product: equipment damage and facility issues go to the gym trainer,
// who also runs the front desk. They hold the TRAINER role.
public class TrainerIncidentHandler implements IncidentHandler {

    @Override
    public String getAssignedRole() {
        return "TRAINER";
    }

    @Override
    public LocalDateTime calculateResponseDueAt(Incident incident) {

        // 1. Non-medical issues get longer deadlines than injuries
        int hoursAllowed = switch (incident.getSeverity()) {
            case HIGH -> 4;
            case MEDIUM -> 24;
            case LOW -> 72;
        };

        // 2. The deadline counts from the moment the incident was reported
        return incident.getReportedAt().plusHours(hoursAllowed);
    }
}
