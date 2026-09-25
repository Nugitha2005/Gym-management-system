package com.example.GymManagement.incident.repository;

import com.example.GymManagement.incident.entity.Incident;
import com.example.GymManagement.incident.entity.IncidentSeverity;
import com.example.GymManagement.incident.entity.IncidentStatus;
import com.example.GymManagement.incident.entity.IncidentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // 1. A member viewing only their own reports
    List<Incident> findByReportedBy_UserIdOrderByReportedAtDesc(Long userId);

    // 2. The doctor's or trainer's review queue — open items, newest first
    List<Incident> findByStatusOrderByReportedAtDesc(IncidentStatus status);

    // 3. Urgent items needing immediate attention
    List<Incident> findBySeverityAndStatus(IncidentSeverity severity, IncidentStatus status);

    // 4. All injuries, for the doctor's dashboard
    List<Incident> findByIncidentTypeOrderByReportedAtDesc(IncidentType incidentType);

    // 5. Monthly safety report for the Sports Council
    List<Incident> findByReportedAtBetween(LocalDateTime start, LocalDateTime end);

    // 6. Count of still-open reports, for a dashboard badge
    long countByStatus(IncidentStatus status);

    // 7. NEW: review queue for ONE role (DOCTOR or TRAINER): everything not yet closed
    List<Incident> findByAssignedRoleAndStatusNot(String assignedRole, IncidentStatus status);

    // 8. NEW: the admin's queue: every incident not yet closed
    List<Incident> findByStatusNot(IncidentStatus status);

}