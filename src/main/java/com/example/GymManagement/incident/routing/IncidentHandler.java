package com.example.GymManagement.incident.routing;

import com.example.GymManagement.incident.entity.Incident;

import java.time.LocalDateTime;

// FACTORY PATTERN: the "product" interface.
// Every handler type answers the same two questions, each in its own way:
// WHO should deal with this incident, and BY WHEN.
public interface IncidentHandler {

    // 1. The role that must handle the incident ("DOCTOR" or "TRAINER")
    String getAssignedRole();

    // 2. The deadline for the first response, based on the severity of the incident
    LocalDateTime calculateResponseDueAt(Incident incident);
}