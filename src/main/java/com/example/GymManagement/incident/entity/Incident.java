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

    // 1. Who raised the report — a student, the trainer, or staff.
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

    // 2. Where in the gym it happened (free text — "treadmill area", "carom table")
    @Column(length = 100)
    private String location;

    @Column(nullable = false)
    private LocalDateTime reportedAt = LocalDateTime.now();

    // 3. Every new report starts in REPORTED and is moved along by the handler
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentStatus status = IncidentStatus.REPORTED;

    // 4. All follow-up actions taken on this incident.
    //    cascade = ALL + orphanRemoval means deleting an incident deletes its actions —
    //    this is how COMPOSITION from the class diagram is enforced in code.
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IncidentAction> actions = new ArrayList<>();

    // 5. Business rule: injuries always need the doctor, and so does anything HIGH severity.
    //    Lives on the entity because it only reads this object's own fields.
    public boolean requiresDoctor() {
        return incidentType == IncidentType.INJURY || severity == IncidentSeverity.HIGH;
    }

    // 6. Keeps both sides of the relationship in sync when adding an action.
    //    Without setting the back-reference, JPA would save an action with a null incident_id.
    public void addAction(IncidentAction action) {
        actions.add(action);
        action.setIncident(this);
    }

    // 7. An incident is only closable once at least one action has been recorded
    public boolean canBeClosed() {
        return !actions.isEmpty() && status == IncidentStatus.RESOLVED;
    }
}