package com.example.GymManagement.incident.dto;

import com.example.GymManagement.incident.entity.IncidentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Body for moving an incident to its next status, e.g. { "status": "RESOLVED" }
@Getter
@Setter
@NoArgsConstructor
public class IncidentStatusUpdateDto {

    @NotNull(message = "New status is required")
    private IncidentStatus status;
}