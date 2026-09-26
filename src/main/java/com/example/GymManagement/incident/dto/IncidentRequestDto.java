package com.example.GymManagement.incident.dto;

import com.example.GymManagement.incident.entity.IncidentSeverity;
import com.example.GymManagement.incident.entity.IncidentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// What the reporter is allowed to send. No status, no assignedRole, no id:
// the server decides those, so a student cannot mark their own report as resolved.
@Getter
@Setter
@NoArgsConstructor
public class IncidentRequestDto {

    @NotNull(message = "Incident type is required")
    private IncidentType incidentType;

    @NotNull(message = "Severity is required")
    private IncidentSeverity severity;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must be 1000 characters or fewer")
    private String description;

    @Size(max = 100, message = "Location must be 100 characters or fewer")
    private String location;
}