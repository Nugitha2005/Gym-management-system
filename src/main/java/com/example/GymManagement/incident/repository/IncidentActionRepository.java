package com.example.GymManagement.incident.repository;

import com.example.GymManagement.incident.entity.IncidentAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentActionRepository extends JpaRepository<IncidentAction, Long> {

    // 1. Full action history for one incident, oldest first
    List<IncidentAction> findByIncident_IncidentIdOrderByActionDateAsc(Long incidentId);

    // 2. Everything a particular doctor or trainer has handled
    List<IncidentAction> findByHandledBy_UserId(Long userId);
}
