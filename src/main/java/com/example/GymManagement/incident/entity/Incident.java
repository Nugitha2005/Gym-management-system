package com.example.GymManagement.incident.entity;

import com.example.GymManagement.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Student ID: [IT25100964] | Table: incident
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "incident")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long incidentId;

    // 1. Who raised the report: a student, the trainer, or staff.
    //    @ManyToOne because one person can report many incidents over time.
    @ManyToOne(optional = false)
    @JoinColumn(name = "reported_by_user_id", nullable = false)
    private User reportedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentType incidentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private IncidentSeverity severity;

    @Column(nullable = false, length = 1000)
    private String description;

    // 2. Where in the gym it happened (free text: "treadmill area", "carom table")
    @Column(length = 100)
    private String location;

    @Column(nullable = false)
    private LocalDateTime reportedAt = LocalDateTime.now();

    // 3. Every new report starts in REPORTED and is moved along by the handler
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentStatus status = IncidentStatus.REPORTED;

    // 4. NEW: which role must deal with this incident ("DOCTOR" or "TRAINER").
    //    Set once, by the IncidentHandlerFactory, when the incident is reported.
    @Column(nullable = false, length = 20)
    private String assignedRole;

    // 5. NEW: deadline for the first response. Also set by the factory, based on severity.
    @Column(nullable = false)
    private LocalDateTime responseDueAt;

    // 6. All follow-up actions taken on this incident.
    //    cascade = ALL + orphanRemoval means deleting an incident deletes its actions.
    //    This is how COMPOSITION from the class diagram is enforced in code.
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IncidentAction> actions = new ArrayList<>();

    // 7. CHANGED: the incident TYPE decides WHO handles it. Only injuries need the doctor.
    //    Severity no longer changes who. It changes HOW FAST (see responseDueAt).
    public boolean requiresDoctor() {
        return incidentType == IncidentType.INJURY;
    }

    // 8. Keeps both sides of the relationship in sync when adding an action.
    //    Without setting the back-reference, JPA would save an action with a null incident_id.
    public void addAction(IncidentAction action) {
        actions.add(action);
        action.setIncident(this);
    }

    // 9. An incident is only closable once it is RESOLVED and has at least one action
    public boolean canBeClosed() {
        return !actions.isEmpty() && status == IncidentStatus.RESOLVED;
    }

    // 10. NEW: overdue means nobody has responded yet AND the deadline has passed
    public boolean isResponseOverdue() {
        return status == IncidentStatus.REPORTED
                && responseDueAt != null
                && LocalDateTime.now().isAfter(responseDueAt);
    }

    // 11. NEW: the workflow rule. An incident can only move ONE step forward:
    //     REPORTED -> IN_REVIEW -> RESOLVED -> CLOSED. No skipping, no going back.
    public boolean canTransitionTo(IncidentStatus nextStatus) {
        return switch (status) {
            case REPORTED -> nextStatus == IncidentStatus.IN_REVIEW;
            case IN_REVIEW -> nextStatus == IncidentStatus.RESOLVED;
            case RESOLVED -> nextStatus == IncidentStatus.CLOSED;
            case CLOSED -> false; // CLOSED is final
        };
    }
}