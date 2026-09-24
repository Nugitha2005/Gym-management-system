package com.example.GymManagement.incident.entity;

import com.example.GymManagement.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Student ID: [IT25100964] | Table: incident_action
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "incident_action")
public class IncidentAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long actionId;

    // 1. The incident this action belongs to.
    //    An action cannot exist on its own — this is the "many" side of the composition.
    @ManyToOne(optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    // 2. Who dealt with it — the Doctor for injuries, the Trainer for damage and facility issues
    @ManyToOne(optional = false)
    @JoinColumn(name = "handled_by_user_id", nullable = false)
    private User handledBy;

    // 3. What was actually done ("applied first aid", "removed racket from circulation")
    @Column(nullable = false, length = 1000)
    private String actionTaken;

    @Column(nullable = false)
    private LocalDateTime actionDate = LocalDateTime.now();

    // 4. Optional follow-up note (referred to hospital, replacement ordered)
    @Column(length = 500)
    private String outcome;
}