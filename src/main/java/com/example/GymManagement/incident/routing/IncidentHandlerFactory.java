package com.example.GymManagement.incident.routing;

import com.example.GymManagement.incident.entity.Incident;
import org.springframework.stereotype.Component;

// FACTORY PATTERN (Simple Factory, Head First Design Patterns Chapter 4).
// The service asks this factory for "the right handler" and never uses `new` itself.
// That keeps the routing decision in ONE place: adding a new handler type later
// only means changing this class, not the service.
@Component
public class IncidentHandlerFactory {

    public IncidentHandler createHandler(Incident incident) {

        // 1. Injuries always go to the doctor
        if (incident.requiresDoctor()) {
            return new DoctorIncidentHandler();
        }

        // 2. Equipment damage and facility issues go to the trainer (STAFF role)
        return new StaffIncidentHandler();
    }
}